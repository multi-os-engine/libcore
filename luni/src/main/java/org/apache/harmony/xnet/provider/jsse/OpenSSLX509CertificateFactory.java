/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OpenSSLX509CertificateFactory extends CertificateFactorySpi {
    private static final byte[] PKCS7_MARKER = "-----BEGIN PKCS7".getBytes();

    private static final int PUSHBACK_SIZE = 64;

    @Override
    public Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException {
        final boolean markable = inStream.markSupported();
        if (markable) {
            inStream.mark(PKCS7_MARKER.length);
        }

        final PushbackInputStream pbis = new PushbackInputStream(inStream, PUSHBACK_SIZE);
        try {
            final byte[] buffer = new byte[PKCS7_MARKER.length];

            final int len = pbis.read(buffer);
            if (len < 0) {
                /* No need to reset here. The stream was empty or EOF. */
                throw new CertificateException("inStream is empty");
            }
            pbis.unread(buffer, 0, len);

            if (buffer[0] == '-') {
                if (len == PKCS7_MARKER.length && Arrays.equals(PKCS7_MARKER, buffer)) {
                    List<OpenSSLX509Certificate> certs = OpenSSLX509Certificate
                            .fromPkcs7PemInputStream(pbis);
                    if (certs.size() == 0) {
                        return null;
                    }
                    certs.get(0);
                } else {
                    return OpenSSLX509Certificate.fromX509PemInputStream(pbis);
                }
            }

            /* PKCS#7 bags have a byte 0x06 at position 4 in the stream. */
            if (buffer[4] == 0x06) {
                List<OpenSSLX509Certificate> certs = OpenSSLX509Certificate
                        .fromPkcs7DerInputStream(pbis);
                if (certs.size() == 0) {
                    return null;
                }
                return certs.get(0);
            } else {
                return OpenSSLX509Certificate.fromX509DerInputStream(pbis);
            }
        } catch (IOException e) {
            if (markable) {
                try {
                    inStream.reset();
                } catch (IOException ignored) {
                }
            }
            throw new CertificateException(e);
        }
    }

    @Override
    public Collection<? extends java.security.cert.Certificate> engineGenerateCertificates(
            InputStream inStream) throws CertificateException {
        try {
            if (inStream == null || inStream.available() == 0) {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            throw new CertificateException("Problem reading input stream", e);
        }

        final boolean markable = inStream.markSupported();
        if (markable) {
            inStream.mark(PUSHBACK_SIZE);
        }

        /* Attempt to see if this is a PKCS#7 bag. */
        final PushbackInputStream pbis = new PushbackInputStream(inStream, PUSHBACK_SIZE);
        try {
            final byte[] buffer = new byte[PKCS7_MARKER.length];

            final int len = pbis.read(buffer);
            if (len < 0) {
                /* No need to reset here. The stream was empty or EOF. */
                throw new CertificateException("inStream is empty");
            }
            pbis.unread(buffer, 0, len);

            if (len == PKCS7_MARKER.length && Arrays.equals(PKCS7_MARKER, buffer)) {
                return OpenSSLX509Certificate.fromPkcs7PemInputStream(pbis);

            }

            /* PKCS#7 bags have a byte 0x06 at position 4 in the stream. */
            if (buffer[4] == 0x06) {
                return OpenSSLX509Certificate.fromPkcs7DerInputStream(pbis);
            }
        } catch (IOException e) {
            if (markable) {
                try {
                    inStream.reset();
                } catch (IOException ignored) {
                }
            }
            throw new CertificateException(e);
        }

        /*
         * It wasn't, so just try to keep grabbing certificates until we can't
         * anymore.
         */
        final Collection<Certificate> coll = new ArrayList<Certificate>();
        Certificate c = null;
        do {
            /*
             * If this stream supports marking, try to mark here in case there
             * is an error during certificate generation.
             */
            if (markable) {
                inStream.mark(PUSHBACK_SIZE);
            }

            try {
                c = engineGenerateCertificate(pbis);
                coll.add(c);
            } catch (CertificateException e) {
                /*
                 * If this stream supports marking, attempt to reset it to the
                 * mark before the failure.
                 */
                if (markable) {
                    try {
                        inStream.reset();
                    } catch (IOException ignored) {
                    }
                }

                c = null;
            }
        } while (c != null);

        return coll;
    }

    @Override
    public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<? extends CRL> engineGenerateCRLs(InputStream inStream) throws CRLException {
        // TODO Auto-generated method stub
        return null;
    }

}
