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
 * limitations under the License.
 */

package benchmarks.regression;

import com.google.caliper.SimpleBenchmark;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class CharsetDecoderBenchmark extends SimpleBenchmark {

    private static byte[] simpleAscii = createAsciiByteSequence(2 << 14);
    private static byte[] twoByteUnicode = createtwoByteCharByteSequence(2 << 14);
    private static byte[] threeByteUnicode = createthreeByteCharByteSequence(2 << 14);
    private static byte[] fourByteUnicode = createFourByteCharByteSequence(2 << 14);


    private static byte[] createAsciiByteSequence(int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i += 4) {
            result[i]   = "A".getBytes()[0];
            result[i+1] = "a".getBytes()[0];
            result[i+2] = "z".getBytes()[0];
            result[i+3] = "Z".getBytes()[0];
        }
        return result;
    }

    private static byte[] createtwoByteCharByteSequence(int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i += 4) {
            result[i] = "ü".getBytes()[0];
            result[i+1] = "ü".getBytes()[1];
            result[i+2] = "Ü".getBytes()[0];
            result[i+3] = "Ü".getBytes()[1];
        }
        return result;
    }

    private static byte[] createthreeByteCharByteSequence(int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i += 4) {
            result[i] = "€".getBytes()[0];
            result[i+1] = "€".getBytes()[1];
            result[i+2] = "€".getBytes()[2];
            result[i+3] = "a".getBytes()[0];  // Pad with an ascii character.
        }
        return result;
    }

    private static byte[] createFourByteCharByteSequence(int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i += 4) {
            result[i] = "\uD800\uDF48".getBytes()[0];
            result[i+1] = "\uD800\uDF48".getBytes()[1];
            result[i+2] = "\uD800\uDF48".getBytes()[2];
            result[i+3] = "\uD800\uDF48".getBytes()[3];
        }
        return result;
    }

    private static final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

    public void time_simpleAscii(int reps) throws CharacterCodingException {
        for (int i = 0; i < reps; i++) {
            decoder.decode(ByteBuffer.wrap(simpleAscii));
        }
    }

    public void time_twoByteUnicode(int reps) throws CharacterCodingException {
        for (int i = 0; i < reps; i++) {
            decoder.decode(ByteBuffer.wrap(twoByteUnicode));
        }
    }

    public void time_threeByteUnicode(int reps) throws CharacterCodingException {
        for (int i = 0; i < reps; i++) {
            decoder.decode(ByteBuffer.wrap(threeByteUnicode));
        }
    }

    public void time_fourByteUnicode(int reps) throws CharacterCodingException {
        for (int i = 0; i < reps; i++) {
            decoder.decode(ByteBuffer.wrap(fourByteUnicode));
        }
    }
}
