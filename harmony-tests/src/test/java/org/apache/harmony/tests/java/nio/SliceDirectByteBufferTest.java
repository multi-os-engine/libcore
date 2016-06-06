/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.nio;


import java.nio.ByteOrder;
import java.nio.DirectByteBuffer;

public class SliceDirectByteBufferTest extends DirectByteBufferTest {

    protected void setUp() throws Exception {
        super.setUp();
        buf.position(1);
        buf = buf.slice();
        baseBuf = buf;
    }

    public void test_values() throws Exception {
        DirectByteBuffer dirBuf = (DirectByteBuffer) DirectByteBuffer.allocateDirect(10);
        dirBuf.put((byte) 'a');
        if (dirBuf.order() == ByteOrder.BIG_ENDIAN) {
            dirBuf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            dirBuf.order(ByteOrder.BIG_ENDIAN);
        }
        DirectByteBuffer dupDirBuf = (DirectByteBuffer) dirBuf.slice();
        assertEquals(dirBuf.address() + 1, dupDirBuf.address());
        assertEquals(dirBuf.array(), dupDirBuf.array());
        assertEquals(dirBuf.order(), dupDirBuf.order());
        assertEquals(0, dupDirBuf.position());
        assertEquals(dirBuf.mark(), dupDirBuf.mark());
        assertEquals(dirBuf.arrayOffset() + 1, dupDirBuf.arrayOffset());
        assertEquals(dirBuf.isReadOnly(), dupDirBuf.isReadOnly());
        assertEquals(dirBuf.capacity() - 1, dupDirBuf.capacity());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
