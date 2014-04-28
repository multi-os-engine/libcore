/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import libcore.util.Base64;
import libcore.util.Base64DataException;

public class Base64Test extends TestCase {

    /**
     * Test that Base64.Encoder.encode() does correct handling of the tail for each call.
     */
    public void testEncoder_getTailLength() throws Exception {
        byte[] input = { (byte) 0x61, (byte) 0x62, (byte) 0x63 };
        byte[] output = new byte[100];

        Base64.Encoder encoder = new Base64.Encoder(Base64.Encoder.FLAG_NO_PADDING, 0, null);

        // Note: The encoder is not reset() between each check below. Each check builds on the last.

        int outputCount = encoder.process(input, 0, 3, output, false);
        assertEquals("YWJj".getBytes(StandardCharsets.US_ASCII), 4, output, outputCount);
        assertEquals(0, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 3, output, false);
        assertEquals("YWJj".getBytes(StandardCharsets.US_ASCII), 4, output, outputCount);
        assertEquals(0, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 1, output, false);
        assertEquals(0, outputCount);
        assertEquals(1, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 1, output, false);
        assertEquals(0, outputCount);
        assertEquals(2, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 1, output, false);
        assertEquals("YWFh".getBytes(StandardCharsets.US_ASCII), 4, output, outputCount);
        assertEquals(0, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 2, output, false);
        assertEquals(0, outputCount);
        assertEquals(2, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 2, output, false);
        assertEquals("YWJh".getBytes(StandardCharsets.US_ASCII), 4, output, outputCount);
        assertEquals(1, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 2, output, false);
        assertEquals("YmFi".getBytes(StandardCharsets.US_ASCII), 4, output, outputCount);
        assertEquals(0, encoder.getTailLength());

        outputCount = encoder.process(input, 0, 1, output, true);
        assertEquals("YQ".getBytes(StandardCharsets.US_ASCII), 2, output, outputCount);
    }

    public void testEncoder_lengthCalcs() {
        // No padding, no line breaks.
        Base64.Encoder encoder = new Base64.Encoder(Base64.Encoder.FLAG_NO_PADDING, 0, null);
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 2);
        assertEncoderLengthCalcs(encoder, 2, 3);
        assertEncoderLengthCalcs(encoder, 3, 4);
        assertEncoderLengthCalcs(encoder, 4, 6);
        assertEncoderLengthCalcs(encoder, 5, 7);
        assertEncoderLengthCalcs(encoder, 7, 10);
        assertEncoderLengthCalcs(encoder, 57, 76);
        assertEncoderLengthCalcs(encoder, 58, 78);

        // Padding, no line breaks.
        encoder = new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 0, null);
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 4); // 1.tuple
        assertEncoderLengthCalcs(encoder, 2, 4);
        assertEncoderLengthCalcs(encoder, 3, 4);
        assertEncoderLengthCalcs(encoder, 4, 8); // 2.tuple
        assertEncoderLengthCalcs(encoder, 5, 8);
        assertEncoderLengthCalcs(encoder, 7, 12);  // 3.tuple
        assertEncoderLengthCalcs(encoder, 57, 76);  // 19.tuple
        assertEncoderLengthCalcs(encoder, 58, 80);  // 20.tuple

        // Padding, 2-character appended line break.
        encoder = new Base64.Encoder(
                Base64.Encoder.FLAG_APPEND_LINE_BREAK, 0, new byte[] {'\r', '\n'});
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 6); // 1.tuple + break
        assertEncoderLengthCalcs(encoder, 2, 6);
        assertEncoderLengthCalcs(encoder, 3, 6);
        assertEncoderLengthCalcs(encoder, 4, 10); // 2.tuple + break
        assertEncoderLengthCalcs(encoder, 5, 10);
        assertEncoderLengthCalcs(encoder, 7, 14); // 3.tuple + break
        assertEncoderLengthCalcs(encoder, 57, 78); // 19.tuple + break
        assertEncoderLengthCalcs(encoder, 58, 82); // 20.tuple + break

        // Padding, 2-character line breaks every tuple except last
        encoder = new Base64.Encoder(
                Base64.Encoder.FLAG_DEFAULT, 4, new byte[] {'\r', '\n'});
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 4); // 1.(tuple + break) - break
        assertEncoderLengthCalcs(encoder, 2, 4);
        assertEncoderLengthCalcs(encoder, 3, 4);
        assertEncoderLengthCalcs(encoder, 4, 10); // 2.(tuple + break) - break
        assertEncoderLengthCalcs(encoder, 5, 10);
        assertEncoderLengthCalcs(encoder, 7, 16); // 3.(tuple + break) - break
        assertEncoderLengthCalcs(encoder, 57, 112); // 19.(tuple + break) - break
        assertEncoderLengthCalcs(encoder, 58, 118); // 20.(tuple + break) - break

        // Padding, 2-character line breaks every tuple, appended line break.
        encoder = new Base64.Encoder(
                Base64.Encoder.FLAG_APPEND_LINE_BREAK, 4, new byte[] {'\r', '\n'});
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 6); // 1.(tuple + break)
        assertEncoderLengthCalcs(encoder, 2, 6);
        assertEncoderLengthCalcs(encoder, 3, 6);
        assertEncoderLengthCalcs(encoder, 4, 12);  // 2.(tuple + break)
        assertEncoderLengthCalcs(encoder, 5, 12);
        assertEncoderLengthCalcs(encoder, 7, 18);  // 3.(tuple + break)
        assertEncoderLengthCalcs(encoder, 57, 114); // 19.(tuple + break)
        assertEncoderLengthCalcs(encoder, 58, 120); // 20.(tuple + break)

        // Padding, 2-character line breaks every line of 76 (19 tuples), appended line break.
        encoder = new Base64.Encoder(
                Base64.Encoder.FLAG_APPEND_LINE_BREAK, 76, new byte[] {'\r', '\n'});
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 6); // 1.tuple + break
        assertEncoderLengthCalcs(encoder, 2, 6);
        assertEncoderLengthCalcs(encoder, 3, 6);
        assertEncoderLengthCalcs(encoder, 4, 10);  // 2.tuple + break
        assertEncoderLengthCalcs(encoder, 5, 10);
        assertEncoderLengthCalcs(encoder, 7, 14);  // 3.tuple + break
        assertEncoderLengthCalcs(encoder, 57, 78); // 19.tuple + break
        assertEncoderLengthCalcs(encoder, 58, 84); // 19.tuple + break + 1.tuple + break

        // Padding, 2-character line breaks every line of 76 (19 tuples),
        encoder = new Base64.Encoder(
                Base64.Encoder.FLAG_DEFAULT, 76, new byte[] {'\r', '\n'});
        assertEncoderLengthCalcs(encoder, 0, 0); // Always empty
        assertEncoderLengthCalcs(encoder, 1, 4); // 1.tuple
        assertEncoderLengthCalcs(encoder, 2, 4);
        assertEncoderLengthCalcs(encoder, 3, 4);
        assertEncoderLengthCalcs(encoder, 4, 8);  // 2.tuple
        assertEncoderLengthCalcs(encoder, 5, 8);
        assertEncoderLengthCalcs(encoder, 7, 12);  // 3.tuple
        assertEncoderLengthCalcs(encoder, 57, 76); // 19.tuple
        assertEncoderLengthCalcs(encoder, 58, 82); // 19.tuple + break + 1.tuple
    }

    private static void assertEncoderLengthCalcs(
            Base64.Encoder encoder, int inputLength, int expectedExactLength) {

        byte[] input = new byte[inputLength];
        byte[] output = new byte[expectedExactLength];
        // Precalculate the length
        int actualCalculatedExactLength = encoder.calculateEncodedLength(inputLength);
        encoder.reset();
        try {
            // Actually try the encoding
            int encodedLength = encoder.process(input, 0, inputLength, output, true);

            // Assert everything agrees.
            assertEquals("Unexpected encodedLength. calculatedLength was: "
                    + actualCalculatedExactLength + ", output was: \""
                    + new String(output, StandardCharsets.US_ASCII) + "\"",
                    expectedExactLength, encodedLength);
            assertEquals("Unexpected calculated length. Output would be something like: \""
                    + new String(output, StandardCharsets.US_ASCII) + "\"",
                    expectedExactLength, actualCalculatedExactLength);

            // Assert the encoding actually filled the output buffer.
            if (expectedExactLength > 0) {
                assertFalse(output[output.length - 1] == 0);
            }

            // Assert maxOutputSize() is no less than the exact calculation.
            assertTrue(encoder.maxOutputSize(inputLength) >= actualCalculatedExactLength);
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("Unable to encode into expected length. calculatedLength was: "
                    + actualCalculatedExactLength + ": " + e.getMessage());
        }
    }

    public void testDecoder_lengthCalcs() {
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);
        assertGte(decoder.maxOutputSize(0), 0);
        assertGte(decoder.maxOutputSize(1), 0);
        assertGte(decoder.maxOutputSize(2), 0);
        assertGte(decoder.maxOutputSize(3), 0);
        assertGte(decoder.maxOutputSize(4), 1);
        assertGte(decoder.maxOutputSize(5), 1);
        assertGte(decoder.maxOutputSize(7), 1);
        assertGte(decoder.maxOutputSize(8), 2);
        assertGte(decoder.maxOutputSize(76), 57);
        assertGte(decoder.maxOutputSize(77), 60);
    }

    private static void assertGte(int expected, int actual) {
        assertTrue("Expected:" + expected + " not >= " + actual, expected >= actual);
    }

    public void testDecoder_empty() throws Exception {
        assertEquals("[]", Arrays.toString(
                Base64.decode(new byte[0], 0, 0, Base64.Decoder.FLAG_DEFAULT)));
    }

    public void testDecoder_rejectNonAlphabetBytes() throws Exception {
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_REJECT_NON_ALPHABET_BYTES);

        // Characters outside alphabet before padding.
        assertBad(decoder, " aGVsbG8sIHdvcmx");
        assertBad(decoder, "aGV sbG8sIHdvcmx");
        assertBad(decoder, "aGVsbG8sIHdvcmx ");
        assertBad(decoder, "*aGVsbG8sIHdvcmx");
        assertBad(decoder, "aGV*sbG8sIHdvcmx");
        assertBad(decoder, "aGVsbG8sIHdvcmx*");
        assertBad(decoder, "\r\naGVsbG8sIHdvcmx");
        assertBad(decoder, "aGV\r\nsbG8sIHdvcmx");
        assertBad(decoder, "aGVsbG8sIHdvcmx\r\n");
        assertBad(decoder, "\naGVsbG8sIHdvcmx");
        assertBad(decoder, "aGV\nsbG8sIHdvcmx");
        assertBad(decoder, "aGVsbG8sIHdvcmx\n");

        // padding 0
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk"));
        // Extra padding
        assertBad(decoder, "aGVsbG8sIHdvcmxk=");
        assertBad(decoder, "aGVsbG8sIHdvcmxk==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxk =");
        assertBad(decoder, "aGVsbG8sIHdvcmxk = = ");

        // padding 1
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE="));
        // Missing padding
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE"));
        // Characters outside alphabet before padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE =");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE*=");
        // Trailing characters, otherwise valid.
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE= ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=*");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=X");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XY");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=\n");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=\r\n");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE= ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE ==");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE = = ");

        // padding 2
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=="));
        // Missing padding
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg"));
        // Partially missing padding
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=");
        // Characters outside alphabet before padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg ==");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg*==");
        // Trailing characters, otherwise valid.
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg== ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==*");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==X");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XY");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZA");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==\n");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==\r\n");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg== ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg===");
        // Characters outside alphabet inside padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg= =");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=*=");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=\r\n=");
        // Characters inside alphabet inside padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=X=");
    }

    public void testDecodeExtraChars_urlSafe() throws Exception {
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_URL_SAFE);

        // Characters outside alphabet before padding.
        assertEquals("hello, world", decodeString(decoder, " aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk "));
        assertEquals("hello, world", decodeString(decoder, "/aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV/sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk/"));
        assertEquals("hello, world", decodeString(decoder, "*aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV*sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk*"));
        assertEquals("hello, world", decodeString(decoder, "\r\naGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV\r\nsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk\r\n"));
        assertEquals("hello, world", decodeString(decoder, "\naGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV\nsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk\n"));

        // padding 0
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk"));
        // Extra padding
        assertBad(decoder, "aGVsbG8sIHdvcmxk=");
        assertBad(decoder, "aGVsbG8sIHdvcmxk==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxk =");
        assertBad(decoder, "aGVsbG8sIHdvcmxk = = ");

        // padding 1
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE="));
        // Missing padding
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE"));
        // Characters outside alphabet before padding.
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE ="));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE*="));
        // Trailing characters, otherwise valid.
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE= "));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=*"));
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=X");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XY");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=\n"));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=\r\n"));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE= "));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=="));
        // Characters outside alphabet intermixed with (too much) padding.
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE =="));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE = = "));

        // padding 2
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=="));
        // Missing padding
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg"));
        // Partially missing padding
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=");
        // Characters outside alphabet before padding.
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg =="));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg*=="));
        // Trailing characters, otherwise valid.
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg== "));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==*"));
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==X");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XY");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZA");
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==\n"));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==\r\n"));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg== "));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==="));
        // Characters outside alphabet inside padding. This is a difference from the RI which is not
        // as permissive.
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg= ="));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=*="));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=\r\n="));
        // Characters inside alphabet inside padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=X=");
    }

    public void testDecoder_extraChars() throws Exception {
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);

        // Characters outside alphabet before padding.
        assertEquals("hello, world", decodeString(decoder, " aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk "));
        assertEquals("hello, world", decodeString(decoder, "_aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV_sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk_"));
        assertEquals("hello, world", decodeString(decoder, "*aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV*sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk*"));
        assertEquals("hello, world", decodeString(decoder, "\r\naGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV\r\nsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk\r\n"));
        assertEquals("hello, world", decodeString(decoder, "\naGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGV\nsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk\n"));

        // padding 0
        assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk"));
        // Extra padding
        assertBad(decoder, "aGVsbG8sIHdvcmxk=");
        assertBad(decoder, "aGVsbG8sIHdvcmxk==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxk =");
        assertBad(decoder, "aGVsbG8sIHdvcmxk = = ");

        // padding 1
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE="));
        // Missing padding
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE"));
        // Characters outside alphabet before padding.
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE ="));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE*="));
        // Trailing characters, otherwise valid.
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE= "));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=*"));
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=X");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XY");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=\n"));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=\r\n"));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE= "));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE=="));
        // Characters outside alphabet intermixed with (too much) padding.
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE =="));
        assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE = = "));

        // padding 2
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=="));
        // Missing padding
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg"));
        // Partially missing padding
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=");
        // Characters outside alphabet before padding.
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg =="));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg*=="));
        // Trailing characters, otherwise valid.
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg== "));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==*"));
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==X");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XY");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZ");
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZA");
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==\n"));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==\r\n"));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg== "));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg==="));
        // Characters outside alphabet inside padding. This is a difference from the RI which is not
        // as permissive.
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg= ="));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=*="));
        assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=\r\n="));
        // Characters inside alphabet inside padding.
        assertBad(decoder, "aGVsbG8sIHdvcmxkLg=X=");
    }

    private static final byte[] BYTES = { (byte) 0xff, (byte) 0xee, (byte) 0xdd,
                                          (byte) 0xcc, (byte) 0xbb, (byte) 0xaa,
                                          (byte) 0x99, (byte) 0x88, (byte) 0x77 };
    
    public void testBinaryDecode() throws Exception {
        assertEquals(BYTES, 0, decode("", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 1, decode("/w==", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 2, decode("/+4=", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 3, decode("/+7d", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 4, decode("/+7dzA==", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 5, decode("/+7dzLs=", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 6, decode("/+7dzLuq", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 7, decode("/+7dzLuqmQ==", Base64.Decoder.FLAG_DEFAULT));
        assertEquals(BYTES, 8, decode("/+7dzLuqmYg=", Base64.Decoder.FLAG_DEFAULT));
    }

    private static byte[] decode(String string, int flags) throws Base64DataException {
        byte[] in = string.getBytes(StandardCharsets.US_ASCII);
        return Base64.decode(in, 0, in.length, flags);
    }

    public void testWebSafe() throws Exception {
        assertEquals(BYTES, 0, decode("", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 1, decode("_w==", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 2, decode("_-4=", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 3, decode("_-7d", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 4, decode("_-7dzA==", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 5, decode("_-7dzLs=", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 6, decode("_-7dzLuq", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 7, decode("_-7dzLuqmQ==", Base64.Decoder.FLAG_URL_SAFE));
        assertEquals(BYTES, 8, decode("_-7dzLuqmYg=", Base64.Decoder.FLAG_URL_SAFE));

        Assert.assertEquals("", encodeToString(BYTES, 0, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_w==", encodeToString(BYTES, 1, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-4=", encodeToString(BYTES, 2, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-7d", encodeToString(BYTES, 3, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-7dzA==", encodeToString(BYTES, 4, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-7dzLs=", encodeToString(BYTES, 5, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-7dzLuq", encodeToString(BYTES, 6, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-7dzLuqmQ==", encodeToString(BYTES, 7, Base64.Encoder.FLAG_URL_SAFE));
        Assert.assertEquals("_-7dzLuqmYg=", encodeToString(BYTES, 8, Base64.Encoder.FLAG_URL_SAFE));
    }

    public void testEncoderDefaultFlags() throws Exception {
        Base64.Encoder encoder = new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 0, null);
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);
        assertEquals("YQ==", encodeToString("a", encoder, decoder));
        assertEquals("YWI=", encodeToString("ab", encoder, decoder));
        assertEquals("YWJj", encodeToString("abc", encoder, decoder));
        assertEquals("YWJjZA==", encodeToString("abcd", encoder, decoder));
    }

    public void testEncoderAppendLineBreak_LF() throws Exception {
        testEncoderAppendLineBreak(new byte[] { '\n' });
    }
    
    public void testEncoderAppendLineBreak_CRLF() throws Exception {
        testEncoderAppendLineBreak(new byte[] { '\r', '\n' });
    }

    private void testEncoderAppendLineBreak(byte[] lineBreak) throws Exception {
        Base64.Encoder encoder =
                new Base64.Encoder(Base64.Encoder.FLAG_APPEND_LINE_BREAK, 0, lineBreak);
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);

        assertEquals(appendBytes("YQ==", lineBreak), encodeToString("a", encoder, decoder));
        assertEquals(appendBytes("YWI=", lineBreak), encodeToString("ab", encoder, decoder));
        assertEquals(appendBytes("YWJj", lineBreak), encodeToString("abc", encoder, decoder));
        assertEquals(appendBytes("YWJjZA==", lineBreak), encodeToString("abcd", encoder, decoder));
    }

    private String appendBytes(String string, byte[] lineBreak) {
        String suffix = new String(lineBreak, StandardCharsets.US_ASCII);
        return string + suffix;
    }

    public void testEncoderNoPadding() throws Exception {
        Base64.Encoder encoder = new Base64.Encoder(Base64.Encoder.FLAG_NO_PADDING, 0, null);
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);

        assertEquals("YQ", encodeToString("a", encoder, decoder));
        assertEquals("YWI", encodeToString("ab", encoder, decoder));
        assertEquals("YWJj", encodeToString("abc", encoder, decoder));
        assertEquals("YWJjZA", encodeToString("abcd", encoder, decoder));
    }
    
    public void testEncoderCombinedConfiguration() throws Exception {
        Base64.Encoder encoder = new Base64.Encoder(
                Base64.Encoder.FLAG_APPEND_LINE_BREAK | Base64.Encoder.FLAG_NO_PADDING,
                4 /* lineLength */, new byte[] {'\r'} /* lineBreak */);
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);

        assertEquals("YQ\r", encodeToString("a", encoder, decoder));
        assertEquals("YWI\r", encodeToString("ab", encoder, decoder));
        assertEquals("YWJj\r", encodeToString("abc", encoder, decoder));
        assertEquals("YWJj\rZA\r", encodeToString("abcd", encoder, decoder));
    }

    /**
     * Encodes string using the supplied encoder. Also asserts that decoding gives the same string.
     * Returns the encoded string.
     */
    private static String encodeToString(
            String string, Base64.Encoder encoder, Base64.Decoder decoder) throws Exception {


        byte[] in = string.getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[encoder.calculateEncodedLength(in.length)];
        encoder.reset();
        int encodedBytesCount = encoder.process(in, 0, in.length, out, true);
        assertEquals(out.length, encodedBytesCount);

        byte[] in2 = new byte[in.length];
        decoder.reset();
        int decodedBytesCount = decoder.process(out, 0, encodedBytesCount, in2, true);
        assertEquals(in.length, decodedBytesCount);
        assertEquals(string, new String(in2, 0, in2.length, StandardCharsets.US_ASCII));

        return new String(out, 0, out.length, StandardCharsets.US_ASCII);
    }

    public void testEncoderLineLength() throws Exception {
        String in_56 = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcd";
        String in_57 = in_56 + "e";
        String in_58 = in_56 + "ef";
        String in_59 = in_56 + "efg";
        String in_60 = in_56 + "efgh";
        String in_61 = in_56 + "efghi";

        String prefix = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXphYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5emFi";
        String out_56 = prefix + "Y2Q=\n";
        String out_57 = prefix + "Y2Rl\n";
        String out_58 = prefix + "Y2Rl\nZg==\n";
        String out_59 = prefix + "Y2Rl\nZmc=\n";
        String out_60 = prefix + "Y2Rl\nZmdo\n";
        String out_61 = prefix + "Y2Rl\nZmdoaQ==\n";

        Base64.Encoder encoder = new Base64.Encoder(Base64.Encoder.FLAG_APPEND_LINE_BREAK, 76, new byte[] {'\n'});
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);

        // no newline for an empty input array.
        assertEquals("", encodeToString("", encoder, decoder));

        assertEquals(out_56, encodeToString(in_56, encoder, decoder));
        assertEquals(out_57, encodeToString(in_57, encoder, decoder));
        assertEquals(out_58, encodeToString(in_58, encoder, decoder));
        assertEquals(out_59, encodeToString(in_59, encoder, decoder));
        assertEquals(out_60, encodeToString(in_60, encoder, decoder));
        assertEquals(out_61, encodeToString(in_61, encoder, decoder));

        Base64.Encoder noPaddingEncoder = new Base64.Encoder(
                Base64.Encoder.FLAG_APPEND_LINE_BREAK | Base64.Encoder.FLAG_NO_PADDING, 76, new byte[] {'\n'});
        
        assertEquals(out_56.replaceAll("=", ""), encodeToString(in_56, noPaddingEncoder, decoder));
        assertEquals(out_57.replaceAll("=", ""), encodeToString(in_57, noPaddingEncoder, decoder));
        assertEquals(out_58.replaceAll("=", ""), encodeToString(in_58, noPaddingEncoder, decoder));
        assertEquals(out_59.replaceAll("=", ""), encodeToString(in_59, noPaddingEncoder, decoder));
        assertEquals(out_60.replaceAll("=", ""), encodeToString(in_60, noPaddingEncoder, decoder));
        assertEquals(out_61.replaceAll("=", ""), encodeToString(in_61, noPaddingEncoder, decoder));

        Base64.Encoder noWrapEncoder = new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 0, null);
        assertEquals(out_56.replaceAll("\n", ""), encodeToString(in_56, noWrapEncoder, decoder));
        assertEquals(out_57.replaceAll("\n", ""), encodeToString(in_57, noWrapEncoder, decoder));
        assertEquals(out_58.replaceAll("\n", ""), encodeToString(in_58, noWrapEncoder, decoder));
        assertEquals(out_59.replaceAll("\n", ""), encodeToString(in_59, noWrapEncoder, decoder));
        assertEquals(out_60.replaceAll("\n", ""), encodeToString(in_60, noWrapEncoder, decoder));
        assertEquals(out_61.replaceAll("\n", ""), encodeToString(in_61, noWrapEncoder, decoder));
    }

    public void testEncoder_lowBytes() throws Exception {
        Base64.Encoder encoder = new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 0, null);
        assertEquals("", encodeBytesToString(encoder));
        assertEquals("Eg==", encodeBytesToString(encoder, 0x12));
        assertEquals("EjQ=", encodeBytesToString(encoder, 0x12, 0x34));
        assertEquals("EjRW", encodeBytesToString(encoder, 0x12, 0x34, 0x56));
        assertEquals("EjRWeA==", encodeBytesToString(encoder, 0x12, 0x34, 0x56, 0x78));
        assertEquals("EjRWeJo=", encodeBytesToString(encoder, 0x12, 0x34, 0x56, 0x78, 0x9A));
        assertEquals("EjRWeJq8", encodeBytesToString(encoder, 0x12, 0x34, 0x56, 0x78, 0x9a, 0xbc));
    }

    /** Encode a set of bytes with the supplied flags and no line wrapping. */
    private static String encodeToString(byte[] in, int length, int flags)
            throws Base64DataException {
        Base64.Encoder encoder =
                new Base64.Encoder(flags, 0 /* lineLength */, null /* lineBreak */);
        byte[] bytes = new byte[encoder.calculateEncodedLength(length)];
        encoder.process(in, 0, length, bytes, true /* finish */);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /** Encode a set of bytes with FLAG_DEFAULT and no line wrapping. */
    private static String encodeBytesToString(Base64.Encoder encoder, int... data)
            throws Base64DataException {
        byte[] dataBytes = intsToBytes(data);

        byte[] output = new byte[encoder.calculateEncodedLength(dataBytes.length)];
        encoder.reset();
        int outputLength =
                encoder.process(dataBytes, 0, dataBytes.length, output, true /* finish */);
        assertEquals(output.length, outputLength);
        return new String(output, StandardCharsets.US_ASCII);
    }

    private static byte[] intsToBytes(int[] data) {
        byte[] dataBytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            dataBytes[i] = (byte) data[i];
        }
        return dataBytes;
    }

    /** Decode a string with the supplied decoder, returning a string. */
    private static String decodeString(Base64.Decoder decoder, String string) throws Exception {
        byte[] in = string.getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[decoder.maxOutputSize(in.length)];
        decoder.reset();
        int bytesDecoded = decoder.process(in, 0, in.length, out, true);
        return new String(out, 0, bytesDecoded, StandardCharsets.US_ASCII);
    }

    /** Assert that decoding string with the supplied decoder throws Base64DataException. */
    private void assertBad(Base64.Decoder decoder, String string) throws Exception {
        byte[] in = string.getBytes(StandardCharsets.US_ASCII);
        assertBad(decoder, in);
    }

    /** Assert that decoding 'in' with the supplied decoder throws Base64DataException. */
    private void assertBad(Base64.Decoder decoder, byte[] in) throws Exception {
        byte[] out = new byte[decoder.maxOutputSize(in.length)];
        decoder.reset();
        try {
            decoder.process(in, 0, in.length, out, true);
            fail("should have failed to decode");
        } catch (Base64DataException e) {
        }
    }

    /** Assert that actual equals the first len bytes of expected. */
    private static void assertEquals(byte[] expected, int len, byte[] actual) {
        assertEquals(len, actual.length);
        for (int i = 0; i < len; ++i) {
            assertEquals(expected[i], actual[i]);
        }
    }

    /** Assert that actual equals the first len bytes of expected. */
    private static void assertEquals(byte[] expected, int len, byte[] actual, int alen) {
        assertEquals(len, alen);
        for (int i = 0; i < len; ++i) {
            assertEquals(expected[i], actual[i]);
        }
    }

}
