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
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Set;

public class OpenSSLX509Certificate extends X509Certificate {
    private int ctx;

    private OpenSSLX509Certificate(int ctx) {
        this.ctx = ctx;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException,
            CertificateNotYetValidException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BigInteger getSerialNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Principal getIssuerDN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Principal getSubjectDN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getNotBefore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getNotAfter() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSigAlgOID() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getBasicConstraints() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        // TODO Auto-generated method stub
        return null;
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
            NativeCrypto.X509_print_ex(bioCtx, ctx, 0, 0);
            return os.toString();
        } finally {
            NativeCrypto.BIO_free(bioCtx);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        /* First try to generate the key from supported OpenSSL key types. */
        try {
            OpenSSLKey pkey = new OpenSSLKey(NativeCrypto.X509_get_pubkey(ctx));
            return pkey.getPublicKey();
        } catch (NoSuchAlgorithmException ignored) {
        }

        /* Try generating the key using other Java providers. */
        String oid = NativeCrypto.get_X509_pubkey_oid(ctx);
        byte[] encoded = NativeCrypto.i2d_X509_PUBKEY(ctx);
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
}
