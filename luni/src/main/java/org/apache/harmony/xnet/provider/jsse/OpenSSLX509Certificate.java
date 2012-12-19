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

import org.apache.harmony.security.utils.AlgNameMapper;
import org.apache.harmony.security.x509.X509PublicKey;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.security.auth.x500.X500Principal;

public class OpenSSLX509Certificate extends X509Certificate {
    private final int mContext;

    private OpenSSLX509Certificate(int ctx) {
        mContext = ctx;
    }

    public static OpenSSLX509Certificate fromDerInputStream(InputStream is)
            throws CertificateEncodingException {
        int bioCtx = NativeCrypto.create_BIO_InputStream(new OpenSSLBIOInputStream(is));
        try {
            return new OpenSSLX509Certificate(NativeCrypto.d2i_X509_bio(bioCtx));
        } catch (Exception e) {
            throw new CertificateEncodingException(e);
        } finally {
            NativeCrypto.BIO_free(bioCtx);
        }
    }

    public static Certificate fromPemInputStream(InputStream is)
            throws CertificateEncodingException {
        int bioCtx = NativeCrypto.create_BIO_InputStream(new OpenSSLBIOInputStream(is));
        try {
            return new OpenSSLX509Certificate(NativeCrypto.PEM_read_bio_X509_AUX(bioCtx));
        } catch (Exception e) {
            throw new CertificateEncodingException(e);
        } finally {
            NativeCrypto.BIO_free(bioCtx);
        }
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return new HashSet<String>(Arrays.asList(NativeCrypto.get_X509_ext_oids(mContext, 1)));
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        return NativeCrypto.X509_get_ext_oid(mContext, oid);
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return new HashSet<String>(Arrays.asList(NativeCrypto.get_X509_ext_oids(mContext, 0)));
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        int ret = NativeCrypto.X509_cmp_current_time(NativeCrypto.X509_get_notBefore(mContext));
        if (ret != -1) {
            throw new CertificateNotYetValidException();
        }

        ret = NativeCrypto.X509_cmp_current_time(NativeCrypto.X509_get_notAfter(mContext));
        if (ret != 1) {
            throw new CertificateExpiredException();
        }
    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException,
            CertificateNotYetValidException {
        long timeMillis = date.getTime();

        int ret = NativeCrypto.X509_cmp_time(NativeCrypto.X509_get_notBefore(mContext), timeMillis);
        if (ret != -1) {
            throw new CertificateNotYetValidException();
        }

        ret = NativeCrypto.X509_cmp_time(NativeCrypto.X509_get_notAfter(mContext), timeMillis);
        if (ret != 1) {
            throw new CertificateExpiredException();
        }
    }

    @Override
    public int getVersion() {
        return (int) NativeCrypto.X509_get_version(mContext) + 1;
    }

    @Override
    public BigInteger getSerialNumber() {
        return new BigInteger(NativeCrypto.X509_get_serialNumber(mContext));
    }

    @Override
    public Principal getIssuerDN() {
        return getIssuerX500Principal();
    }

    @Override
    public Principal getSubjectDN() {
        return getSubjectX500Principal();
    }

    @Override
    public Date getNotBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        NativeCrypto.ASN1_TIME_to_Calendar(NativeCrypto.X509_get_notBefore(mContext), calendar);
        return calendar.getTime();
    }

    @Override
    public Date getNotAfter() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        NativeCrypto.ASN1_TIME_to_Calendar(NativeCrypto.X509_get_notAfter(mContext), calendar);
        return calendar.getTime();
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getSignature() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSigAlgName() {
        return AlgNameMapper.map2AlgName(getSigAlgOID());
    }

    @Override
    public String getSigAlgOID() {
        return NativeCrypto.get_X509_sig_alg_oid(mContext);
    }

    @Override
    public byte[] getSigAlgParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean[] getKeyUsage() {
        return NativeCrypto.get_X509_ex_kusage(mContext);
    }

    @Override
    public int getBasicConstraints() {
        if (NativeCrypto.X509_check_ca(mContext) != 1) {
           return -1;
        }

        final int pathLen = NativeCrypto.get_X509_ex_pathlen(mContext);
        if (pathLen == -1) {
            return Integer.MAX_VALUE;
        }

        return pathLen;
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return NativeCrypto.i2d_X509(mContext);
    }

    @Override
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException {
        // TODO Auto-generated method stub

    }

    @Override
    public void verify(PublicKey key, String sigProvider) throws CertificateException,
            NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
            SignatureException {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int bioCtx = NativeCrypto.create_BIO_OutputStream(os);
        try {
            NativeCrypto.X509_print_ex(bioCtx, mContext, 0, 0);
            return os.toString();
        } finally {
            NativeCrypto.BIO_free(bioCtx);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        /* First try to generate the key from supported OpenSSL key types. */
        try {
            OpenSSLKey pkey = new OpenSSLKey(NativeCrypto.X509_get_pubkey(mContext));
            return pkey.getPublicKey();
        } catch (NoSuchAlgorithmException ignored) {
        }

        /* Try generating the key using other Java providers. */
        String oid = NativeCrypto.get_X509_pubkey_oid(mContext);
        byte[] encoded = NativeCrypto.i2d_X509_PUBKEY(mContext);
        try {
            KeyFactory kf = KeyFactory.getInstance(oid);
            return kf.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (NoSuchAlgorithmException ignored) {
        } catch (InvalidKeySpecException ignored) {
        }

        /*
         * We couldn't find anything else, so just return a nearly-unusable
         * X.509-encoded key.
         */
        return new X509PublicKey(oid, encoded, null);
    }

    @Override
    public X500Principal getIssuerX500Principal() {
        final byte[] issuer = NativeCrypto.X509_get_issuer_name(mContext);
        return new X500Principal(issuer);
    }

    @Override
    public X500Principal getSubjectX500Principal() {
        final byte[] subject = NativeCrypto.X509_get_subject_name(mContext);
        return new X500Principal(subject);
    }

    @Override
    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        String[] extUsage = NativeCrypto.get_X509_ex_xkusage(mContext);
        if (extUsage == null) {
            return null;
        }

        return Arrays.asList(extUsage);
    }

    @Override
    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        Object[][] altNameArray = NativeCrypto.get_X509_GENERAL_NAME_stack(mContext, 1);
        if (altNameArray == null) {
            return null;
        }

        Collection<List<?>> coll = new ArrayList<List<?>>(altNameArray.length);
        for (int i = 0; i < altNameArray.length; i++) {
            coll.add(Arrays.asList(altNameArray[i]));
        }

        return coll;
    }

    @Override
    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        // TODO Auto-generated method stub
        return super.getIssuerAlternativeNames();
    }

    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }
}
