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

public class ReadOnlyDirectByteBufferTest extends DirectByteBufferTest {

    protected void setUp() throws Exception {
        super.setUp();
        buf.put((byte)'a');
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test_values() throws Exception {
        DirectByteBuffer dirBuf = (DirectByteBuffer) DirectByteBuffer.allocateDirect(10);
        dirBuf.put((byte) 'a');
        if (dirBuf.order() == ByteOrder.BIG_ENDIAN) {
            dirBuf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            dirBuf.order(ByteOrder.BIG_ENDIAN);
        }
        DirectByteBuffer dupDirBuf = (DirectByteBuffer) dirBuf.asReadOnlyBuffer();
        assertEquals(dirBuf.address(), dupDirBuf.address());
        assertEquals(dirBuf.order(), dupDirBuf.order());
        assertEquals(dirBuf.position(), dupDirBuf.position());
        assertEquals(dirBuf.mark(), dupDirBuf.mark());
        assertTrue(dupDirBuf.isReadOnly());
        assertEquals(dirBuf.capacity(), dupDirBuf.capacity());
    }
    
    public void testIsReadOnly() {
        assertTrue(buf.isReadOnly());
    }
    
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }
    
    public void testHashCode() {
        super.readOnlyHashCode();
    }

}
