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

/**
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.provider.cert;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.asn1.ASN1Any;
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.pkcs7.ContentInfo;
import org.apache.harmony.security.pkcs7.SignedData;
import org.apache.harmony.security.x509.Certificate;

/**
 * This class is an implementation of X.509 CertPath. This implementation
 * provides ability to create the instance of X.509 Certification Path
 * by several means:<br>
 *
 * &nbsp;  1. It can be created over the list of X.509 certificates
 * (implementations of X509Certificate class) provided in constructor.<br>
 *
 * &nbsp;  2. It can be created by means of <code>getInstance</code> methods
 * on the base of the following ASN.1 DER encoded forms:<br>
 *
 * &nbsp;&nbsp;  - PkiPath as defined in
 * ITU-T Recommendation X.509(2000) Corrigendum 1(2001)
 * (can be seen at
 * ftp://ftp.bull.com/pub/OSIdirectory/DefectResolution/TechnicalCorrigenda/ApprovedTechnicalCorrigendaToX.509/8%7CX.509-TC1(4th).pdf)
 * <br>
 * &nbsp;&nbsp;  - PKCS #7 SignedData object provided in the form of
 * ContentInfo structure. CertPath object is generated on the base of
 * certificates presented in <code>certificates</code> field of the SignedData
 * object which in its turn is retrieved from ContentInfo structure.
 * (see http://www.ietf.org/rfc/rfc2315.txt
 * for more info on PKCS #7)
 * <br>
 * &nbsp;
 */
public class X509CertPathImpl extends CertPath {

    /** API name for CertPath encoding in PKCS#7 format. */
    private static final String PKCS7_NAME = "PKCS7";

    /** API name for CertPath encoding in PkiPath format. */
    private static final String PKI_PATH_NAME = "PkiPath";

    /**
     * @serial
     */
    private static final long serialVersionUID = 7989755106209515436L;

    /** supported encoding types */
    public static final int PKI_PATH = 0;
    public static final int PKCS7 = 1;

    /**
     * Supported encoding names. The first entry represents the default
     * encoding.
     */
    private static final String[] encodingsArr = new String[] { PKI_PATH_NAME, PKCS7_NAME };

    /** Unmodifiable list of encodings for the API. */
    static final List<String> encodings = Collections.unmodifiableList(Arrays.asList(encodingsArr));

    /** The list of certificates in the order of target toward trust anchor. */
    private final List<X509Certificate> certificates;

    /** PkiPath encoding of the certification path. */
    private byte[] pkiPathEncoding;

    /** PKCS7 encoding of the certification path. */
    private byte[] pkcs7Encoding;

    /**
     * Creates an instance of X.509 Certification Path over the specified
     * list of certificates.
     * @throws CertificateException if some of the object in the list
     * is not an instance of subclass of X509Certificate.
     */
    public X509CertPathImpl(List<? extends java.security.cert.Certificate> certs)
            throws CertificateException {
        super("X.509");
        int size = certs.size();
        this.certificates = new ArrayList<X509Certificate>(size);
        for (int i = 0; i < size; i++) {
            final Object cert = certs.get(i);
            if (!(cert instanceof X509Certificate)) {
                throw new CertificateException("Certificate " + i + " is not an X.509 certificate");
            }

            certificates.add((X509Certificate) cert);
        }
    }

    /*
     * Internally used constructor.
     * Creates an X.509 Certification Path over the specified
     * list of certificates and their encoded form of specified type.
     * @param certs - the list of certificates
     * @param type - the type of the encoded form on the base of which
     * this list of certificates had been built.
     * @param encoding - encoded form of certification path.
     */
    private X509CertPathImpl(List<X509Certificate> certs, int type, byte[] encoding) {
        super("X.509");
        if (type == PKI_PATH) {
            this.pkiPathEncoding = encoding;
        } else if (type == PKCS7) { // PKCS7
            this.pkcs7Encoding = encoding;
        } else {
            throw new IllegalArgumentException("unknown encoding type: " + type);
        }

        // We do not need the type check and list cloning here,
        // because it has been done during decoding.
        certificates = certs;
    }

    /**
     * Returns true if {@code certs} is already sorted in such a way that the
     * end entity is at position 0, each subsequent entry is the issuer of the
     * previous entry, and the certificate before the trust anchor is at the end
     * of the list.
     */
    private static boolean isSorted(List<X509Certificate> certs) {
        if (certs.size() == 0) {
            return true;
        }

        X500Principal issuer = certs.get(0).getIssuerX500Principal();

        for (int i = 1; i < certs.size(); i++) {
            final X509Certificate cert = certs.get(i);

            if (!cert.getSubjectX500Principal().equals(issuer)) {
                return false;
            }

            issuer = cert.getIssuerX500Principal();
        }

        return true;
    }

    /**
     * Sorts the {@code certs} in such a way that the end entity is at position
     * 0, each subsequent entry is the issuer of the previous entry, and the
     * certificate before the trust anchor is at the end of the list.
     */
    private static List<X509Certificate> sortCerts(final List<X509Certificate> certs) {
        /* If we're already sorted, bail early. */
        if (isSorted(certs)) {
            return certs;
        }

        /*
         * Try to narrow it down to one end entity. If it isn't possible, return
         * the original list.
         */
        final List<X509Certificate> leftOver = new ArrayList<X509Certificate>();
        final List<X509Certificate> endEntities = new ArrayList<X509Certificate>(certs);

        final Iterator<X509Certificate> it = endEntities.iterator();
        while (it.hasNext()) {
            final X509Certificate possibleIssuer = it.next();
            final X500Principal endEntityPrincipal = possibleIssuer.getSubjectX500Principal();

            boolean endEntity = true;

            for (X509Certificate cert : certs) {
                if (endEntityPrincipal.equals(cert.getIssuerX500Principal())) {
                    it.remove();
                    endEntity = false;
                    break;
                }
            }

            if (!endEntity) {
                leftOver.add(possibleIssuer);
            }
        }

        if (endEntities.size() != 1) {
            return certs;
        }

        /*
         * Now that we have our single end entity, go through the list and try
         * to sort it out. If it isn't possible, return the original list.
         */
        final X509Certificate endEntity = endEntities.get(0);

        final List<X509Certificate> sortedList = new ArrayList<X509Certificate>(certs.size());
        sortedList.add(endEntity);

        X500Principal issuer = endEntity.getIssuerX500Principal();

        while (leftOver.size() > 0) {
            boolean foundNext = false;

            for (int i = 0; i < leftOver.size(); i++) {
                final X509Certificate nextCert = leftOver.get(i);
                final X500Principal subject = nextCert.getSubjectX500Principal();

                if (issuer.equals(subject)) {
                    foundNext = true;

                    sortedList.add(nextCert);
                    leftOver.remove(i);

                    issuer = nextCert.getIssuerX500Principal();
                    break;
                }
            }

            if (!foundNext) {
                return certs;
            }
        }

        return sortedList;
    }

    /**
     * Extract a CertPath from a PKCS#7 {@code contentInfo} object.
     */
    private static X509CertPathImpl getCertPathFromContentInfo(ContentInfo contentInfo)
            throws CertificateException {
        final SignedData sd = contentInfo.getSignedData();
        if (sd == null) {
            throw new CertificateException("Incorrect PKCS7 encoded form: missing signed data");
        }

        List<Certificate> certs = sd.getCertificates();
        if (certs == null) {
            certs = new ArrayList<Certificate>();
        }

        final List<X509Certificate> result = new ArrayList<X509Certificate>(certs.size());
        for (Certificate cert : certs) {
            result.add(new X509CertImpl(cert));
        }

        return new X509CertPathImpl(sortCerts(result), PKCS7, contentInfo.getEncoded());
    }

    /**
     * Generates certification path object on the base of PkiPath encoded form
     * provided via input stream.
     * 
     * @throws CertificateException if some problems occurred during the
     *             decoding.
     */
    public static X509CertPathImpl getInstance(InputStream in) throws CertificateException {
        try {
            return (X509CertPathImpl) ASN1.decode(in);
        } catch (IOException e) {
            throw new CertificateException("Incorrect encoded form: " + e.getMessage());
        }
    }

    /**
     * Generates certification path object on the base of encoding provided via
     * input stream. The format of provided encoded form is specified by
     * parameter <code>encoding</code>.
     * @throws CertificateException if specified encoding form is not supported,
     * or some problems occurred during the decoding.
     */
    public static X509CertPathImpl getInstance(InputStream in, String encoding)
            throws CertificateException {
        try {
            if (PKI_PATH_NAME.equals(encoding)) {
                // generate the object from PkiPath encoded form
                return (X509CertPathImpl) ASN1.decode(in);
            } else if (PKCS7_NAME.equals(encoding)) {
                // generate the object from PKCS #7 encoded form
                return getCertPathFromContentInfo((ContentInfo) ContentInfo.ASN1.decode(in));
            } else {
                throw new CertificateException("Unsupported encoding: " + encoding);
            }
        } catch (IOException e) {
            throw new CertificateException("Problem decoding CertPath", e);
        }
    }

    /**
     * Generates certification path object on the base of PkiPath
     * encoded form provided via array of bytes.
     * @throws CertificateException if some problems occurred during
     * the decoding.
     */
    public static X509CertPathImpl getInstance(byte[] in) throws CertificateException {
        try {
            return (X509CertPathImpl) ASN1.decode(in);
        } catch (IOException e) {
            throw new CertificateException("Incorrect encoded form: " + e.getMessage());
        }
    }

    /**
     * Generates certification path object on the base of encoding provided via
     * array of bytes. The format of provided encoded form is specified by
     * parameter {@code encoding}.
     * 
     * @throws CertificateException if specified encoding form is not supported,
     *             or some problems occurred during the decoding.
     */
    public static X509CertPathImpl getInstance(byte[] in, String encoding)
            throws CertificateException {
        try {
            if (PKI_PATH_NAME.equals(encoding)) {
                // generate the object from PkiPath encoded form
                return (X509CertPathImpl) ASN1.decode(in);
            } else if (PKCS7_NAME.equals(encoding)) {
                // generate the object from PKCS #7 encoded form
                return getCertPathFromContentInfo((ContentInfo) ContentInfo.ASN1.decode(in));
            } else {
                throw new CertificateException("Unsupported encoding: " + encoding);
            }
        } catch (IOException e) {
            throw new CertificateException("Incorrect encoded form", e);
        }
    }

    // ---------------------------------------------------------------------
    // ---- java.security.cert.CertPath abstract method implementations ----
    // ---------------------------------------------------------------------

    /**
     * @see java.security.cert.CertPath#getCertificates()
     * method documentation for more info
     */
    @Override
    public List<X509Certificate> getCertificates() {
        return Collections.unmodifiableList(certificates);
    }

    /**
     * @see java.security.cert.CertPath#getEncoded()
     * method documentation for more info
     */
    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        if (pkiPathEncoding == null) {
            pkiPathEncoding = ASN1.encode(this);
        }

        return pkiPathEncoding.clone();
    }

    /**
     * @see java.security.cert.CertPath#getEncoded(String)
     * method documentation for more info
     */
    @Override
    public byte[] getEncoded(String encoding) throws CertificateEncodingException {
        if (PKI_PATH_NAME.equals(encoding)) {
            // PkiPath encoded form
            return getEncoded();
        } else if (PKCS7_NAME.equals(encoding)) {
            // PKCS7 encoded form
            if (pkcs7Encoding == null) {
                pkcs7Encoding = PKCS7_SIGNED_DATA_OBJECT.encode(this);
            }

            return pkcs7Encoding.clone();
        } else {
            throw new CertificateEncodingException("Unsupported encoding: " + encoding);
        }
    }

    /**
     * @see java.security.cert.CertPath#getEncodings()
     * method documentation for more info
     */
    @Override
    public Iterator<String> getEncodings() {
        return encodings.iterator();
    }

    /**
     * ASN.1 DER Encoder/Decoder for PkiPath structure.
     */
    public static final ASN1SequenceOf ASN1 = new ASN1SequenceOf(ASN1Any.getInstance()) {
        /**
         * Builds the instance of X509CertPathImpl on the base of the list of
         * ASN.1 encodings of X.509 certificates provided via PkiPath structure.
         * This method participates in decoding process.
         */
        public Object getDecodedObject(BerInputStream in) throws IOException {
            // retrieve the decoded content
            final List<byte[]> encodedCerts = (List<byte[]>) in.content;

            final int size = encodedCerts.size();
            final List<X509Certificate> certificates = new ArrayList<X509Certificate>(size);

            for (int i = size - 1; i >= 0; i--) {
                // create the X.509 certificate on the base of its encoded form
                // and add it to the list.
                certificates.add(new X509CertImpl((Certificate) Certificate.ASN1
                        .decode(encodedCerts.get(i))));
            }

            // create and return the resulting object
            return new X509CertPathImpl(sortCerts(certificates), PKI_PATH, in.getEncoded());
        }

        /**
         * Returns the Collection of the encoded form of certificates contained
         * in the X509CertPathImpl object to be encoded.
         * This method participates in encoding process.
         */
        public Collection<byte[]> getValues(Object object) {
            // object to be encoded
            final X509CertPathImpl cp = (X509CertPathImpl) object;

            // if it has no certificates in it - create the sequence of size 0
            if (cp.certificates == null) {
                return new ArrayList<byte[]>();
            }

            final int size = cp.certificates.size();
            final List<byte[]> encodings = new ArrayList<byte[]>(size);

            try {
                for (int i = size - 1; i >= 0; i--) {
                    // get the encoded form of certificate and place it into the
                    // list to be encoded in PkiPath format
                    encodings.add(cp.certificates.get(i).getEncoded());
                }
            } catch (CertificateEncodingException e) {
                throw new IllegalArgumentException("Encoding error occurred", e);
            }

            return encodings;
        }
    };


    //
    // encoder for PKCS#7 SignedData
    // it is assumed that only certificate field is important
    // all other fields contain precalculated encodings:
    //
    // encodes X509CertPathImpl objects
    //
    private static final ASN1Sequence ASN1_SIGNED_DATA = new ASN1Sequence(
            new ASN1Type[] {
                    // version ,digestAlgorithms, content info
                    ASN1Any.getInstance(),
                    // certificates
                    new ASN1Implicit(0, ASN1),
                    // set of crls is optional and is missed here
                    ASN1Any.getInstance(),// signers info
            }) {

        // precalculated ASN.1 encodings for
        // version ,digestAlgorithms, content info field of SignedData
        private final byte[] PRECALCULATED_HEAD = new byte[] { 0x02, 0x01,
                0x01,// version (v1)
                0x31, 0x00,// empty set of DigestAlgorithms
                0x30, 0x03, 0x06, 0x01, 0x00 // empty ContentInfo with oid=0
        };

        // precalculated empty set of SignerInfos
        private final byte[] SIGNERS_INFO = new byte[] { 0x31, 0x00 };

        protected void getValues(Object object, Object[] values) {
            values[0] = PRECALCULATED_HEAD;
            values[1] = object; // pass X509CertPathImpl object
            values[2] = SIGNERS_INFO;
        }

        // stub to prevent using the instance as decoder
        public Object decode(BerInputStream in) throws IOException {
            throw new RuntimeException(
                    "Invalid use of encoder for PKCS#7 SignedData object");
        }
    };

    private static final ASN1Sequence PKCS7_SIGNED_DATA_OBJECT = new ASN1Sequence(
            new ASN1Type[] { ASN1Any.getInstance(), // contentType
                    new ASN1Explicit(0, ASN1_SIGNED_DATA) // SignedData
            }) {

        // precalculated ASN.1 encoding for SignedData object oid
        private final byte[] SIGNED_DATA_OID = ASN1Oid.getInstance().encode(
                ContentInfo.SIGNED_DATA);

        protected void getValues(Object object, Object[] values) {
            values[0] = SIGNED_DATA_OID;
            values[1] = object; // pass X509CertPathImpl object
        }

        // stub to prevent using the instance as decoder
        public Object decode(BerInputStream in) throws IOException {
            throw new RuntimeException(
                    "Invalid use of encoder for PKCS#7 SignedData object");
        }
    };
}
