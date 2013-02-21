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

import static libcore.java.security.SignatureTest.hexToBytes;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import junit.framework.TestCase;

public class OpenSSLECDHKeyAgreementTest extends TestCase {
    private static final OpenSSLProvider sProvider = new OpenSSLProvider();

    // Two key pairs and the resulting shared secret for the Known Answer Test
    private static final ECPublicKey KAT_PUBLIC_KEY1 = getECPublicKeyFromX509Bytes(hexToBytes(
            "3059301306072a8648ce3d020106082a8648ce3d030107034200049fc2f71f85446b1371244491d83"
            + "9cf97b5d27cedbb04d2c0058b59709df3a216e6b4ca1b2d622588c5a0e6968144a8965e816a600c"
            + "05305a1da3df2bf02b41d1"));
    private static final ECPrivateKey KAT_PRIVATE_KEY1 = getECPrivateKeyFromPkcs8Bytes(hexToBytes(
            "308193020100301306072a8648ce3d020106082a8648ce3d030107047930770201010420e1e683003"
            + "c8b963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271ca00a06082a8648ce3d030107"
            + "a144034200049fc2f71f85446b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216e6b"
            + "4ca1b2d622588c5a0e6968144a8965e816a600c05305a1da3df2bf02b41d1"));

    private static final ECPublicKey KAT_PUBLIC_KEY2 = getECPublicKeyFromX509Bytes(hexToBytes(
            "3059301306072a8648ce3d020106082a8648ce3d03010703420004358efb6d91e5bbcae21774af3f6"
            + "d85d0848630e7e61dbeb5ac9e47036ed0f8d38c7a1d1bb249f92861c7c9153fff33f45ab5b171eb"
            + "e8cad741125e6bb4fc6b07"));
    private static final ECPrivateKey KAT_PRIVATE_KEY2 = getECPrivateKeyFromPkcs8Bytes(hexToBytes(
            "308193020100301306072a8648ce3d020106082a8648ce3d0301070479307702010104202b1810a69"
            + "e12b74d50bf0343168f705f0104f76299855268aa526fdb31e6eec0a00a06082a8648ce3d030107"
            + "a14403420004358efb6d91e5bbcae21774af3f6d85d0848630e7e61dbeb5ac9e47036ed0f8d38c7"
            + "a1d1bb249f92861c7c9153fff33f45ab5b171ebe8cad741125e6bb4fc6b07"));

    private static final byte[] KAT_SECRET =
            hexToBytes("4faa0594c0e773eb26c8df2163af2443e88aab9578b9e1f324bc61e42d222783");


    public void testKnownAnswer_withOpenSSLKeys() throws Exception {
        assertTrue(Arrays.equals(
                KAT_SECRET, generateSecret(KAT_PRIVATE_KEY1, KAT_PUBLIC_KEY2)));
        assertTrue(Arrays.equals(
                KAT_SECRET, generateSecret(KAT_PRIVATE_KEY2, KAT_PUBLIC_KEY1)));
    }

    public void testKnownAnswer_withNonOpenSSLKeys() throws Exception {
        assertTrue(Arrays.equals(
                KAT_SECRET,
                generateSecret(
                        new DelegatingECPrivateKey(KAT_PRIVATE_KEY1),
                        new DelegatingECPublicKey(KAT_PUBLIC_KEY2))));
        assertTrue(Arrays.equals(
                KAT_SECRET,
                generateSecret(
                        new DelegatingECPrivateKey(KAT_PRIVATE_KEY2),
                        new DelegatingECPublicKey(KAT_PUBLIC_KEY1))));
    }

    public void testGetAlgorithm() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        assertEquals("ECDH", keyAgreement.getAlgorithm());
    }

    public void testGetProvider() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        assertEquals(sProvider, keyAgreement.getProvider());
    }

    public void testInit_withNullPrivateKey() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        try {
            keyAgreement.init(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void testInit_withUnsupportedPrivateKeyType() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        try {
            keyAgreement.init(KAT_PUBLIC_KEY1);
            fail();
        } catch (InvalidKeyException expected) {}
    }

    public void testInit_withUnsupportedAlgorithmParameterSpec() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        try {
            keyAgreement.init(KAT_PRIVATE_KEY1, new ECGenParameterSpec("prime256v1"));
            fail();
        } catch (InvalidAlgorithmParameterException expected) {}
    }

    public void testDoPhase_whenNotInitialized() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        try {
            keyAgreement.doPhase(KAT_PUBLIC_KEY1, true);
            fail();
        } catch (IllegalStateException expected) {}
    }

    public void testDoPhaseReturnsNull() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY2);
        assertNull(keyAgreement.doPhase(KAT_PUBLIC_KEY1, true));
    }

    public void testDoPhase_withPhaseWhichIsNotLast() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY2);
        try {
            keyAgreement.doPhase(KAT_PUBLIC_KEY1, false);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void testDoPhase_withNullKey() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY1);
        try {
            keyAgreement.doPhase(null, true);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void testDoPhase_withInvalidKeyType() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY1);
        try {
            keyAgreement.doPhase(KAT_PRIVATE_KEY1, true);
            fail();
        } catch (InvalidKeyException expected) {}
    }

    public void testGenerateSecret_withNullOutputBuffer() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY1);
        keyAgreement.doPhase(KAT_PUBLIC_KEY2, true);
        try {
            keyAgreement.generateSecret(null, 0);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void testGenerateSecret_withBufferOfTheRightSize() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY1);
        keyAgreement.doPhase(KAT_PUBLIC_KEY2, true);

        byte[] buffer = new byte[KAT_SECRET.length];
        int secretLengthBytes = keyAgreement.generateSecret(buffer, 0);
        assertEquals(KAT_SECRET.length, secretLengthBytes);
        assertTrue(Arrays.equals(KAT_SECRET, buffer));
    }

    public void testGenerateSecret_withLargerThatNeededBuffer() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY1);
        keyAgreement.doPhase(KAT_PUBLIC_KEY2, true);

        // Place the shared secret in the middle of the larger buffer and check that only that
        // part of the buffer is affected.
        byte[] buffer = new byte[KAT_SECRET.length + 2];
        buffer[0] = (byte) 0x85; // arbitrary canary value
        buffer[buffer.length - 1] = (byte) 0x3b; // arbitrary canary value
        int secretLengthBytes = keyAgreement.generateSecret(buffer, 1);
        assertEquals(KAT_SECRET.length, secretLengthBytes);
        assertEquals((byte) 0x85, buffer[0]);
        assertEquals((byte) 0x3b, buffer[buffer.length - 1]);
        byte[] secret = new byte[KAT_SECRET.length];
        System.arraycopy(buffer, 1, secret, 0, secret.length);
        assertTrue(Arrays.equals(KAT_SECRET, secret));
    }

    public void testGenerateSecret_withSmallerThanNeededBuffer() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY1);
        keyAgreement.doPhase(KAT_PUBLIC_KEY2, true);
        try {
            // Although the buffer is big enough (1024 bytes) the shared secret should be placed
            // at offset 1020 thus leaving only 4 bytes for the secret, which is not enough.
            keyAgreement.generateSecret(new byte[1024], 1020);
            fail();
        } catch (ShortBufferException expected) {}
    }

    public void testGenerateSecret_withoutBuffer() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY2);
        keyAgreement.doPhase(KAT_PUBLIC_KEY1, true);

        byte[] secret = keyAgreement.generateSecret();
        assertTrue(Arrays.equals(KAT_SECRET, secret));
    }

    public void testGenerateSecret_withAlgorithm() throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(KAT_PRIVATE_KEY2);
        keyAgreement.doPhase(KAT_PUBLIC_KEY1, true);

        SecretKey key = keyAgreement.generateSecret("AES");
        assertEquals("AES", key.getAlgorithm());
        // The check below will need to change if it's a hardware-backed key.
        // We'll have to encrypt a known plaintext and check that the ciphertext is as expected.
        assertTrue(Arrays.equals(KAT_SECRET, key.getEncoded()));
    }

    private static KeyAgreement getKeyAgreement() throws NoSuchAlgorithmException {
        return KeyAgreement.getInstance("ECDH", sProvider);
    }

    private static ECPublicKey getECPublicKeyFromX509Bytes(byte[] encoded) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode ECPublicKey from X.509 bytes", e);
        }
    }

    private static ECPrivateKey getECPrivateKeyFromPkcs8Bytes(byte[] encoded) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode ECPrivateKey from PKCS#8 bytes", e);
        }
    }

    private static byte[] generateSecret(PrivateKey privateKey, PublicKey publicKey)
            throws Exception {
        KeyAgreement keyAgreement = getKeyAgreement();
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }


    private static class DelegatingECPublicKey implements ECPublicKey {
        private final ECPublicKey mDelegate;

        private DelegatingECPublicKey(ECPublicKey delegate) {
            mDelegate = delegate;
        }

        @Override
        public String getAlgorithm() {
            return mDelegate.getAlgorithm();
        }

        @Override
        public byte[] getEncoded() {
            return mDelegate.getEncoded();
        }

        @Override
        public String getFormat() {
            return mDelegate.getFormat();
        }

        @Override
        public ECParameterSpec getParams() {
            return mDelegate.getParams();
        }

        @Override
        public ECPoint getW() {
            return mDelegate.getW();
        }
    }

    private static class DelegatingECPrivateKey implements ECPrivateKey {
        private final ECPrivateKey mDelegate;

        private DelegatingECPrivateKey(ECPrivateKey delegate) {
            mDelegate = delegate;
        }

        @Override
        public String getAlgorithm() {
            return mDelegate.getAlgorithm();
        }

        @Override
        public byte[] getEncoded() {
            return mDelegate.getEncoded();
        }

        @Override
        public String getFormat() {
            return mDelegate.getFormat();
        }

        @Override
        public ECParameterSpec getParams() {
            return mDelegate.getParams();
        }

        @Override
        public BigInteger getS() {
            return mDelegate.getS();
        }
    }
}
