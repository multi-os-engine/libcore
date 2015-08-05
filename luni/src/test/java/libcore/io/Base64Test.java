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

package libcore.io;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import junit.framework.TestCase;

public final class Base64Test extends TestCase {

    public void testDecodeEmpty() throws Exception {
        assertEquals("[]", Arrays.toString(Base64.decode(new byte[0])));
    }

    public void testEncodeAndDecode() throws Exception {
        assertEncodeAndDecode("");
        assertEncodeAndDecode("AA==", 0x0);
        assertEncodeAndDecode("Eg==", 0x12);
        assertEncodeAndDecode("EjQ=", 0x12, 0x34);
        assertEncodeAndDecode("EjRW", 0x12, 0x34, 0x56);
        assertEncodeAndDecode("EjRWeA==", 0x12, 0x34, 0x56, 0x78);
        assertEncodeAndDecode("EjRWeJo=", 0x12, 0x34, 0x56, 0x78, 0x9A);
        assertEncodeAndDecode("EjRWeJq8", 0x12, 0x34, 0x56, 0x78, 0x9a, 0xbc);
        assertEncodeAndDecode("mYg=", (byte) 0x99, (byte) 0x88);
        assertEncodeAndDecode("A0B1C2///+++cedZ",
                0x03, 0x40, 0x75, 0x0b, 0x6f, 0xff, 0xff, 0xef, 0xbe, 0x71, 0xe7, 0x59);
    }

    public void testEncodeDoesNotWrap() throws Exception {
        int[] data = new int[61];
        Arrays.fill(data, 0xff);
        String expected = "///////////////////////////////////////////////////////////////////////"
                + "//////////w=="; // 84 chars
        assertEncodeAndDecode(expected, data);
    }

    public void assertEncodeAndDecode(String encoded, int... plain) throws Exception {
        byte[] dataBytes = new byte[plain.length];
        for (int i = 0; i < plain.length; i++) {
            dataBytes[i] = (byte) plain[i];
        }
        assertEquals("encoding", encoded, Base64.encode(dataBytes));
        assertEquals("decoding",
                Arrays.toString(dataBytes),
                Arrays.toString(Base64.decode(encoded.getBytes("ASCII"))));
    }
}

