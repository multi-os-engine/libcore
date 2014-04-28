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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Utilities for encoding and decoding the Base64 representation of
 * binary data.  See RFCs <a
 * href="http://www.ietf.org/rfc/rfc2045.txt">2045</a> and <a
 * href="http://www.ietf.org/rfc/rfc3548.txt">3548</a>.
 */
public class Base64 {

    //  --------------------------------------------------------
    //  shared code
    //  --------------------------------------------------------

    /**
     * A shared interface for {@link Encoder} and {@link Decoder}.
     */
    public static abstract class Coder {
        /**
         * Encode/decode another block of input data. output is provided by the caller, and must be
         * big enough to hold all the coded data. On exit, the return value contains the length
         * of the coded data.
         *
         * @param finish true if this is the final call to process for this object.  Will finalize
         *        the coder state and include any final bytes in the output.
         * @throws Base64DataException if the input data could not be processed
         */
        public abstract int process(
                byte[] input, int inputOffset, int inputLength, byte[] output, boolean finish)
                throws Base64DataException;

        /**
         * Returns the maximum number of bytes a call to process() could produce for the given
         * number of input bytes. Intended for use when sizing output buffers for calls to
         * process().
         */
        public abstract int maxOutputSize(int len);
    }

    //  --------------------------------------------------------
    //  decoding
    //  --------------------------------------------------------

    /**
     * Decode the Base64-encoded data in input and return the data in a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but if any are present,
     * there must be the correct number of them.
     *
     * @param input the input array to decode
     * @throws Base64DataException if the input data could not be processed
     */
    public static byte[] decode(byte[] input) throws Base64DataException {
        int flags = Base64.Decoder.FLAG_DEFAULT;
        return decode(input, 0, input.length, flags);
    }

    /**
     * Decode the Base64-encoded data in input and return the data in a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but if any are present,
     * there must be the correct number of them.
     *
     * @param input  the data to decode
     * @param offset the position within the input array at which to start
     * @param len    the number of bytes of input to decode
     * @param flags  controls certain features of the decoded output. Pass {@link Decoder#FLAG_DEFAULT}
     *     to decode standard Base64.
     * @throws Base64DataException if the input data could not be processed
     */
    public static byte[] decode(byte[] input, int offset, int len, int flags)
            throws Base64DataException {

        Decoder decoder = new Decoder(flags);

        // Allocate space for the most data the input could represent.
        // (It could contain less if it contains whitespace, etc.)
        byte[] output = new byte[decoder.maxOutputSize(len)];
        int op = decoder.process(input, offset, len, output, true);

        // Maybe we got lucky and allocated exactly enough output space.
        if (op == output.length) {
            return output;
        }

        // Need to shorten the array, so allocate a new one of the
        // right size and copy.
        byte[] temp = new byte[op];
        System.arraycopy(output, 0, temp, 0, op);
        return temp;
    }

    /**
     * A base64 decoder. See the various FLAG_ constants for configuration. Decoder is not
     * thread-safe. A decoder can be reused by calling {@link #reset()}.

     */
    public static class Decoder extends Coder {
        /**
         * Default values for decoder flags. Default means:
         * <ul>
         *     <li>Use the standard dictionary for base 64 decoding</li>
         *     <li>Reject any bytes outside of the base64 alphabet, including whitespace.</li>
         * </ul>
         */
        public static final int FLAG_DEFAULT = 0;

        /**
         * Decoder flag bit to indicate using the "URL and filename safe" variant of Base64 (see RFC
         * 3548 section 4) where {@code -} and {@code _} are used in place of {@code +} and
         * {@code /}.
         */
        public static final int FLAG_URL_SAFE = 1;

        /**
         * Decoder flag bit to indicate the behavior if a byte read falls outside of the Base64
         * alphabet table.
         */
        public static final int FLAG_REJECT_NON_ALPHABET_BYTES = 2;

        /**
         * Lookup table for turning bytes into their position in the Base64 alphabet.
         */
        private static final byte[] DECODE = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
                -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
                -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        /**
         * Decode lookup table for the "web safe" variant (RFC 3548 sec. 4) where - and _ replace +
         * and /.
         */
        private static final byte[] DECODE_WEBSAFE = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
                -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63,
                -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        /** Non-data values in the DECODE arrays. */
        @SuppressWarnings("unused")
        private static final int OUTSIDE_ALPHABET = -1; // skip
        private static final int EQUALS = -2; // (pad)

        private final byte[] alphabet;

        /**
         * If true, any byte values that fall outside of the valid Base64 dictionary cause an
         * immediate error.
         */
        private final boolean rejectInvalidBytes;

        /**
         * State number (0 to 6).
         *
         * States 0-3 are reading through the next input tuple.
         * State 4 is having read one '=' and expecting exactly one more.
         * State 5 is expecting no more data or padding characters in the input.
         * State 6 is the error state; an error has been detected in the input and no future input
         * can "fix" it.
         */
        private int state;

        /** Any previously partially-read tuple (only meaningful for states 1 & 2) */
        private int partiallyDecodedTuple;

        public Decoder(int flags) {
            alphabet = ((flags & FLAG_URL_SAFE) == 0) ? DECODE : DECODE_WEBSAFE;
            rejectInvalidBytes = (flags & FLAG_REJECT_NON_ALPHABET_BYTES) > 0;
            state = 0;
            partiallyDecodedTuple = 0;
        }

        /**
         * Reset the decoder's state so that it can be used to decode a new set of bytes.
         */
        public void reset() {
            state = 0;
            partiallyDecodedTuple = 0;
        }

        @Override
        public int maxOutputSize(int len) {
            // The len may contain ignorable characters, so this may be too much. The +10 is to
            // provide a sensible minimum buffer size.
            return ((len * 3) / 4) + 10;
        }

        /**
         * Decode another block of input data.
         *
         * @return the number of bytes successfully decoded
         * @throws Base64DataException if the input data could not be processed
         */
        @Override
        public int process(
                byte[] input, int inputOffset, int inputLength, byte[] output, boolean finish)
                throws Base64DataException {

            if (state == 6) throw new Base64DataException("Bad state");

            int currentPos = inputOffset;
            int endPos = inputLength + inputOffset;

            // Using local variables makes the decoder about 12% faster than if we manipulate the
            // member variables in the loop.  (Even alphabet makes a measurable difference, which is
            // somewhat surprising to me since the member variable is final.)
            int state = this.state;
            int decodedTuple = this.partiallyDecodedTuple;
            int bytesDecoded = 0;
            final byte[] alphabet = this.alphabet;

            while (currentPos < endPos) {
                // Try the fast path:  we're starting a new tuple and the next four bytes of the
                // input stream are all data bytes. This corresponds to going through states
                // 0-1-2-3-0.  We expect to use this method for most of the data.
                //
                // If any of the next four bytes of input are non-data (whitespace, invalid, etc.),
                // decodedTuple will end up negative.  (All the non-data values in alphabet are
                // small negative numbers, so shifting any of them up and or'ing them together will
                // result in a decodedTuple with its top bit set.)
                //
                // You can remove this whole block and the output should be the same, just slower.
                if (state == 0) {
                    while (currentPos + 4 <= endPos &&
                            (decodedTuple = ((alphabet[input[currentPos] & 0xff] << 18) |
                                    (alphabet[input[currentPos + 1] & 0xff] << 12) |
                                    (alphabet[input[currentPos + 2] & 0xff] << 6) |
                                    (alphabet[input[currentPos + 3] & 0xff]))) >= 0) {
                        output[bytesDecoded + 2] = (byte) decodedTuple;
                        output[bytesDecoded + 1] = (byte) (decodedTuple >> 8);
                        output[bytesDecoded] = (byte) (decodedTuple >> 16);
                        bytesDecoded += 3;
                        currentPos += 4;
                    }
                    if (currentPos >= endPos) break;
                }

                // The fast path isn't available -- either we've read a partial tuple, or the next
                // four input bytes aren't all data, or whatever.  Fall back to the slower state
                // machine implementation.

                int decodedByte = alphabet[input[currentPos++] & 0xff];

                switch (state) {
                    case 0:
                        if (decodedByte >= 0) {
                            decodedTuple = decodedByte;
                            ++state;
                        } else if (decodedByte == EQUALS || rejectInvalidBytes) {
                            // rejectInvalidBytes also implies decodedByte == OUTSIDE_ALPHABET
                            this.state = 6;
                            byte rejectedByte = input[currentPos - 1];
                            throw new Base64DataException("Invalid first byte of tuple read: " +
                                    (rejectedByte & 0xff) + " (" + (char) rejectedByte + ")");
                        }
                        break;
                    case 1:
                        if (decodedByte >= 0) {
                            decodedTuple = (decodedTuple << 6) | decodedByte;
                            ++state;
                        } else if (decodedByte == EQUALS || rejectInvalidBytes) {
                            // rejectInvalidBytes also implies decodedByte == OUTSIDE_ALPHABET
                            this.state = 6;
                            byte rejectedByte = input[currentPos - 1];
                            throw new Base64DataException("Invalid second byte of tuple read: " +
                                    (rejectedByte & 0xff) + " (" + (char) rejectedByte + ")");
                        }
                        break;
                    case 2:
                        if (decodedByte >= 0) {
                            decodedTuple = (decodedTuple << 6) | decodedByte;
                            ++state;
                        } else if (decodedByte == EQUALS) {
                            // Emit the last (partial) output tuple; expect exactly one more padding
                            // character.
                            output[bytesDecoded++] = (byte) (decodedTuple >> 4);
                            state = 4;
                        } else if (rejectInvalidBytes) {
                            // rejectInvalidBytes also implies decodedByte == OUTSIDE_ALPHABET
                            this.state = 6;
                            byte rejectedByte = input[currentPos - 1];
                            throw new Base64DataException("Invalid third byte of tuple read: " +
                                    (rejectedByte & 0xff) + " (" + (char) rejectedByte + ")");
                        }
                        break;
                    case 3:
                        if (decodedByte >= 0) {
                            // Emit the output triple and return to state 0.
                            decodedTuple = (decodedTuple << 6) | decodedByte;
                            output[bytesDecoded + 2] = (byte) decodedTuple;
                            output[bytesDecoded + 1] = (byte) (decodedTuple >> 8);
                            output[bytesDecoded] = (byte) (decodedTuple >> 16);
                            bytesDecoded += 3;
                            state = 0;
                        } else if (decodedByte == EQUALS) {
                            // Emit the last (partial) output tuple; expect no further data or
                            // padding characters.
                            output[bytesDecoded + 1] = (byte) (decodedTuple >> 2);
                            output[bytesDecoded] = (byte) (decodedTuple >> 10);
                            bytesDecoded += 2;
                            state = 5;
                        } else if (rejectInvalidBytes) {
                            // rejectInvalidBytes also decodedByte == OUTSIDE_ALPHABET
                            this.state = 6;
                            byte rejectedByte = input[currentPos - 1];
                            throw new Base64DataException("Invalid fourth byte of tuple read: " +
                                    (rejectedByte & 0xff) + " (" + (char) rejectedByte + ")");
                        }
                        break;
                    case 4:
                        // Unexpected bytes inside padding are treated differently:
                        // If we are to rejectInvalidBytes only EQUALS is accepted.
                        // If we are not to rejectInvalidBytes, only alphabet bytes cause an error.
                        if ((rejectInvalidBytes && decodedByte != EQUALS)
                                || (!rejectInvalidBytes && decodedByte >= 0)) {
                            this.state = 6;
                            byte rejectedByte = input[currentPos - 1];
                            throw new Base64DataException("Expected final \'=\' but read " +
                                    (rejectedByte & 0xff) + " (" + (char) rejectedByte + ")");
                        }
                        // If the byte is EQUALS we can move to the next state. Any other byte read
                        // must have been an OUTSIDE_ALPHABET and is therefore skipped.
                        if (decodedByte == EQUALS) {
                            ++state;
                        }
                        break;

                    case 5:
                        // Trailing bytes after padding are treated differently:
                        // If (rejectInvalidBytes): all trailing bytes are invalid.
                        // If (!rejectInvalidBytes): only alphabet bytes cause an error.
                        if (rejectInvalidBytes || (!rejectInvalidBytes && decodedByte >= 0)) {
                            this.state = 6;
                            byte rejectedByte = input[currentPos - 1];
                            throw new Base64DataException("Unexpected trailing character: " +
                                    (rejectedByte & 0xff) + " (" + (char) rejectedByte + ")");
                        }
                        break;
                }
            }

            if (!finish) {
                // We're out of input, but a future call could provide more.
                this.state = state;
                this.partiallyDecodedTuple = decodedTuple;
                return bytesDecoded;
            }

            // Done reading input.  Now figure out where we are left in the state machine and finish
            // up.

            switch (state) {
                case 0:
                    // Output length is a multiple of three. Fine.
                    break;
                case 1:
                    // Read one extra input byte, which isn't enough to make another output byte.
                    // Illegal.
                    this.state = 6;
                    throw new Base64DataException(
                            "Input data stopped after a single byte of the expected 2, 3 or 4");
                case 2:
                    // Read two extra input bytes, enough to emit 1 more output byte. Fine.
                    output[bytesDecoded++] = (byte) (decodedTuple >> 4);
                    break;
                case 3:
                    // Read three extra input bytes, enough to emit 2 more output bytes.  Fine.
                    output[bytesDecoded++] = (byte) (decodedTuple >> 10);
                    output[bytesDecoded++] = (byte) (decodedTuple >> 2);
                    break;
                case 4:
                    // Read one padding '=' when we expected 2. Illegal.
                    this.state = 6;
                    throw new Base64DataException(
                            "Input data stopped after one \'=\' where two were expected");
                case 5:
                    // Read all the padding '='s we expected and no more. Fine.
                    break;
            }

            this.state = state;
            return bytesDecoded;
        }

        /**
         * Returns true if the {@code lineSeparator} bytes could be used as a Base64 separator with
         * the specified alphabet. A {@code null} or empty lineSeparator is considered invalid.
         *
         * @param lineSeparator the line separator bytes
         * @param webSafe true to use the "web safe" alphabet (table 2), false to use the basic
         *     (table 1) alphabet
         */
        public static boolean isValidLineSeparator(boolean webSafe, byte[] lineSeparator) {
            if (lineSeparator == null || lineSeparator.length == 0) {
                return false;
            }
            byte[] alphabet = webSafe ? DECODE_WEBSAFE : DECODE;
            for (byte lineSeparatorByte : lineSeparator) {
                if (alphabet[lineSeparatorByte] != OUTSIDE_ALPHABET) {
                    return false;
                }
            }
            return true;
        }
    }

    //  --------------------------------------------------------
    //  encoding
    //  --------------------------------------------------------

    /**
     * Base64-encode the given data and return a newly allocated String with the result.
     * No line wrapping is performed.
     *
     * @param input the data to encode
     */
    public static String encodeToString(byte[] input) {
        Encoder encoder = new Encoder(
                Base64.Encoder.FLAG_DEFAULT, 0 /* no line breaks */, null /* lineBreak */);
        int output_len = encoder.calculateEncodedLength(input.length);
        byte[] output = new byte[output_len];

        int op = encoder.process(input, 0, input.length, output, true);

        assert op == output_len;

        try {
            return new String(output, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // US-ASCII is guaranteed to be available.
            throw new AssertionError(e);
        }
    }

    /**
     * A base64 encoder. See the various FLAG_ constants for configuration. Encoder is not
     * thread-safe. An encoder can be reused by calling {@link #reset()}.
     */
    public static class Encoder extends Coder {
        /**
         * Default values for encoder flags. Default means:
         * <ul>
         *     <li>Use the standard dictionary for base 64 encoding</li>
         *     <li>Pad the encoded bytes as per the RFC</li>
         *     <li>Do not append a trailing line break at the very end</li>
         * </ul>
         */
        public static final int FLAG_DEFAULT = 0;

        /**
         * Encoder flag bit to omit the padding '=' characters at the end
         * of the output (if any).
         */
        public static final int FLAG_NO_PADDING = 1;

        /**
         * Encoder flag bit to indicate using the "URL and
         * filename safe" variant of Base64 (see RFC 3548 section 4) where
         * {@code -} and {@code _} are used in place of {@code +} and
         * {@code /}.
         */
        public static final int FLAG_URL_SAFE = 2;

        /**
         * Encoder flag bit to indicate the output should have a trailing line break appended. The
         * linebreak is only appended if a non-empty encoding is produced.
         */
        public static final int FLAG_APPEND_LINE_BREAK = 4;

        /**
         * Lookup table for turning Base64 alphabet positions (6 bits) into output bytes.
         */
        private static final byte[] ENCODE = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
        };

        /**
         * Lookup table for turning Base64 alphabet positions (6 bits) into output bytes.
         */
        private static final byte[] ENCODE_WEBSAFE = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_',
        };

        private final byte[] alphabet;
        /** The number of tuples to output per line. 0 means no line breaks during encoding, */
        private final int tuplesPerLine;
        /** Whether to end the encoding with a line separator. */
        private final boolean appendLineBreak;
        /** The line break to use. Only valid if tuplesPerLine > 0 or appendLineBreak == true */
        private final byte[] lineBreak;
        private final byte[] tail;
        private final boolean doPadding;

        private int tailLen;
        /** The number of tuples already output on the current line. */
        private int count;

        /**
         * Creates an Encoder with the supplied configuration.
         *
         * <p>lineLength can be 0, meaning no line wrapping, or the number of bytes to be output per
         * line. If lineLength != 0 then lineLength must be exactly divisible by 4.
         * lineSeparator must be non-null and non-empty when lineLength > 0 or the
         * {@link #FLAG_APPEND_LINE_BREAK} flag is set.
         */
        public Encoder(int flags, int lineLength, byte[] lineBreak) {
            if (lineLength < 0) {
                throw new IllegalArgumentException("lineLength must not be negative");
            }
            if (lineLength % 4 != 0) {
                throw new IllegalArgumentException("lineLength must be zero or a multiple of 4");
            }

            appendLineBreak = (flags & FLAG_APPEND_LINE_BREAK) > 0;

            if ((appendLineBreak || lineLength > 0)
                    && (lineBreak == null || lineBreak.length == 0)) {
                throw new IllegalArgumentException("lineSeparator must not be null or empty");
            }

            this.lineBreak = lineBreak;
            tuplesPerLine = lineLength / 4;
            doPadding = (flags & FLAG_NO_PADDING) == 0;
            alphabet = ((flags & FLAG_URL_SAFE) == 0) ? ENCODE : ENCODE_WEBSAFE;

            tail = new byte[2];
            tailLen = 0;
            count = 0;
        }

        /**
         * Reset the encoder's state so that it can be used to encode a new set of bytes.
         */
        public void reset() {
            tailLen = 0;
            count = 0;
        }

        /**
         * Calculates an exact length for the encoded version of a total number of bytes. This
         * method assumes the encoder that len is the total length to be encoded and does not
         * account for any previously encoded bytes, and assumes that padding and line breaks will
         * be added as set by the encoder's configuration. This method should therefore only be used
         * when the total number of bytes to be encoded is known. If incremental encoding is being
         * used then call {@link #maxOutputSize(int)} which will provide an upper bound.
         */
        public int calculateEncodedLength(int len) {
            // Account for the raw encoding.
            int outputLength = len / 3 * 4;

            // Account for the tail of the data and the padding bytes, if any.
            if (doPadding) {
                if (len % 3 > 0) {
                    outputLength += 4;
                }
            } else {
                switch (len % 3) {
                    case 0: break;
                    case 1: outputLength += 2; break;
                    case 2: outputLength += 3; break;
                }
            }

            // Account for the newlines, if any.
            if (len > 0) {
                if (tuplesPerLine > 0) {
                    outputLength += ((len - 1) / (3 * tuplesPerLine))
                            * lineBreak.length;
                }
                if (appendLineBreak) {
                    outputLength += lineBreak.length;
                }
            }

            return outputLength;
        }

        @Override
        public int maxOutputSize(int len) {
            // This initial number includes padding and line breaks. There could be up to 2 bytes
            // in the tail array when process() is called so we add them here for a worst case.
            int length = calculateEncodedLength(len + 2);
            // If incremental encoding is taking place, line breaks are enabled and an initial line
            // has already been output then the line break would also be output.
            if (tuplesPerLine > 0) {
                length += lineBreak.length;
            }
            // Avoid pointlessly small buffers.
            if (length < 10) {
                length += 10;
            }
            return length;
        }

        /**
         * Encode another block of input data.
         *
         * @return the number of bytes successfully encoded
         */
        @Override
        public int process(
                byte[] input, int inputOffset, int inputLength, byte[] output, boolean finish) {
            // Using local variables makes the encoder about 9% faster.
            final byte[] alphabet = this.alphabet;
            int outputCount = 0;
            int count = this.count;

            int pos = inputOffset;
            int endPos = inputLength + inputOffset;
            int tuple = -1;

            // First we need to concatenate the tail of the previous call
            // with any input bytes available now and see if we can empty
            // the tail.

            switch (tailLen) {
                case 0:
                    // There was no tail.
                    break;

                case 1:
                    if (pos + 2 <= endPos) {
                        // A 1-byte tail with at least 2 bytes of
                        // input available now.
                        tuple = ((tail[0] & 0xff) << 16) |
                                ((input[pos++] & 0xff) << 8) |
                                (input[pos++] & 0xff);
                        tailLen = 0;
                    }
                    break;

                case 2:
                    if (pos + 1 <= endPos) {
                        // A 2-byte tail with at least 1 byte of input.
                        tuple = ((tail[0] & 0xff) << 16) |
                                ((tail[1] & 0xff) << 8) |
                                (input[pos++] & 0xff);
                        tailLen = 0;
                    }
                    break;
            }

            if (tuple != -1) {
                // Break the line, if required, before outputting the next tuple.
                if (tuplesPerLine > 0 && count >= tuplesPerLine) {
                    for (byte aLineSeparator : lineBreak) {
                        output[outputCount++] = aLineSeparator;
                    }
                    count = 0;
                }
                output[outputCount++] = alphabet[(tuple >> 18) & 0x3f];
                output[outputCount++] = alphabet[(tuple >> 12) & 0x3f];
                output[outputCount++] = alphabet[(tuple >> 6) & 0x3f];
                output[outputCount++] = alphabet[tuple & 0x3f];
                count++;
            }

            // At this point either there is no tail, or there are fewer
            // than 3 bytes of input available.

            // The main loop, turning 3 input bytes into 4 output bytes on
            // each iteration.
            while (pos + 3 <= endPos) {
                // Break the line, if required, before outputting the next tuple.
                if (tuplesPerLine > 0 && count >= tuplesPerLine) {
                    for (byte aLineSeparator : lineBreak) {
                        output[outputCount++] = aLineSeparator;
                    }
                    count = 0;
                }

                tuple = ((input[pos] & 0xff) << 16) |
                        ((input[pos + 1] & 0xff) << 8) |
                        (input[pos + 2] & 0xff);
                output[outputCount] = alphabet[(tuple >> 18) & 0x3f];
                output[outputCount + 1] = alphabet[(tuple >> 12) & 0x3f];
                output[outputCount + 2] = alphabet[(tuple >> 6) & 0x3f];
                output[outputCount + 3] = alphabet[tuple & 0x3f];
                count++;
                pos += 3;
                outputCount += 4;
            }

            if (finish) {
                // Finish up the tail of the input.  Note that we need to
                // consume any bytes in tail before any bytes
                // remaining in input; there should be at most two bytes
                // total.
                if (pos - tailLen == endPos - 1) {
                    int t = 0;
                    tuple = ((tailLen > 0 ? tail[t++] : input[pos++]) & 0xff) << 4;
                    tailLen -= t;
                    // Break the line, if required, before outputting the next tuple.
                    if (tuplesPerLine > 0 && count >= tuplesPerLine) {
                        for (byte aLineSeparator : lineBreak) {
                            output[outputCount++] = aLineSeparator;
                        }
                        count = 0;
                    }
                    output[outputCount++] = alphabet[(tuple >> 6) & 0x3f];
                    output[outputCount++] = alphabet[tuple & 0x3f];
                    if (doPadding) {
                        output[outputCount++] = '=';
                        output[outputCount++] = '=';
                    }
                    count++;
                } else if (pos - tailLen == endPos - 2) {
                    int t = 0;
                    tuple = (((tailLen > 1 ? tail[t++] : input[pos++]) & 0xff) << 10) |
                            (((tailLen > 0 ? tail[t++] : input[pos++]) & 0xff) << 2);
                    tailLen -= t;
                    // Break the line, if required, before outputting the next tuple.
                    if (tuplesPerLine > 0 && count >= tuplesPerLine) {
                        for (byte aLineSeparator : lineBreak) {
                            output[outputCount++] = aLineSeparator;
                        }
                        count = 0;
                    }
                    output[outputCount++] = alphabet[(tuple >> 12) & 0x3f];
                    output[outputCount++] = alphabet[(tuple >> 6) & 0x3f];
                    output[outputCount++] = alphabet[tuple & 0x3f];
                    if (doPadding) {
                        output[outputCount++] = '=';
                    }
                    count++;
                }

                if (appendLineBreak && outputCount > 0) {
                    for (byte aLineSeparator : lineBreak) {
                        output[outputCount++] = aLineSeparator;
                    }
                }

                assert tailLen == 0;
                assert pos == endPos;
            } else {
                // Save the leftovers in tail to be consumed on the next
                // call to process.

                if (pos == endPos - 1) {
                    tail[tailLen++] = input[pos];
                } else if (pos == endPos - 2) {
                    tail[tailLen++] = input[pos];
                    tail[tailLen++] = input[pos + 1];
                }
            }

            this.count = count;

            return outputCount;
        }

        /**
         * Returns the number of processed but un-encoded bytes currently buffered by the encoder.
         */
        public int getTailLength() {
            return tailLen;
        }
    }

    private Base64() { }   // don't instantiate
}
