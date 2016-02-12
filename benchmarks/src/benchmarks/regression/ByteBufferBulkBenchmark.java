/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmarks.regression;

import com.google.caliper.Param;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

public class ByteBufferBulkBenchmark {
    @Param({"true", "false"}) private boolean aligned;
    enum MyBufferType {
        DIRECT, HEAP
    }
    @Param private MyBufferType bufferType;

    enum MySize {
        SMALL_4K(4 * 1024),
        LARGE_1M(1 * 1024 * 1024);

        final int size;
        MySize(int size) {
            this.size = size;
        }
    }
    @Param private MySize bufferSize;

    public static ByteBuffer newBuffer(boolean aligned, MyBufferType bufferType, MySize mySize) throws IOException {
        int size = aligned ? mySize.size : mySize.size + 8 + 1;
        ByteBuffer result = null;
        switch (bufferType) {
        case DIRECT:
            result = ByteBuffer.allocateDirect(size);
            break;
        case HEAP:
            result = ByteBuffer.allocate(size);
            break;
        }
        result.position(aligned ? 0 : 1);
        return result;
    }

    public void timeByteBuffer_putDirectByteBuffer(int reps) throws Exception {
        ByteBuffer src = ByteBufferBulkBenchmark.newBuffer(aligned, bufferType, bufferSize);
        ByteBuffer data = ByteBufferBulkBenchmark.newBuffer(true, MyBufferType.DIRECT, bufferSize);
        for (int rep = 0; rep < reps; ++rep) {
            src.position(aligned ? 0 : 1);
            data.position(aligned ? 0 : 1 );
            src.put(data);
        }
    }

    public void timeByteBuffer_putHeapByteBuffer(int reps) throws Exception {
        ByteBuffer src = ByteBufferBulkBenchmark.newBuffer(aligned, bufferType, bufferSize);
        ByteBuffer data = ByteBufferBulkBenchmark.newBuffer(true, MyBufferType.HEAP, bufferSize);
        for (int rep = 0; rep < reps; ++rep) {
            src.position(aligned ? 0 : 1);
            data.position(aligned ? 0 : 1);
            src.put(data);
        }
    }
}
