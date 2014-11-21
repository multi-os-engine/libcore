/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.net.ssl;

import java.net.InetAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.security.auth.x500.X500Principal;

/**
 * {@link HostnameVerifier} consistent with the intersection of {@code RFC 2818} and {@code Baseline
 * Requirements for the Issuance and Management of Publicly-Trusted Certificates, v1.0}.
 *
 * @hide accessible via HttpsURLConnection.getDefaultHostnameVerifier()
 */
public final class DefaultHostnameVerifier implements HostnameVerifier {
    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;

    @Override
    public final boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certificates = session.getPeerCertificates();
            return verify(host, (X509Certificate) certificates[0]);
        } catch (SSLException e) {
            return false;
        }
    }

    public boolean verify(String host, X509Certificate certificate) {
        return InetAddress.isNumeric(host)
                ? verifyIpAddress(host, certificate)
                : verifyHostName(host, certificate);
    }

    /**
     * Returns true if {@code certificate} matches {@code ipAddress}.
     */
    private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
        for (String altName : getSubjectAltNames(certificate, ALT_IPA_NAME)) {
            if (ipAddress.equalsIgnoreCase(altName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if {@code certificate} matches {@code hostName}.
     */
    private boolean verifyHostName(String hostName, X509Certificate certificate) {
        hostName = hostName.toLowerCase(Locale.US);
        boolean hasDns = false;
        for (String altName : getSubjectAltNames(certificate, ALT_DNS_NAME)) {
            hasDns = true;
            if (verifyHostName(hostName, altName)) {
                return true;
            }
        }

        if (!hasDns) {
            X500Principal principal = certificate.getSubjectX500Principal();
            // RFC 2818 advises using the most specific name for matching.
            String cn = new DistinguishedNameParser(principal).findMostSpecific("cn");
            if (cn != null) {
                return verifyHostName(hostName, cn);
            }
        }

        return false;
    }

    private List<String> getSubjectAltNames(X509Certificate certificate, int type) {
        List<String> result = new ArrayList<String>();
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                return Collections.emptyList();
            }
            for (Object subjectAltName : subjectAltNames) {
                List<?> entry = (List<?>) subjectAltName;
                if (entry == null || entry.size() < 2) {
                    continue;
                }
                Integer altNameType = (Integer) entry.get(0);
                if (altNameType == null) {
                    continue;
                }
                if (altNameType == type) {
                    String altName = (String) entry.get(1);
                    if (altName != null) {
                        result.add(altName);
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Checks whether {@code hostName} matches the domain name {@code pattern}.
     *
     * @param hostName lower-case host name.
     * @param pattern domain name pattern from certificate. May be a wildcard pattern such as
     *        {@code *.android.com}.
     */
    public boolean verifyHostName(String hostName, String pattern) {
        // Basic sanity checks
        if ((hostName == null) || (hostName.isEmpty()) || (hostName.startsWith("."))
                || (hostName.endsWith("..")) || (hostName.contains("*"))) {
            // Invalid domain name
            return false;
        }
        if ((pattern == null) || (pattern.isEmpty()) || (pattern.startsWith("."))
                || (pattern.endsWith(".."))) {
            // Invalid pattern/domain name
            return false;
        }

        // Normalize hostName and pattern by turning them into absolute domain names if they are not
        // yet absolute. This is needed because server certificates do not normally contain absolute
        // names or patterns, but they should be treated as absolute. At the same time, any hostName
        // presented to this method should also be treated as absolute for the purposes of matching
        // to the server certificate.
        //     www.android.com  matches www.android.com
        //     www.android.com  matches www.android.com.
        //     www.android.com. matches www.android.com.
        //     www.android.com. matches www.android.com
        if (!hostName.endsWith(".")) {
            hostName += '.';
        }
        if (!pattern.endsWith(".")) {
            pattern += '.';
        }
        // hostName and pattern are now absolute domain names.

        pattern = pattern.toLowerCase(Locale.US);
        // hostName and pattern are now in lower case -- domain names are case-insensitive.

        // WILDCARD PATTERN RULES:
        // 1. Asterisk (*) is only permitted in the left-most domain name label and must be the
        //    only character in that label (i.e., must match the whole left-most label).
        //    For example, *.example.com is permitted, while *a.example.com, a*.example.com,
        //    a*b.example.com, a.*.example.com are not permitted.
        // 2. Asterisk (*) cannot match across domain name labels.
        //    For example, *.example.com matches test.example.com but does not match
        //    sub.test.example.com.

        if (!pattern.startsWith("*.")) {
            // Not a wildcard pattern -- hostName and pattern must match exactly.
            return hostName.equals(pattern);
        }
        // Wildcard pattern -- asterisk must match the whole left-most label of hostName. No need
        // to check whether it contains more asterisks because hostName does not contain asterisks
        // and thus such a pattern won't match anyway.

        if (hostName.length() < pattern.length()) {
            // Optimization: hostName too short to match the pattern. hostName must be at least as
            // long as the pattern because asterisk must match a the whole left-most label and
            // hostName cannot start with an empty label.
            return false;
        }

        if (!containsAtLeastTwoDomainNameLabelsExcludingRoot(pattern)) {
            return false; // reject wildcard patterns consisting of only one label.
        }

        // hostName must end with the region of pattern following the asterisk.
        String suffix = pattern.substring(1);
        if (!hostName.endsWith(suffix)) {
            // hostName does not end with the suffix
            return false;
        }

        // Check that asterisk did not match across domain name labels.
        int suffixStartIndexInHostName = hostName.length() - suffix.length();
        if (hostName.lastIndexOf('.', suffixStartIndexInHostName - 1) != -1) {
            // Asterisk is matching across domain name labels -- not permitted.
            return false;
        }

        // hostName matches pattern
        return true;
    }

    /**
     * Checks whether the provided hostname consists of at least two domain name labels, excluding
     * the root label.
     *
     * <p>For example, this method returns {@code true} for {@code www.android.com} and
     * {@code foo.com} and {@code foo.com.}, and returns {@code false} for {@code foo} and
     * {@code foo.}.
     */
    private static boolean containsAtLeastTwoDomainNameLabelsExcludingRoot(String hostname) {
        int delimiterIndex = hostname.indexOf('.');
        if (delimiterIndex == -1) {
            // No delimiters -- only one label
            return false;
        }
        if (delimiterIndex == hostname.length() - 1) {
            // Only one delimiter at the every end of the hostname -- this is an absolute hostname
            // consisting of one label
            return false;
        }
        // At least two labels
        return true;
    }
}
