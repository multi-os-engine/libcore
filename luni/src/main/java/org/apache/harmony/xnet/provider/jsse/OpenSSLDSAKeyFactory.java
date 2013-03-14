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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class OpenSSLDSAKeyFactory extends KeyFactorySpi {

    @Override
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        }

        if (keySpec instanceof DSAPublicKeySpec) {
            DSAPublicKeySpec dsaKeySpec = (DSAPublicKeySpec) keySpec;

            return new OpenSSLDSAPublicKey(dsaKeySpec);
        } else if (keySpec instanceof X509EncodedKeySpec) {
            X509EncodedKeySpec x509KeySpec = (X509EncodedKeySpec) keySpec;

            try {
                final OpenSSLKey key = new OpenSSLKey(
                        NativeCrypto.d2i_PUBKEY(x509KeySpec.getEncoded()));
                return new OpenSSLDSAPublicKey(key);
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        }
        throw new InvalidKeySpecException("Must use DSAPublicKeySpec or X509EncodedKeySpec; was "
                + keySpec.getClass().getName());
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        }

        if (keySpec instanceof DSAPrivateKeySpec) {
            DSAPrivateKeySpec dsaKeySpec = (DSAPrivateKeySpec) keySpec;

            return new OpenSSLDSAPrivateKey(dsaKeySpec);
        } else if (keySpec instanceof PKCS8EncodedKeySpec) {
            PKCS8EncodedKeySpec pkcs8KeySpec = (PKCS8EncodedKeySpec) keySpec;

            try {
                final OpenSSLKey key = new OpenSSLKey(
                        NativeCrypto.d2i_PKCS8_PRIV_KEY_INFO(pkcs8KeySpec.getEncoded()));
                return new OpenSSLDSAPrivateKey(key);
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        }
        throw new InvalidKeySpecException("Must use DSAPrivateKeySpec or PKCS8EncodedKeySpec; was "
                + keySpec.getClass().getName());
    }

    @Override
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        if (key == null) {
            throw new InvalidKeySpecException("key == null");
        }

        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        }

        if (key instanceof DSAPublicKey && DSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
            DSAPublicKey dsaKey = (DSAPublicKey) key;
            DSAParams params = dsaKey.getParams();
            return (T) new DSAPublicKeySpec(dsaKey.getY(), params.getP(), params.getQ(),
                    params.getG());
        } else if (key instanceof PublicKey && DSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
            final byte[] encoded = key.getEncoded();
            if (!"X.509".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid X.509 encoding");
            }
            DSAPublicKey dsaKey =
                    (DSAPublicKey) engineGeneratePublic(new X509EncodedKeySpec(encoded));
            DSAParams params = dsaKey.getParams();
            return (T) new DSAPublicKeySpec(dsaKey.getY(), params.getP(), params.getQ(),
                    params.getG());
        } else if (key instanceof DSAPrivateKey
                && DSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            DSAPrivateKey dsaKey = (DSAPrivateKey) key;
            DSAParams params = dsaKey.getParams();
            return (T) new DSAPrivateKeySpec(dsaKey.getX(), params.getP(), params.getQ(),
                    params.getG());
        } else if (key instanceof PrivateKey && DSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            final byte[] encoded = key.getEncoded();
            if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
            }
            DSAPrivateKey dsaKey =
                    (DSAPrivateKey) engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
            DSAParams params = dsaKey.getParams();
            return (T) new DSAPrivateKeySpec(dsaKey.getX(), params.getP(), params.getQ(),
                    params.getG());
        } else if (key instanceof PrivateKey && PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
            final byte[] encoded = key.getEncoded();
            if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
            }
            return (T) new PKCS8EncodedKeySpec(encoded);
        } else if (key instanceof PublicKey && X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
            final byte[] encoded = key.getEncoded();
            if (!"X.509".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid X.509 encoding");
            }
            return (T) new X509EncodedKeySpec(encoded);
        } else {
            throw new InvalidKeySpecException("Unknown key type and key spec combination; key="
                    + key.getClass().getName() + ", keySpec=" + keySpec.getName());
        }
    }

    @Override
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key == null");
        }

        if (key instanceof DSAPublicKey) {
            DSAPublicKey dsaKey = (DSAPublicKey) key;

            BigInteger y = dsaKey.getY();

            DSAParams params = dsaKey.getParams();
            BigInteger p = params.getP();
            BigInteger q = params.getQ();
            BigInteger g = params.getG();

            try {
                return engineGeneratePublic(new DSAPublicKeySpec(y, p, q, g));
            } catch (InvalidKeySpecException e) {
                throw new InvalidKeyException(e);
            }
        } else if (key instanceof DSAPrivateKey) {
            DSAPrivateKey dsaKey = (DSAPrivateKey) key;

            BigInteger x = dsaKey.getX();

            DSAParams params = dsaKey.getParams();
            BigInteger p = params.getP();
            BigInteger q = params.getQ();
            BigInteger g = params.getG();

            try {
                return engineGeneratePrivate(new DSAPrivateKeySpec(x, p, q, g));
            } catch (InvalidKeySpecException e) {
                throw new InvalidKeyException(e);
            }
        } else if ("PKCS#8".equals(key.getFormat())) {
            try {
                return engineGeneratePrivate(new PKCS8EncodedKeySpec(key.getEncoded()));
            } catch (InvalidKeySpecException e) {
                throw new InvalidKeyException(e);
            }
        } else if ("X.509".equals(key.getFormat())) {
            try {
                return engineGeneratePublic(new X509EncodedKeySpec(key.getEncoded()));
            } catch (InvalidKeySpecException e) {
                throw new InvalidKeyException(e);
            }
        } else {
            throw new InvalidKeyException("Key must be DSA public or private key; was "
                    + key.getClass().getName());
        }
    }
}
