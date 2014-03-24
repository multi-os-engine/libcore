/*
 * Copyright 2014 The Android Open Source Project
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

package libcore.java.security;

import java.security.CryptoPrimitive;

import junit.framework.TestCase;

public class CryptoPrimitiveTest extends TestCase {
    public void testCryptoPrimitive_ordinal_ExpectedValues() throws Exception {
        assertEquals("BLOCK_CIPHER", 2, CryptoPrimitive.BLOCK_CIPHER.ordinal());
        assertEquals("KEY_AGREEMENT", 9, CryptoPrimitive.KEY_AGREEMENT.ordinal());
        assertEquals("KEY_ENCAPSULATION", 8, CryptoPrimitive.KEY_ENCAPSULATION.ordinal());
        assertEquals("KEY_WRAP", 5, CryptoPrimitive.KEY_WRAP.ordinal());
        assertEquals("MAC", 4, CryptoPrimitive.MAC.ordinal());
        assertEquals("MESSAGE_DIGEST", 0, CryptoPrimitive.MESSAGE_DIGEST.ordinal());
        assertEquals("PUBLIC_KEY_ENCRYPTION", 6, CryptoPrimitive.PUBLIC_KEY_ENCRYPTION.ordinal());
        assertEquals("SECURE_RANDOM", 1, CryptoPrimitive.SECURE_RANDOM.ordinal());
        assertEquals("SIGNATURE", 7, CryptoPrimitive.SIGNATURE.ordinal());
        assertEquals("STREAM_CIPHER", 3, CryptoPrimitive.STREAM_CIPHER.ordinal());
    }

    public void testCryptoPrimitive_values_ExpectedValues() throws Exception {
        CryptoPrimitive[] primitives = CryptoPrimitive.values();
        assertEquals(10, primitives.length);
        assertEquals(CryptoPrimitive.BLOCK_CIPHER, primitives[2]);
        assertEquals(CryptoPrimitive.KEY_AGREEMENT, primitives[9]);
        assertEquals(CryptoPrimitive.KEY_ENCAPSULATION, primitives[8]);
        assertEquals(CryptoPrimitive.KEY_WRAP, primitives[5]);
        assertEquals(CryptoPrimitive.MAC, primitives[4]);
        assertEquals(CryptoPrimitive.MESSAGE_DIGEST, primitives[0]);
        assertEquals(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION, primitives[6]);
        assertEquals(CryptoPrimitive.SECURE_RANDOM, primitives[1]);
        assertEquals(CryptoPrimitive.SIGNATURE, primitives[7]);
        assertEquals(CryptoPrimitive.STREAM_CIPHER, primitives[3]);
    }

    public void testCryptoPrimitive_valueOf_ExpectedValues() throws Exception {
        assertEquals(CryptoPrimitive.BLOCK_CIPHER, CryptoPrimitive.valueOf("BLOCK_CIPHER"));
        assertEquals(CryptoPrimitive.KEY_AGREEMENT, CryptoPrimitive.valueOf("KEY_AGREEMENT"));
        assertEquals(CryptoPrimitive.KEY_ENCAPSULATION, CryptoPrimitive.valueOf("KEY_ENCAPSULATION"));
        assertEquals(CryptoPrimitive.KEY_WRAP, CryptoPrimitive.valueOf("KEY_WRAP"));
        assertEquals(CryptoPrimitive.MAC, CryptoPrimitive.valueOf("MAC"));
        assertEquals(CryptoPrimitive.MESSAGE_DIGEST, CryptoPrimitive.valueOf("MESSAGE_DIGEST"));
        assertEquals(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION, CryptoPrimitive.valueOf("PUBLIC_KEY_ENCRYPTION"));
        assertEquals(CryptoPrimitive.SECURE_RANDOM, CryptoPrimitive.valueOf("SECURE_RANDOM"));
        assertEquals(CryptoPrimitive.SIGNATURE, CryptoPrimitive.valueOf("SIGNATURE"));
        assertEquals(CryptoPrimitive.STREAM_CIPHER, CryptoPrimitive.valueOf("STREAM_CIPHER"));
    }
}
