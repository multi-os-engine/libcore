/*
 * Copyright (C) 2016 The Android Open Source Project
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

package libcore.java.nio.channels;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsynchronousSocketChannelTest extends junit.framework.TestCase {
    ByteBuffer allocateByteBuffer(int size, boolean direct) {
        ByteBuffer bb = direct ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
        for (int i = 0;i < size; ++i) {
            bb.put(i, (byte)i);
        }
        return bb;
    }

    public void test_connect() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        assertNotNull(asc.provider());
        assertTrue(asc.isOpen());
        assertNull(asc.getRemoteAddress());
        assertNull(asc.getLocalAddress());

        // Connect
        Future<Void> connectFuture = asc.connect(ss.getLocalSocketAddress());
        connectFuture.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(asc.getRemoteAddress());
        assertNotNull(asc.getLocalAddress());
        assertTrue(asc.isOpen());

        asc.close();
        ss.close();
    }

    public void test_bind_unresolvedAddress() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        try {
            asc.bind(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (UnresolvedAddressException expected) {}

        assertNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        try {
            asc.bind(ss.getLocalSocketAddress());
            fail();
        } catch (BindException expected) {}

        ss.close();
        asc.close();
    }

    public void test_bind_npe() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();
        asc.bind(null);
        asc.close();
    }

    public void test_connect_unresolvedAddress() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        try {
            asc.connect(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (UnresolvedAddressException expected) {}

        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());
        asc.close();
    }

    public void test_close() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();
        assertTrue(asc.isOpen());

        asc.close();

        try {
            asc.getRemoteAddress();
            fail();
        } catch (ClosedChannelException expected) {}
        try {
            asc.getLocalAddress();
            fail();
        } catch (ClosedChannelException expected) {}
        assertFalse(asc.isOpen());
    }

    private void test_futureReadWrite(boolean useDirectByteBuffer) throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        Future<Void> connectFuture = asc.connect(ss.getLocalSocketAddress());
        connectFuture.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(asc.getRemoteAddress());

        // Accept & write data
        ByteBuffer sendData = allocateByteBuffer(32, useDirectByteBuffer);
        Socket sss = ss.accept();
        sss.getOutputStream().write(sendData.array(), sendData.arrayOffset(), 32);

        // Read data from async channel and call #get on result future
        ByteBuffer receivedData = allocateByteBuffer(32, useDirectByteBuffer);
        assertEquals(32, (int)asc.read(receivedData).get(1000, TimeUnit.MILLISECONDS));

        // Compare results
        receivedData.flip();
        assertEquals(sendData, receivedData);

        // Write data to async channel and call #get on result future
        assertEquals(32, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));

        // Read data and compare with original
        byte[] readArray = new byte[32];
        assertEquals(32, sss.getInputStream().read(readArray));

        // Compare results
        sendData.flip();
        assertEquals(sendData, ByteBuffer.wrap(readArray));

        asc.close();
        sss.close();
        ss.close();
    }

    public void test_futureReadWrite() throws Throwable {
        test_futureReadWrite(false /* useDirectByteBuffer */);
    }

    public void test_futureReadWrite_DirectByteBuffer() throws Throwable {
        test_futureReadWrite(true /* useDirectByteBuffer */);
    }

    private void test_completionHandlerReadWrite(boolean useDirectByteBuffer) throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler = new FutureLikeCompletionHandler<Void>();
        asc.connect(ss.getLocalSocketAddress(), null,
                    connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());

        // Accept & write data
        ByteBuffer sendData = allocateByteBuffer(32, useDirectByteBuffer);
        Socket sss = ss.accept();
        sss.getOutputStream().write(sendData.array(), sendData.arrayOffset(), 32);

        // Read data from async channel
        ByteBuffer receivedData = allocateByteBuffer(32, useDirectByteBuffer);
        FutureLikeCompletionHandler<Integer> readCompletionHandler = new FutureLikeCompletionHandler<Integer>();
        asc.read(receivedData, null, readCompletionHandler);
        assertEquals(32, (int)readCompletionHandler.get(1000));

        // Compare results
        receivedData.flip();
        assertEquals(sendData, receivedData);

        // Write data to async channel
        FutureLikeCompletionHandler<Integer> writeCompletionHandler = new FutureLikeCompletionHandler<Integer>();
        asc.write(sendData, null, writeCompletionHandler);
        assertEquals(32, (int)writeCompletionHandler.get(1000));

        // Read data and compare with original
        byte[] readArray = new byte[32];
        assertEquals(32, sss.getInputStream().read(readArray));
        sendData.flip();
        assertEquals(sendData, ByteBuffer.wrap(readArray));

        asc.close();
        sss.close();
        ss.close();
    }

    public void test_completionHandlerReadWrite() throws Throwable {
        test_completionHandlerReadWrite(false /* useDirectByteBuffer */);
    }

    public void test_completionHandlerReadWrite_DirectByteBuffer() throws Throwable {
        test_completionHandlerReadWrite(true /* useDirectByteBuffer */);
    }

    public void test_completionHandler_connect_npe() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        FutureLikeCompletionHandler<Void> connectCompletionHandler = new FutureLikeCompletionHandler<Void>();
        // 1st argument NPE
        try {
            asc.connect(null, null, connectCompletionHandler);
            fail();
        } catch(IllegalArgumentException expected) {}

        // 3rd argument NPE
        try {
            asc.connect(ss.getLocalSocketAddress(), null, null);
            fail();
        } catch(NullPointerException expected) {}

        asc.close();
        ss.close();
    }

    public void test_completionHandler_read_npe() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler = new FutureLikeCompletionHandler<Void>();
        asc.connect(ss.getLocalSocketAddress(), null,
                    connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());

        // Read data from async channel
        ByteBuffer receivedData = allocateByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> readCompletionHandler = new FutureLikeCompletionHandler<Integer>();

        // 1st argument NPE
        try {
            asc.read(null, null, readCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // 3rd argument NPE
        try {
            asc.read(receivedData, null, null);
            fail();
        } catch(NullPointerException expected) {}

        asc.close();
        ss.close();
    }

    public void test_shutdown() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        Future<Void> connectFuture = asc.connect(ss.getLocalSocketAddress());
        connectFuture.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(asc.getRemoteAddress());

        // Accept & write data
        ByteBuffer sendData = allocateByteBuffer(32, false);
        Socket sss = ss.accept();
        sss.getOutputStream().write(sendData.array());

        // Shutdown input, expect -1 from read
        asc.shutdownInput();
        ByteBuffer receivedData = allocateByteBuffer(32, false);

        // We did write something into the socket,  #shutdownInput javadocs
        // say that "...effect on an outstanding read operation is system dependent and
        // therefore not specified...". It looks like on android/linux the data in
        // received buffer is discarded.
        assertEquals(-1, (int)asc.read(receivedData).get(1000, TimeUnit.MILLISECONDS));
        assertEquals(-1, (int)asc.read(receivedData).get(1000, TimeUnit.MILLISECONDS));

        // But we can still write!
        assertEquals(32, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));
        byte[] readArray = new byte[32];
        assertEquals(32, sss.getInputStream().read(readArray));
        assertTrue(Arrays.equals(sendData.array(), readArray));

        // Shutdown output, expect ClosedChannelException
        asc.shutdownOutput();
        try {
            assertEquals(-1, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));
            fail();
        } catch(ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }
        try {
            assertEquals(-1, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));
            fail();
        } catch(ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }

        // shutdownInput() & shudownOutput() != closed, shocking!
        assertNotNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.close();
        sss.close();
        ss.close();
    }

    public void test_options() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        asc.setOption(StandardSocketOptions.SO_SNDBUF, 5000);
        assertEquals(5000, (long)asc.getOption(StandardSocketOptions.SO_SNDBUF));

        asc.setOption(StandardSocketOptions.SO_RCVBUF, 5000);
        assertEquals(5000, (long)asc.getOption(StandardSocketOptions.SO_RCVBUF));

        asc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        assertTrue(asc.getOption(StandardSocketOptions.SO_KEEPALIVE));

        asc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        assertTrue(asc.getOption(StandardSocketOptions.SO_REUSEADDR));

        asc.setOption(StandardSocketOptions.TCP_NODELAY, true);
        assertTrue(asc.getOption(StandardSocketOptions.TCP_NODELAY));
    }

    public void test_group() throws Throwable {
        AsynchronousChannelProvider provider =
            AsynchronousChannelProvider.provider();
        AsynchronousChannelGroup group =
            provider.openAsynchronousChannelGroup(2, Executors.defaultThreadFactory());

        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open(group);
        assertEquals(provider, asc.provider());
        asc.close();
    }

}
