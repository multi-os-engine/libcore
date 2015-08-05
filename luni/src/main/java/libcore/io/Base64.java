/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.io;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Perform encoding and decoding of Base64 byte arrays as described in
 * http://www.ietf.org/rfc/rfc2045.txt
 */
public final class Base64 {
    private static final byte[] BASE_64_ALPHABET = initializeBase64Alphabet();

    private static byte[] initializeBase64Alphabet() {
        try {
            return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
                    .getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /** Where to find the first encoded 6-bit in an int expressing a byte triplet. */
    private static final int FIRST_OUTPUT_BYTE_MASK = 0x3f << 18;
    private static final int SECOND_OUTPUT_BYTE_MASK = 0x3f << 12;
    private static final int THIRD_OUTPUT_BYTE_MASK = 0x3f << 6;
    private static final int FOURTH_OUTPUT_BYTE_MASK = 0x3f;

    public static String encode(byte[] in) {
        int len = in.length;
        int outputLen;
        outputLen = computeEncodingOutputLen(len);
        byte[] output = new byte[outputLen];

        int outputIndex = 0;
        int i = 0;
        for (i = 0; i < len; i += 3) {
            // Only a "triplet" if there are there are at least three remaining bytes
            // in the input...
            // Mask with 0xff to avoid signed extension.
            int byteTripletAsInt = in[i] & 0xff;
            if (i + 1 < len) {
                // Add second byte to the triplet.
                byteTripletAsInt <<= 8;
                byteTripletAsInt += in[i + 1] & 0xff;
                if (i + 2 < len) {
                    byteTripletAsInt <<= 8;
                    byteTripletAsInt += in[i + 2] & 0xff;
                } else {
                    // Insert 2 zero bits as to make output 18 bits long.
                    byteTripletAsInt <<= 2;
                }
            } else {
                // Insert 4 zero bits as to make output 12 bits long.
                byteTripletAsInt <<= 4;
            }

            if (i + 2 < len) {
                // The int may have up to 24 non-zero bits.
                output[outputIndex++] = BASE_64_ALPHABET[
                        (byteTripletAsInt & FIRST_OUTPUT_BYTE_MASK) >> 18];
            }
            if (i + 1 < len) {
                // The int may have up to 18 non-zero bits.
                output[outputIndex++] = BASE_64_ALPHABET[
                        (byteTripletAsInt & SECOND_OUTPUT_BYTE_MASK) >> 12];
            }
            output[outputIndex++] = BASE_64_ALPHABET[
                    (byteTripletAsInt & THIRD_OUTPUT_BYTE_MASK) >> 6];
            output[outputIndex++] = BASE_64_ALPHABET[
                    byteTripletAsInt & FOURTH_OUTPUT_BYTE_MASK];
        }

        int inLengthMod3 = len % 3;
        // Add padding as per the spec.
        if (inLengthMod3 > 0) {
            output[outputIndex++] = '=';
            if (inLengthMod3 == 1) {
                output[outputIndex++] = '=';
            }
        }

        try {
            return new String(output, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static int computeEncodingOutputLen(int inLength) {
        int inLengthMod3 = inLength % 3;
        int outputLen;
        if (inLengthMod3 == 2) {
            outputLen = ((inLength) / 3) * 4
                    // Need 3 6-bit characters as to express the last 16 bits.
                    + 3
                    // Padding as per spec.
                    + 1;
        } else if (inLengthMod3 == 1) {
            outputLen = ((inLength) / 3) * 4
                    // Need 2 6-bit characters as to express the last 8 bits.
                    + 2
                    // Padding as per spec.
                    + 2;
        } else {
            outputLen = ((inLength) / 3) * 4;
        }
        return outputLen;
    }

    public static byte[] decode(byte[] in) {
        return decode(in, in.length);
    }

    /** Decodes the input from position 0 (inclusive) to len (exclusive). */
    public static byte[] decode(byte[] in, int len) {
        final int inLength = in.length;
        int i = 0;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while (i < inLength) {
            int byteTripletAsInt = 0;
            byte c = 0;

            // j is the index in a 4-tuple of 6-bit characters where are trying to read from the
            // input.
            for (int j = 0; j < 4; j++) {
                // Ignore all characters not in the alphabet as per spec.
                while (i < inLength
                        && (c = base64AlphabetToInt(in[i])) == NON_BASE64_ALPHABET_AS_BYTE) {
                    i++;
                }
                if (i == inLength || c == PAD_AS_BYTE) {
                    // The input is over.
                    switch (j) {
                        case 0:
                            return output.toByteArray();
                        case 1:
                            // The input is actually ill-formed. Assuming the 6 bits we have so
                            // far are the least significant bits of a byte.
                            output.write(byteTripletAsInt);
                            return output.toByteArray();
                        case 2:
                            // The input is over with two 6-bit characters: a single byte padded
                            // with 4 extra 0's.
                            byteTripletAsInt >>= 4;
                            output.write(byteTripletAsInt);
                            return output.toByteArray();
                        case 3:
                            // The input is over with three 6-bit characters: two bytes padded
                            // with 2 extra 0's.
                            byteTripletAsInt >>= 2;
                            output.write(byteTripletAsInt >> 8);
                            output.write(byteTripletAsInt & 0xff);
                            return output.toByteArray();
                    }
                }
                byteTripletAsInt <<= 6;
                byteTripletAsInt += (c & 0xff);
                i++;
            }

            // We have four 6-bit characters, outputting the corresponding 3 bytes
            output.write(byteTripletAsInt >> 16);
            output.write((byteTripletAsInt >> 8) & 0xff);
            output.write(byteTripletAsInt & 0xff);
        }
        return output.toByteArray();
    }

    private static final byte PAD_AS_BYTE = -1;
    private static final byte NON_BASE64_ALPHABET_AS_BYTE = -2;
    private static byte base64AlphabetToInt(byte c) {
        if ('A' <= c && c <= 'Z') {
            return (byte) (c - 'A');
        }
        if ('a' <= c && c <= 'z') {
            return (byte) (c - 'a' + 26);
        }
        if ('0' <= c && c <= '9') {
            return (byte) (c - '0' + 52);
        }
        if (c == '+')
            return (byte) 62;
        if (c == '/')
            return (byte) 63;
        if (c == '=') {
            return PAD_AS_BYTE;
        }
        return NON_BASE64_ALPHABET_AS_BYTE;
    }
}
