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

package java.lang;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * A cheaper ByteArrayOutputStream for internal use. This class is unsynchronized,
 * and returns its internal array if it's the right size. This makes String.getBytes("UTF-8")
 * 10x faster than the baseline non-fast-path implementation instead of 8x faster when using
 * ByteArrayOutputStream. When GC and uncontended synchronization become cheap, we should be
 * able to get rid of this class. In the meantime, if you need to add further API, please try
 * to keep it plug-compatible with ByteArrayOutputStream with an eye to future obsolescence.
 *
 * @hide
 */
public class UnsafeByteSequence {
    private byte[] bytes;
    private int count;

    public UnsafeByteSequence(int initialCapacity) {
        this.bytes = new byte[initialCapacity];
    }

    public int size() {
        return count;
    }

    /**
     * Moves the write pointer back to the beginning of the sequence,
     * but without resizing or reallocating the buffer.
     */
    public void rewind() {
        count = 0;
    }

    /**
     * Special case for copying bytes from a {@link RandomAccessFile} without
     * allocating an intermediate buffer. If the size of this file is less than
     * or equal to the initial capacity of the internal buffer, no additional
     * allocations are necessary.
     *
     * When {@code unknownLength} is false, we assume that the initial capacity
     * of this byte sequence is equal to the length of the file. This class
     * <b>DOES NOT</b> assert that this true (since it costs an additional stat),
     * it's up to the caller to set things up correctly.
     *
     * @return the true length of the file (total number of bytes read).
     */
    public int readFully(RandomAccessFile raf, boolean unknownLength)
            throws IOException {
        while (true) {
            final int capacity = bytes.length;
            while (count < capacity) {
                final int read = raf.read(bytes, count, capacity - count);
                if (read == -1) {
                    return count;
                }
                count += read;
            }

            // If we don't know the length of this file, we need to continue
            // reading until raf.read() returns -1.
            //
            // NOTE: We can get rid of unknownLength at the cost of an additional
            // read() on the RAF or an unnecessary doubling (or copy).
            if (unknownLength) {
                expand(0);
            } else {
                return count;
            }
        }
    }

    public void write(byte[] buffer, int offset, int length) {
        if (count + length >= bytes.length) {
            expand(length);
        }
        System.arraycopy(buffer, offset, bytes, count, length);
        count += length;
    }

    public void write(int b) {
        if (count == bytes.length) {
            expand(0);
        }
        bytes[count++] = (byte) b;
    }

    private void expand(int nextWriteLength) {
        byte[] newBytes = new byte[(count + nextWriteLength) * 2];
        System.arraycopy(bytes, 0, newBytes, 0, count);
        bytes = newBytes;
    }

    @FindBugsSuppressWarnings("EI_EXPOSE_REP")
    public byte[] toByteArray() {
        if (count == bytes.length) {
            return bytes;
        }
        byte[] result = new byte[count];
        System.arraycopy(bytes, 0, result, 0, count);
        return result;
    }

    public String toString(Charset cs) {
        return new String(bytes, 0, count, cs);
    }
}
