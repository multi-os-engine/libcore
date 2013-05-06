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

// partial copy of libcore.io.Streams

package org.conscrypt.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Streams {
    private Streams() {}

    /**
     * Implements InputStream.read(int) in terms of InputStream.read(byte[], int, int).
     * InputStream assumes that you implement InputStream.read(int) and provides default
     * implementations of the others, but often the opposite is more efficient.
     */
    public static int readSingleByte(InputStream in) throws IOException {
        byte[] buffer = new byte[1];
        int result = in.read(buffer, 0, 1);
        return (result != -1) ? buffer[0] & 0xff : -1;
    }

    /**
     * Implements OutputStream.write(int) in terms of OutputStream.write(byte[], int, int).
     * OutputStream assumes that you implement OutputStream.write(int) and provides default
     * implementations of the others, but often the opposite is more efficient.
     */
    public static void writeSingleByte(OutputStream out, int b) throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte) (b & 0xff);
        out.write(buffer);
    }

    /**
     * Fills 'dst' with bytes from 'in', throwing EOFException if insufficient bytes are available.
     */
    public static void readFully(InputStream in, byte[] dst) throws IOException {
        readFully(in, dst, 0, dst.length);
    }

    /**
     * Reads exactly 'byteCount' bytes from 'in' (into 'dst' at offset 'offset'), and throws
     * EOFException if insufficient bytes are available.
     *
     * Used to implement {@link java.io.DataInputStream#readFully(byte[], int, int)}.
     */
    public static void readFully(InputStream in, byte[] dst, int offset, int byteCount) throws IOException {
        if (byteCount == 0) {
            return;
        }
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        if (dst == null) {
            throw new NullPointerException("dst == null");
        }
        Arrays.checkOffsetAndCount(dst.length, offset, byteCount);
        while (byteCount > 0) {
            int bytesRead = in.read(dst, offset, byteCount);
            if (bytesRead < 0) {
                throw new EOFException();
            }
            offset += bytesRead;
            byteCount -= bytesRead;
        }
    }
}
