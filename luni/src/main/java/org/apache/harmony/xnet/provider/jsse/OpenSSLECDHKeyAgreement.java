/*
 * Copyright (C) 2013 The Android Open Source Project
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public final class OpenSSLECDHKeyAgreement extends KeyAgreementSpi {
    private boolean mCompleted;
    private ECPrivateKey mPrivateKey;
    private byte[] mResult;

    @Override
    public Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException {
        if (mPrivateKey == null) {
            throw new IllegalStateException("Missing private key");
        }
        if (!lastPhase) {
            throw new IllegalArgumentException("ECDH only has one phase");
        }
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (!(key instanceof ECPublicKey)) {
            throw new InvalidKeyException("This phase requires an ECPublicKey. Actual key type: "
                + key.getClass());
        }
        ECPublicKey publicKey = (ECPublicKey) key;

        OpenSSLKey openSslPrivateKey;
        if (mPrivateKey instanceof OpenSSLECPrivateKey) {
            // OpenSSL-backed key
            openSslPrivateKey = ((OpenSSLECPrivateKey) mPrivateKey).getOpenSSLKey();
        } else {
            // Not an OpenSSL-backed key -- create an OpenSSL-backed key from its PKCS#8 encoding
            if (!"PKCS#8".equals(mPrivateKey.getFormat())) {
                throw new InvalidKeyException("Non-OpenSSL private key (" + mPrivateKey.getClass()
                    + ") offers unsupported encoding format: " + mPrivateKey.getFormat());
            }
            byte[] encoded = mPrivateKey.getEncoded();
            if (encoded == null) {
                throw new InvalidKeyException("Non-OpenSSL private key (" + mPrivateKey.getClass()
                    + ") does not provide encoded form");
            }
            try {
                openSslPrivateKey = new OpenSSLKey(NativeCrypto.d2i_PKCS8_PRIV_KEY_INFO(encoded));
            } catch (Exception e) {
                throw new InvalidKeyException("Failed to decode PKCS#8 encoded private key", e);
            }
        }

        OpenSSLKey openSslPublicKey;
        if (publicKey instanceof OpenSSLECPublicKey) {
            // OpenSSL-backed key
            openSslPublicKey = ((OpenSSLECPublicKey) publicKey).getOpenSSLKey();
        } else {
            // Not an OpenSSL-backed key -- create an OpenSSL-backed key from its X.509 encoding
            if (!"X.509".equals(publicKey.getFormat())) {
                throw new InvalidKeyException("Non-OpenSSL public key (" + publicKey.getClass()
                    + ") offers unsupported encoding format: " + publicKey.getFormat());
            }
            byte[] encoded = publicKey.getEncoded();
            if (encoded == null) {
                throw new InvalidKeyException("Non-OpenSSL public key (" + publicKey.getClass()
                    + ") does not provide encoded form");
            }
            try {
                openSslPublicKey = new OpenSSLKey(NativeCrypto.d2i_PUBKEY(encoded));
            } catch (Exception e) {
                throw new InvalidKeyException("Failed to decode X.509 encoded public key", e);
            }
        }

        int expectedResultLengthBytes =
                (mPrivateKey.getParams().getCurve().getField().getFieldSize() + 7) / 8;
        byte[] result = new byte[expectedResultLengthBytes];
        int actualResultLengthBytes = NativeCrypto.ECDH_compute_key(
                result,
                0,
                openSslPublicKey.getPkeyContext(),
                openSslPrivateKey.getPkeyContext());
        if (actualResultLengthBytes == -1) {
            throw new RuntimeException("Engine returned " + actualResultLengthBytes);
        } else if (actualResultLengthBytes == expectedResultLengthBytes) {
            // The output is as long as expected -- use the whole buffer
            mResult = result;
        } else if (actualResultLengthBytes < expectedResultLengthBytes) {
            // The output is shorter than expected -- use only what's produced by the engine
            mResult = new byte[actualResultLengthBytes];
            System.arraycopy(result, 0, mResult, 0, mResult.length);
        } else {
            // The output is longer than expected
            throw new RuntimeException("Engine produced a longer than expected result. Expected: "
                + expectedResultLengthBytes + ", actual: " + actualResultLengthBytes);
        }

        mCompleted = true;
        return null; // No intermediate key
    }

    @Override
    protected int engineGenerateSecret(byte[] sharedSecret, int offset)
            throws ShortBufferException {
        checkCompleted();
        int available = sharedSecret.length - offset;
        if (mResult.length > available) {
            throw new ShortBufferException(
                    "Needed: " + mResult.length + ", available: " + available);
        }
        System.arraycopy(mResult, 0, sharedSecret, offset, mResult.length);
        return mResult.length;
    }

    @Override
    protected byte[] engineGenerateSecret() {
        checkCompleted();
        return mResult;
    }

    @Override
    protected SecretKey engineGenerateSecret(String algorithm) {
        checkCompleted();
        return new SecretKeySpec(engineGenerateSecret(), algorithm);
    }

    @Override
    protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (!(key instanceof ECPrivateKey)) {
            throw new InvalidKeyException("Not an EC private key: " + key.getClass());
        }
        mPrivateKey = (ECPrivateKey) key;
        mCompleted = false;
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        // ECDH doesn't need an AlgorithmParameterSpec
        if (params != null) {
          throw new InvalidAlgorithmParameterException("No alrogithm parameters supported");
        }
        engineInit(key, random);
    }

    private void checkCompleted() {
      if (!mCompleted) {
        throw new IllegalStateException("Key agreement not completed");
      }
    }
}
