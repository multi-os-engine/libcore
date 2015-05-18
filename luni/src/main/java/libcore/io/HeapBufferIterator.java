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

package libcore.io;

import java.nio.ByteOrder;
import libcore.io.Memory;

/**
 * Iterates over big- or little-endian bytes in a Java byte[].
 *
 * @hide don't make this public without adding bounds checking.
 */
public final class HeapBufferIterator extends BufferIterator {
    private final byte[] buffer;
    private final int offset;
    private final int byteCount;
    private final ByteOrder order;

    private int position;

    HeapBufferIterator(byte[] buffer, int offset, int byteCount, ByteOrder order) {
        this.buffer = buffer;
        this.offset = offset;
        this.byteCount = byteCount;
        this.order = order;
    }

    public void seek(int offset) {
        position = offset;
    }

    public void skip(int byteCount) {
        position += byteCount;
    }

    public void readByteArray(byte[] dst, int dstOffset, int byteCount) {
        System.arraycopy(buffer, offset + position, dst, dstOffset, byteCount);
        position += byteCount;
    }

    public byte readByte() {
        byte result = buffer[offset + position];
        ++position;
        return result;
    }

    public int readInt() {
        int result = Memory.unsafePeekInt(buffer, offset + position, order.needsSwap);
        position += SizeOf.INT;
        return result;
    }

    public void readIntArray(int[] dst, int dstOffset, int intCount) {
        final int byteCount = intCount * SizeOf.INT;
        Memory.unsafeBulkGet(dst, dstOffset, byteCount, buffer, offset + position, SizeOf.INT, order.needsSwap);
        position += byteCount;
    }

    public short readShort() {
        short result = Memory.unsafePeekShort(buffer, offset + position, order.needsSwap);
        position += SizeOf.SHORT;
        return result;
    }

    /**
     * Returns a new iterator over {@code buffer}, starting at {@code offset} and continuing for
     * {@code byteCount} bytes. Items larger than a byte are interpreted using the given byte order.
     */
    public static BufferIterator iterator(byte[] buffer, int offset, int byteCount, ByteOrder order) {
        return new HeapBufferIterator(buffer, offset, byteCount, order);
    }
}
