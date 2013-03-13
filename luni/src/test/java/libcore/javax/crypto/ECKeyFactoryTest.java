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

package libcore.javax.crypto;

import static libcore.java.security.SignatureTest.hexToBytes;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Tests for all registered Elliptic Curve {@link KeyFactory} providers.
 */
public class ECKeyFactoryTest extends TestCase {

    private static final byte[] PUBLIC_KEY_X509_WITH_NAMED_CURVE_SPEC = hexToBytes(
            "3059301306072a8648ce3d020106082a8648ce3d030107034200049fc2f71f85446b1371244491d83"
            + "9cf97b5d27cedbb04d2c0058b59709df3a216e6b4ca1b2d622588c5a0e6968144a8965e816a600c"
            + "05305a1da3df2bf02b41d1");
    private static final byte[] PUBLIC_KEY_X509_WITH_UNNAMED_CURVE_SPEC = hexToBytes(
            "3082014b3082010306072a8648ce3d02013081f7020101302c06072a8648ce3d0101022100fffffff"
            + "f00000001000000000000000000000000ffffffffffffffffffffffff305b0420ffffffff000000"
            + "01000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd557"
            + "69886bc651d06b0cc53b0f63bce3c3e27d2604b031500c49d360886e704936a6678e1139d26b781"
            + "9f7e900441046b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c2964fe"
            + "342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5022100ffffffff0000"
            + "0000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551020101034200049fc2f71f85446"
            + "b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216e6b4ca1b2d622588c5a0e6968144"
            + "a8965e816a600c05305a1da3df2bf02b41d1");
    private static final byte[] PUBLIC_KEY_X509_WITH_UNNAMED_CURVE_SPEC2 = hexToBytes(
            "308201333081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100ffffffff0"
            + "0000001000000000000000000000000ffffffffffffffffffffffff30440420ffffffff00000001"
            + "000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd55769"
            + "886bc651d06b0cc53b0f63bce3c3e27d2604b0441046b17d1f2e12c4247f8bce6e563a440f27703"
            + "7d812deb33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb"
            + "6406837bf51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc63"
            + "2551020101034200049fc2f71f85446b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a"
            + "216e6b4ca1b2d622588c5a0e6968144a8965e816a600c05305a1da3df2bf02b41d1");

    private static final byte[] PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC = hexToBytes(
            "308193020100301306072a8648ce3d020106082a8648ce3d030107047930770201010420e1e683003"
            + "c8b963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271ca00a06082a8648ce3d030107"
            + "a144034200049fc2f71f85446b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216e6b"
            + "4ca1b2d622588c5a0e6968144a8965e816a600c05305a1da3df2bf02b41d1");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC2 = hexToBytes(
            "3041020100301306072a8648ce3d020106082a8648ce3d030107042730250201010420e1e683003c8b"
            + "963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271c");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC3 = hexToBytes(
            "304d020100301306072a8648ce3d020106082a8648ce3d030107043330310201010420e1e683003c8b"
            + "963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271ca00a06082a8648ce3d030107");

    private static final byte[] PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC4 = hexToBytes(
            "308187020100301306072a8648ce3d020106082a8648ce3d030107046d306b0201010420e1e683003c"
            + "8b963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271ca144034200049fc2f71f85446b"
            + "1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216e6b4ca1b2d622588c5a0e6968144a8"
            + "965e816a600c05305a1da3df2bf02b41d1");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC = hexToBytes(
            "308201610201003081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100ffff"
            + "ffff00000001000000000000000000000000ffffffffffffffffffffffff30440420ffffffff0000"
            + "0001000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd55"
            + "769886bc651d06b0cc53b0f63bce3c3e27d2604b0441046b17d1f2e12c4247f8bce6e563a440f277"
            + "037d812deb33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececb"
            + "b6406837bf51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc63"
            + "2551020101046d306b0201010420e1e683003c8b963a92742e5f955ce7fddc81d0c3ae9b149d6af8"
            + "6a0cacb2271ca144034200049fc2f71f85446b1371244491d839cf97b5d27cedbb04d2c0058b5970"
            + "9df3a216e6b4ca1b2d622588c5a0e6968144a8965e816a600c05305a1da3df2bf02b41d1");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC2 = hexToBytes(
            "308202340201003082010306072a8648ce3d02013081f7020101302c06072a8648ce3d0101022100ff"
            + "ffffff00000001000000000000000000000000ffffffffffffffffffffffff305b0420ffffffff00"
            + "000001000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd"
            + "55769886bc651d06b0cc53b0f63bce3c3e27d2604b031500c49d360886e704936a6678e1139d26b7"
            + "819f7e900441046b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c2964f"
            + "e342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5022100ffffffff0000"
            + "0000ffffffffffffffffbce6faada7179e84f3b9cac2fc6325510201010482012630820122020101"
            + "0420e1e683003c8b963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271ca081fa3081f7"
            + "020101302c06072a8648ce3d0101022100ffffffff00000001000000000000000000000000ffffff"
            + "ffffffffffffffffff305b0420ffffffff00000001000000000000000000000000ffffffffffffff"
            + "fffffffffc04205ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b03"
            + "1500c49d360886e704936a6678e1139d26b7819f7e900441046b17d1f2e12c4247f8bce6e563a440"
            + "f277037d812deb33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315e"
            + "cecbb6406837bf51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2"
            + "fc632551020101");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC3 = hexToBytes(
            "308201790201003082010306072a8648ce3d02013081f7020101302c06072a8648ce3d0101022100ff"
            + "ffffff00000001000000000000000000000000ffffffffffffffffffffffff305b0420ffffffff00"
            + "000001000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd"
            + "55769886bc651d06b0cc53b0f63bce3c3e27d2604b031500c49d360886e704936a6678e1139d26b7"
            + "819f7e900441046b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c2964f"
            + "e342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5022100ffffffff0000"
            + "0000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551020101046d306b0201010420e1e6"
            + "83003c8b963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271ca144034200049fc2f71f"
            + "85446b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216e6b4ca1b2d622588c5a0e696"
            + "8144a8965e816a600c05305a1da3df2bf02b41d1");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC4 = hexToBytes(
            "3082024b0201003081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100ffff"
            + "ffff00000001000000000000000000000000ffffffffffffffffffffffff30440420ffffffff0000"
            + "0001000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd55"
            + "769886bc651d06b0cc53b0f63bce3c3e27d2604b0441046b17d1f2e12c4247f8bce6e563a440f277"
            + "037d812deb33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececb"
            + "b6406837bf51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc63"
            + "255102010104820155308201510201010420e1e683003c8b963a92742e5f955ce7fddc81d0c3ae9b"
            + "149d6af86a0cacb2271ca081e33081e0020101302c06072a8648ce3d0101022100ffffffff000000"
            + "01000000000000000000000000ffffffffffffffffffffffff30440420ffffffff00000001000000"
            + "000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd55769886bc65"
            + "1d06b0cc53b0f63bce3c3e27d2604b0441046b17d1f2e12c4247f8bce6e563a440f277037d812deb"
            + "33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf"
            + "51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551020101"
            + "a144034200049fc2f71f85446b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216e6b4"
            + "ca1b2d622588c5a0e6968144a8965e816a600c05305a1da3df2bf02b41d1");
    private static final byte[] PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC5 = hexToBytes(
            "308202050201003081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100ffff"
            + "ffff00000001000000000000000000000000ffffffffffffffffffffffff30440420ffffffff0000"
            + "0001000000000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd55"
            + "769886bc651d06b0cc53b0f63bce3c3e27d2604b0441046b17d1f2e12c4247f8bce6e563a440f277"
            + "037d812deb33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececb"
            + "b6406837bf51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc63"
            + "25510201010482010f3082010b0201010420e1e683003c8b963a92742e5f955ce7fddc81d0c3ae9b"
            + "149d6af86a0cacb2271ca081e33081e0020101302c06072a8648ce3d0101022100ffffffff000000"
            + "01000000000000000000000000ffffffffffffffffffffffff30440420ffffffff00000001000000"
            + "000000000000000000fffffffffffffffffffffffc04205ac635d8aa3a93e7b3ebbd55769886bc65"
            + "1d06b0cc53b0f63bce3c3e27d2604b0441046b17d1f2e12c4247f8bce6e563a440f277037d812deb"
            + "33a0f4a13945d898c2964fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf"
            + "51f5022100ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551020101");

    private static final byte[] PUBLIC_KEY_X509 = PUBLIC_KEY_X509_WITH_NAMED_CURVE_SPEC;
    private static final byte[] PRIVATE_KEY_PKCS8 = PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC;

    private static final BigInteger PUBLIC_KEY_X =
            new BigInteger("9fc2f71f85446b1371244491d839cf97b5d27cedbb04d2c0058b59709df3a216", 16);
    private static final BigInteger PUBLIC_KEY_Y =
            new BigInteger("e6b4ca1b2d622588c5a0e6968144a8965e816a600c05305a1da3df2bf02b41d1", 16);
    private static final BigInteger PRIVATE_KEY_S =
            new BigInteger("e1e683003c8b963a92742e5f955ce7fddc81d0c3ae9b149d6af86a0cacb2271c", 16);

    private static final PublicKey OPAQUE_PUBLIC_KEY =
            new OpaquePublicKey("EC", PUBLIC_KEY_X509, "X.509");
    private static final PrivateKey OPAQUE_PRIVATE_KEY =
            new OpaquePrivateKey("EC", PRIVATE_KEY_PKCS8, "PKCS#8");
    private static final ECParameterSpec NIST_P_256_PARAMETER_SPEC = getNistP256CurveParameters();

    public void testGetProvider() throws Exception {
        invokeForEachKeyFactoryProvider(new ProviderRunnable() {
            @Override
            public void run(Provider provider) throws Exception {
                assertSame(provider, getKeyFactory(provider).getProvider());
            }
        });
    }

    public void testGetAlgorithm() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                assertEquals("EC", keyFactory.getAlgorithm());
            }
        });
    }

    public void testGeneratePrivate_withPkcs8EncodedKeySpecWithNamedCurve() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC2)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC3)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC4)));
            }
        });
    }

    public void testGeneratePrivate_withPkcs8EncodedKeySpecWithUnnamedCurve() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC2)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC3)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC4)));
                assertHardcodedPrivateKey(keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC5)));
            }
        });
    }

    public void testGeneratePrivate_withECPrivateKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                ECPrivateKeySpec spec = new ECPrivateKeySpec(
                        PRIVATE_KEY_S,
                        NIST_P_256_PARAMETER_SPEC);
                assertHardcodedPrivateKey(keyFactory.generatePrivate(spec));
            }
        });
    }

    public void testGeneratePrivate_withNullKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.generatePrivate(null);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGeneratePrivate_withUnsupportedKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.generatePrivate(new X509EncodedKeySpec(PUBLIC_KEY_X509));
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGeneratePrivate_withInvalidPKCS8EncodedKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.generatePrivate(new PKCS8EncodedKeySpec(PUBLIC_KEY_X509));
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGeneratePublic_withX509EncodedKeySpecWithNamedCurve() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                assertHardcodedPublicKey(keyFactory.generatePublic(
                        new X509EncodedKeySpec(PUBLIC_KEY_X509_WITH_NAMED_CURVE_SPEC)));
            }
        });
    }

    public void testGeneratePublic_withX509EncodedKeySpecWithUnnamedCurve() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                assertHardcodedPublicKey(keyFactory.generatePublic(
                        new X509EncodedKeySpec(PUBLIC_KEY_X509_WITH_UNNAMED_CURVE_SPEC)));
                assertHardcodedPublicKey(keyFactory.generatePublic(
                        new X509EncodedKeySpec(PUBLIC_KEY_X509_WITH_UNNAMED_CURVE_SPEC2)));
            }
        });
    }

    public void testGeneratePublic_withECPublicKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                ECPublicKeySpec spec = new ECPublicKeySpec(
                        new ECPoint(PUBLIC_KEY_X, PUBLIC_KEY_Y),
                        NIST_P_256_PARAMETER_SPEC);
                assertHardcodedPublicKey(keyFactory.generatePublic(spec));
            }
        });
    }

    public void testGeneratePublic_withNullKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.generatePublic(null);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGeneratePublic_withUnsupportedKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.generatePublic(new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8));
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGeneratePublic_withInvalidX509EncodedKeySpec() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.generatePublic(new X509EncodedKeySpec(PRIVATE_KEY_PKCS8));
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGetKeySpec_withNullKey() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.getKeySpec(null, ECPrivateKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGetKeySpec_withPrivateKey() throws Exception {
        invokeForEachKeyFactoryPair(new KeyFactoryPairRunnable() {
            @Override
            public void run(KeyFactory keyFactory1, KeyFactory keyFactory2) throws Exception {
                checkGetKeySpec_withPrivateKey(
                        keyFactory1,
                        keyFactory2.generatePrivate(new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8)));
            }
        });
    }

    public void testGetKeySpec_withOpaquePrivateKey() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                checkGetKeySpec_withPrivateKey(keyFactory, OPAQUE_PRIVATE_KEY);
            }
        });
    }

    private void checkGetKeySpec_withPrivateKey(KeyFactory keyFactory, PrivateKey key)
            throws Exception {
        assertHardcodedECPrivateKeySpec(keyFactory.getKeySpec(key, ECPrivateKeySpec.class));
        assertHardcodedPKCS8EncodedKeySpec(keyFactory.getKeySpec(key, PKCS8EncodedKeySpec.class));

        // X.509 encoded public key spec shouldn't be available for private keys
        try {
            keyFactory.getKeySpec(key, X509EncodedKeySpec.class);
            fail();
        } catch (InvalidKeySpecException expected) {}
    }

    public void testGetKeySpec_withOpaquePrivateKeyWithoutEncodedForm() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                PrivateKey key = new OpaquePrivateKey("EC", null, null);
                try {
                    keyFactory.getKeySpec(key, ECPrivateKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
                try {
                    keyFactory.getKeySpec(key, PKCS8EncodedKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGetKeySpec_withOpaquePrivateKeyWithUnsupportedEncodedForm() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                PrivateKey key = new OpaquePrivateKey("EC", PRIVATE_KEY_PKCS8, "test");
                try {
                    keyFactory.getKeySpec(key, ECPrivateKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
                try {
                    keyFactory.getKeySpec(key, PKCS8EncodedKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}

                key = new OpaquePrivateKey("EC", PUBLIC_KEY_X509, "X.509");
                try {
                    keyFactory.getKeySpec(key, ECPrivateKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
                try {
                    keyFactory.getKeySpec(key, PKCS8EncodedKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGetKeySpec_withPublicKey() throws Exception {
        invokeForEachKeyFactoryPair(new KeyFactoryPairRunnable() {
            @Override
            public void run(KeyFactory keyFactory1, KeyFactory keyFactory2) throws Exception {
                checkGetKeySpec_withPublicKey(
                        keyFactory1,
                        keyFactory2.generatePublic(new X509EncodedKeySpec(PUBLIC_KEY_X509)));
            }
        });
    }

    public void testGetKeySpec_withOpaquePublicKey() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                checkGetKeySpec_withPublicKey(keyFactory, OPAQUE_PUBLIC_KEY);
            }
        });
    }

    private void checkGetKeySpec_withPublicKey(KeyFactory keyFactory, PublicKey key)
            throws Exception {
        assertHardcodedECPublicKeySpec(keyFactory.getKeySpec(key, ECPublicKeySpec.class));
        assertHardcodedX509EncodedKeySpec(keyFactory.getKeySpec(key, X509EncodedKeySpec.class));

        // PKCS#8 encoded private key spec shouldn't be available for public keys
        try {
            keyFactory.getKeySpec(key, PKCS8EncodedKeySpec.class);
            fail();
        } catch (InvalidKeySpecException expected) {}
    }

    public void testGetKeySpec_withOpaquePublicKeyWithoutEncodedForm() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                PublicKey key = new OpaquePublicKey("EC", null, null);
                try {
                    keyFactory.getKeySpec(key, ECPublicKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
                try {
                    keyFactory.getKeySpec(key, X509EncodedKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testGetKeySpec_withOpaquePublicKeyWithUnsupportedEncodedForm() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                PublicKey key = new OpaquePublicKey("EC", PUBLIC_KEY_X509, "test");
                try {
                    keyFactory.getKeySpec(key, ECPublicKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
                try {
                    keyFactory.getKeySpec(key, X509EncodedKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}

                key = new OpaquePublicKey("EC", PRIVATE_KEY_PKCS8, "PKCS#8");
                try {
                    keyFactory.getKeySpec(key, ECPublicKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
                try {
                    keyFactory.getKeySpec(key, X509EncodedKeySpec.class);
                    fail();
                } catch (InvalidKeySpecException expected) {}
            }
        });
    }

    public void testTranslate_withNullKey() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                try {
                    keyFactory.translateKey(null);
                    fail();
                } catch (InvalidKeyException expected) {}
            }
        });
    }

    public void testTranslate_withPrivateKey() throws Exception {
        invokeForEachKeyFactoryPair(new KeyFactoryPairRunnable() {
            @Override
            public void run(KeyFactory keyFactory1, KeyFactory keyFactory2) throws Exception {
                checkTranslate_withPrivateKey(
                        keyFactory1,
                        keyFactory2.generatePrivate(new PKCS8EncodedKeySpec(PRIVATE_KEY_PKCS8)));
            }
        });
    }

    public void testTranslate_withOpaquePrivateKey() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                checkTranslate_withPrivateKey(keyFactory, OPAQUE_PRIVATE_KEY);
            }
        });
    }

    private void checkTranslate_withPrivateKey(KeyFactory keyFactory, PrivateKey inputKey)
            throws Exception {
        assertHardcodedPrivateKey((PrivateKey) keyFactory.translateKey(inputKey));
    }

    public void testTranslatePublicKey() throws Exception {
        invokeForEachKeyFactoryPair(new KeyFactoryPairRunnable() {
            @Override
            public void run(KeyFactory keyFactory1, KeyFactory keyFactory2) throws Exception {
                checkTranslate_withPublicKey(
                        keyFactory1,
                        keyFactory2.generatePublic(new X509EncodedKeySpec(PUBLIC_KEY_X509)));
            }
        });
    }

    public void testTranslatePublicKey_withOpaqueKey() throws Exception {
        invokeForEachKeyFactory(new KeyFactoryRunnable() {
            @Override
            public void run(KeyFactory keyFactory) throws Exception {
                checkTranslate_withPublicKey(keyFactory, OPAQUE_PUBLIC_KEY);
            }
        });
    }

    private void checkTranslate_withPublicKey(KeyFactory keyFactory, PublicKey inputKey)
            throws Exception {
        assertHardcodedPublicKey((PublicKey) keyFactory.translateKey(inputKey));
    }

    private static KeyFactory getKeyFactory(Provider provider) throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("EC", provider);
    }

    private static Provider[] getKeyFactoryProviders() {
        Provider[] providers = Security.getProviders("KeyFactory.EC");
        if (providers == null) {
            return new Provider[0];
        }
        // Sort providers by name to guarantee non-determinism in the order in which providers are
        // used in the tests.
        return sortByName(providers);
    }

    private static Provider[] sortByName(Provider[] providers) {
        Arrays.sort(providers, new Comparator<Provider>() {
            @Override
            public int compare(Provider lhs, Provider rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        return providers;
    }

    private static void assertHardcodedPrivateKey(PrivateKey key) {
        assertEquals("EC", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());
        assertHardcodedPrivateKeyPKCS8Encoding(key.getEncoded());
        assertTrue(key instanceof ECPrivateKey);
        ECPrivateKey privateKey = (ECPrivateKey) key;
        assertEquals(PRIVATE_KEY_S, privateKey.getS());
        assertECParameterSpecEqualsIgnoreSeeds(NIST_P_256_PARAMETER_SPEC, privateKey.getParams());
    }

    private static void assertHardcodedPrivateKeyPKCS8Encoding(byte[] encoded) {
        if ((!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC2, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC3, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_NAMED_CURVE_SPEC4, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC2, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC3, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC4, encoded))
                && (!Arrays.equals(PRIVATE_KEY_PKCS8_WITH_UNNAMED_CURVE_SPEC5, encoded))) {
            fail("Unexpected PKCS#8 encoded form: "
                    + new BigInteger(1, encoded).toString(16));
        }
    }

    private static void assertHardcodedECPrivateKeySpec(ECPrivateKeySpec spec) {
        assertEquals(PRIVATE_KEY_S, spec.getS());
        assertECParameterSpecEqualsIgnoreSeeds(NIST_P_256_PARAMETER_SPEC, spec.getParams());
    }

    private static void assertHardcodedPKCS8EncodedKeySpec(PKCS8EncodedKeySpec spec) {
        assertEquals("PKCS#8", spec.getFormat());
        assertHardcodedPrivateKeyPKCS8Encoding(spec.getEncoded());
    }

    private static void assertHardcodedPublicKey(PublicKey key) {
        assertEquals("EC", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
        assertHardcodedPublicKeyX509Encoding(key.getEncoded());
        assertTrue(key instanceof ECPublicKey);
        ECPublicKey privateKey = (ECPublicKey) key;
        assertEquals(PUBLIC_KEY_X, privateKey.getW().getAffineX());
        assertEquals(PUBLIC_KEY_Y, privateKey.getW().getAffineY());
        assertECParameterSpecEqualsIgnoreSeeds(NIST_P_256_PARAMETER_SPEC, privateKey.getParams());
    }

    private static void assertHardcodedPublicKeyX509Encoding(byte[] encoded) {
        if ((!Arrays.equals(PUBLIC_KEY_X509_WITH_NAMED_CURVE_SPEC, encoded))
                && (!Arrays.equals(PUBLIC_KEY_X509_WITH_UNNAMED_CURVE_SPEC, encoded))
                && (!Arrays.equals(PUBLIC_KEY_X509_WITH_UNNAMED_CURVE_SPEC2, encoded))) {
            fail("Unexpected X.509 encoded form: "
                    + new BigInteger(1, encoded).toString(16));
        }
    }

    private static void assertHardcodedECPublicKeySpec(ECPublicKeySpec spec) {
        assertEquals(PUBLIC_KEY_X, spec.getW().getAffineX());
        assertEquals(PUBLIC_KEY_Y, spec.getW().getAffineY());
        assertECParameterSpecEqualsIgnoreSeeds(NIST_P_256_PARAMETER_SPEC, spec.getParams());
    }

    private static void assertHardcodedX509EncodedKeySpec(X509EncodedKeySpec spec) {
        assertEquals("X.509", spec.getFormat());
        assertHardcodedPublicKeyX509Encoding(spec.getEncoded());
    }

    private static void invokeForEachKeyFactoryProvider(ProviderRunnable runnable)
            throws Exception {
        for (Provider provider : getKeyFactoryProviders()) {
            try {
                runnable.run(provider);
            } catch (Exception e) {
                throw new Exception("provider: " + provider.getName(), e);
            }
        }
    }

    private static void invokeForEachKeyFactory(KeyFactoryRunnable runnable)
            throws Exception {
        for (Provider provider : getKeyFactoryProviders()) {
            try {
                runnable.run(getKeyFactory(provider));
            } catch (Exception e) {
                throw new Exception("provider: " + provider.getName(), e);
            }
        }
    }

    private static void invokeForEachKeyFactoryPair(KeyFactoryPairRunnable runnable)
            throws Exception {
        for (Provider provider1 : getKeyFactoryProviders()) {
            for (Provider provider2 : getKeyFactoryProviders()) {
                try {
                    runnable.run(getKeyFactory(provider1), getKeyFactory(provider2));
                } catch (Exception e) {
                    throw new Exception("provider1: " + provider1.getName()
                            + ", provider2: " + provider2.getName(),
                            e);
                }
            }
        }
    }

    private interface ProviderRunnable {
        void run(Provider provider) throws Exception;
    }

    private interface KeyFactoryRunnable {
        void run(KeyFactory keyFactory) throws Exception;
    }

    private interface KeyFactoryPairRunnable {
        void run(KeyFactory keyFactory1, KeyFactory keyFactory2) throws Exception;
    }

    /**
     * Gets the {@link ECParameterSpec} for the NIST P-256 (aka SECG secp256r1, X9.62 prime256v1)
     * curve.
     */
    private static ECParameterSpec getNistP256CurveParameters() {
        // Based on http://www.nsa.gov/ia/_files/nist-routines.pdf

        // Prime number "p" for the finite field
        BigInteger p = new BigInteger(
                "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16);
        // Curve coefficient "a"
        BigInteger a = new BigInteger(
                "ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", 16);
        // Curve coefficient "b"
        BigInteger b = new BigInteger(
                "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
        // Seed "S" using which the curve was chosen verifiably "at random"
        byte[] s = hexToBytes("c49d360886e704936a6678e1139d26b7819f7e90");
        // Generator "G"
        ECPoint g = new ECPoint(new BigInteger(
                "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296",
                16), new BigInteger(
                "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16));
        // Order "n" of "G"
        BigInteger n = new BigInteger(
                "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16);
        // Cofactor
        int h = 1;

        ECField field = new ECFieldFp(p);
        EllipticCurve curve = new EllipticCurve(field, a, b, s);
        return new ECParameterSpec(curve, g, n, h);
    }

    /**
     * Asserts that the two provided {@link ECParameterSpec} instances are equal while ignoring
     * their seeds. The seeds have no effects on the computations performed using the curves and
     * thus may or may not be set by various implementations.
     */
    private static void assertECParameterSpecEqualsIgnoreSeeds(
            ECParameterSpec params1, ECParameterSpec params2) {
        if (params1 == null) {
            assertNull(params2);
        }
        if (params1.equals(params2)) {
            return;
        }
        assertEquals(params1.getCofactor(), params2.getCofactor());
        assertEquals(params1.getOrder(), params2.getOrder());
        assertEquals(params1.getGenerator(), params2.getGenerator());
        assertEllipticCurveEqualsIgnoreSeeds(params1.getCurve(), params2.getCurve());
    }

    /**
     * Asserts that the two provided {@link EllipticCurve} instances are equal while ignoring their
     * seeds. The seeds have no effects on the computations performed using the curves and thus may
     * or may not be set by various implementations.
     */
    private static void assertEllipticCurveEqualsIgnoreSeeds(
            EllipticCurve curve1, EllipticCurve curve2) {
        if (curve1 == null) {
            assertNull(curve2);
        }
        if (curve1.equals(curve2)) {
            return;
        }
        assertEquals(curve1.getClass(), curve2.getClass());
        assertEquals(curve1.getA(), curve2.getA());
        assertEquals(curve1.getB(), curve2.getB());
        assertEquals(curve1.getField(), curve2.getField());
    }
}
