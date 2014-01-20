/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import junit.framework.TestCase;

/*
 * test for ServerSocketChannel
 */
public class ServerSocketChannelTest extends TestCase {

    private static final int CAPACITY_NORMAL = 200;

    private static final int CAPACITY_64KB = 65536;

    private static final int TIME_UNIT = 200;

    private ServerSocketChannel serverChannel;

    private SocketChannel clientChannel;

    protected void setUp() throws Exception {
        super.setUp();
        this.serverChannel = ServerSocketChannel.open();
        this.clientChannel = SocketChannel.open();
    }

    protected void tearDown() throws Exception {
        if (null != this.serverChannel) {
            try {
                this.serverChannel.close();
            } catch (Exception e) {
                //ignore
            }

        }
        if (null != this.clientChannel) {
            try {
                this.clientChannel.close();
            } catch (Exception e) {
                //ignore
            }
        }
        super.tearDown();
    }

    // -------------------------------------------------------------------
    // Test for methods in abstract class.
    // -------------------------------------------------------------------

    /*
     * Test method for 'java.nio.channels.ServerSocketChannel.validOps()'
     */
    public void testValidOps() {
        MockServerSocketChannel testMSChnlnull = new MockServerSocketChannel(
                null);
        MockServerSocketChannel testMSChnl = new MockServerSocketChannel(
                SelectorProvider.provider());
        assertEquals(SelectionKey.OP_ACCEPT, this.serverChannel.validOps());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnl.validOps());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnlnull.validOps());

    }

    /*
     * Test method for 'java.nio.channels.ServerSocketChannel.open()'
     */
    public void testOpen() {
        MockServerSocketChannel testMSChnl = new MockServerSocketChannel(null);
        MockServerSocketChannel testMSChnlnotnull = new MockServerSocketChannel(
                SelectorProvider.provider());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnlnotnull.validOps());
        assertNull(testMSChnl.provider());
        assertNotNull(testMSChnlnotnull.provider());
        assertNotNull(this.serverChannel.provider());
        assertEquals(testMSChnlnotnull.provider(), this.serverChannel
                .provider());
    }

    // -------------------------------------------------------------------
    // Tests for bind()
    // -------------------------------------------------------------------

    public void test_bind_null() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            assertNull(ssc.getLocalAddress());

            ssc.bind(null);

            InetSocketAddress localAddress = (InetSocketAddress) ssc.getLocalAddress();
            assertTrue(localAddress.getAddress().isAnyLocalAddress());
            assertTrue(localAddress.getPort() > 0);
        } finally {
            ssc.close();
        }
    }

    public void test_bind_failure() throws Exception {
        ServerSocketChannel portHog = ServerSocketChannel.open();
        portHog.bind(null);

        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            // Bind to a local address that is in use
            ssc.bind(portHog.getLocalAddress());
            fail();
        } catch (IOException expected) {
        } finally {
            ssc.close();
            portHog.close();
        }
    }

    public void test_bind_closed() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.close();

        try {
            ssc.bind(null);
            fail();
        } catch (ClosedChannelException expected) {
        } finally {
            ssc.close();
        }
    }

    public void test_bind_twice() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(null);

        try {
            ssc.bind(null);
            fail();
        } catch (AlreadyBoundException expected) {
        } finally {
            ssc.close();
        }
    }

    public void test_bind_explicitPort() throws Exception {
        ServerSocketChannel portPickingChannel = ServerSocketChannel.open();
        // Have the OS find a free port.
        portPickingChannel.bind(null);

        InetSocketAddress address = (InetSocketAddress) portPickingChannel.getLocalAddress();
        assertTrue(address.getPort() > 0);
        portPickingChannel.close();

        // There is a risk of flakiness here if the port is allocated to something else between
        // close() and bind().
        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress bindAddress = new InetSocketAddress("localhost", address.getPort());
        ssc.bind(bindAddress);

        InetSocketAddress boundAddress = (InetSocketAddress) ssc.getLocalAddress();
        assertEquals(bindAddress.getHostName(), boundAddress.getHostName());
        assertEquals(bindAddress.getPort(), boundAddress.getPort());

        ssc.close();
    }

    public void test_bind_socketSync() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        assertNull(ssc.getLocalAddress());

        ServerSocket socket = ssc.socket();
        assertNull(socket.getLocalSocketAddress());
        assertFalse(socket.isBound());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        ssc.bind(bindAddr);

        InetSocketAddress actualAddr = (InetSocketAddress) ssc.getLocalAddress();
        assertEquals(actualAddr, socket.getLocalSocketAddress());
        assertEquals(bindAddr.getHostName(), actualAddr.getHostName());
        assertTrue(socket.isBound());
        assertFalse(socket.isClosed());

        ssc.close();

        assertFalse(ssc.isOpen());
        assertTrue(socket.isClosed());
    }

    public void test_bind_socketSyncAfterBind() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        assertNull(ssc.getLocalAddress());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        ssc.bind(bindAddr);

        // Socket creation after bind().
        ServerSocket socket = ssc.socket();
        InetSocketAddress actualAddr = (InetSocketAddress) ssc.getLocalAddress();
        assertEquals(actualAddr, socket.getLocalSocketAddress());
        assertEquals(bindAddr.getHostName(), actualAddr.getHostName());
        assertTrue(socket.isBound());
        assertFalse(socket.isClosed());

        ssc.close();

        assertFalse(ssc.isOpen());
        assertTrue(socket.isClosed());
    }

    // -------------------------------------------------------------------
    // Test for getLocalSocketAddress()
    // -------------------------------------------------------------------

    public void test_getLocalSocketAddress_afterClose() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        assertNull(ssc.getLocalAddress());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        ssc.bind(bindAddr);

        assertNotNull(ssc.getLocalAddress());

        ssc.close();

        assertFalse(ssc.isOpen());

        try {
            ssc.getLocalAddress();
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    // -------------------------------------------------------------------
    // Test for socket()
    // -------------------------------------------------------------------

    /*
     * Test method for 'java.nio.channels.ServerSocketChannel.socket()'
     */
    public void testSocket_Block_BeforeClose() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket s1 = this.serverChannel.socket();
        assertFalse(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        // same
        assertSame(s1, s2);

        // socket close makes the channel close
        s1.close();
        assertFalse(this.serverChannel.isOpen());

    }

    public void testSocket_NonBlock_BeforeClose() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.configureBlocking(false);
        ServerSocket s1 = this.serverChannel.socket();
        assertFalse(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        // same
        assertSame(s1, s2);

        // socket close makes the channel close
        s1.close();
        assertFalse(this.serverChannel.isOpen());

    }

    public void testSocket_Block_Closed() throws Exception {
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket s1 = this.serverChannel.socket();
        assertTrue(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        // same
        assertSame(s1, s2);
    }

    public void testSocket_NonBlock_Closed() throws Exception {
        this.serverChannel.configureBlocking(false);
        this.serverChannel.close();
        assertFalse(this.serverChannel.isBlocking());
        assertFalse(this.serverChannel.isOpen());
        ServerSocket s1 = this.serverChannel.socket();
        assertTrue(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        // same
        assertSame(s1, s2);
    }

    private void assertSocketNotAccepted(ServerSocket s) throws IOException {
        assertFalse(s.isBound());
        assertNull(s.getInetAddress());
        assertEquals(-1, s.getLocalPort());
        assertNull(s.getLocalSocketAddress());
        try {
            assertEquals(0, s.getSoTimeout());
        } catch (IOException expected) {
            // Android doesn't cache the timeout, so the getsockopt(2) fails and throws.
        }
    }

    public void testChannelBasicStatus() {
        ServerSocket gotSocket = this.serverChannel.socket();
        assertFalse(gotSocket.isClosed());
        assertTrue(this.serverChannel.isBlocking());
        assertFalse(this.serverChannel.isRegistered());
        assertEquals(SelectionKey.OP_ACCEPT, this.serverChannel.validOps());
        assertEquals(SelectorProvider.provider(), this.serverChannel.provider());
    }

    // -------------------------------------------------------------------
    // Test for accept()
    // -------------------------------------------------------------------

    /*
     * Test method for 'java.nio.channels.ServerSocketChannel.accept()'
     */

    public void testAccept_Block_NotYetBound() throws IOException {
        assertTrue(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        try {
            this.serverChannel.accept();
            fail("Should throw NotYetBoundException");
        } catch (NotYetBoundException e) {
            // correct
        }
    }

    public void testAccept_NonBlock_NotYetBound() throws IOException {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.configureBlocking(false);
        try {
            this.serverChannel.accept();
            fail("Should throw NotYetBoundException");
        } catch (NotYetBoundException e) {
            // correct
        }
    }

    public void testAccept_ClosedChannel() throws Exception {
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
        try {
            this.serverChannel.accept();
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // OK.
        }
    }

    public void testAccept_Block_NoConnect() throws IOException {
        assertTrue(this.serverChannel.isBlocking());
        serverChannel.bind(null);
        // blocking mode , will block and wait for ever...
        // so must close the server channel with another thread.
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    ServerSocketChannelTest.this.serverChannel.close();
                } catch (Exception e) {
                    fail("Fail to close the server channel because of"
                            + e.getClass().getName());
                }
            }
        }.start();
        try {
            this.serverChannel.accept();
            fail("Should throw a AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
            // OK.
        }
    }

    public void testAccept_NonBlock_NoConnect() throws IOException {
        this.serverChannel.bind(null);
        this.serverChannel.configureBlocking(false);
        // non-blocking mode , will immediately return
        assertNull(this.serverChannel.accept());
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_Blocking_RealData() throws IOException {
        serverChannel.bind(null);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);

        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            buf.put((byte) i);
        }
        clientChannel.connect(serverChannel.getLocalAddress());
        Socket serverSocket = serverChannel.accept().socket();
        InputStream in = serverSocket.getInputStream();
        buf.flip();
        clientChannel.write(buf);
        clientChannel.close();
        assertReadResult(in,CAPACITY_NORMAL);
    }

    /**
     * Asserts read content. The read content should contain <code>size</code>
     * bytes, and the value should be a sequence from 0 to size-1
     * ([0,1,...size-1]). Otherwise, the method throws Exception.
     *
     */
    private void assertReadResult(InputStream in, int size) throws IOException{
        byte[] readContent = new byte[size + 1];
        int count = 0;
        int total = 0;
        while ((count = in.read(readContent, total, size + 1 - total)) != -1) {
            total = total + count;
        }
        assertEquals(size, total);
        for (int i = 0; i < size; i++) {
            assertEquals((byte) i, readContent[i]);
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_NonBlocking_RealData() throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.bind(null);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(serverChannel.getLocalAddress());
        Socket serverSocket = serverChannel.accept().socket();
        InputStream in = serverSocket.getInputStream();
        clientChannel.write(buf);
        clientChannel.close();
        assertReadResult(in,CAPACITY_NORMAL);
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_Blocking_RealData() throws IOException {
        assertTrue(serverChannel.isBlocking());
        serverChannel.bind(null);

        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(serverChannel.getLocalAddress());
        Socket socket = serverChannel.accept().socket();
        OutputStream out = socket.getOutputStream();
        out.write(writeContent);
        out.flush();
        socket.close();
        assertWriteResult(CAPACITY_NORMAL);
    }


    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_NonBlocking_RealData() throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.bind(null);

        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(serverChannel.getLocalAddress());
        Socket clientSocket = serverChannel.accept().socket();
        OutputStream out = clientSocket.getOutputStream();
        out.write(writeContent);
        clientSocket.close();
        assertWriteResult(CAPACITY_NORMAL);
    }

    /**
     * @throws InterruptedException
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_LByteBuffer_Blocking_ReadWriteRealLargeData()
            throws IOException, InterruptedException {
        serverChannel.bind(null);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_64KB);
        for (int i = 0; i < CAPACITY_64KB; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(serverChannel.getLocalAddress());
        WriteChannelThread writeThread = new WriteChannelThread(clientChannel, buf);
        writeThread.start();
        Socket socket = serverChannel.accept().socket();
        InputStream in = socket.getInputStream();
        assertReadResult(in,CAPACITY_64KB);
        writeThread.join();
        // check if the thread threw any exceptions
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    class WriteChannelThread extends Thread {
        SocketChannel channel;
        ByteBuffer buffer;
        IOException exception;

        public WriteChannelThread(SocketChannel channel, ByteBuffer buffer) {
            this.channel = channel;
            this.buffer = buffer;
        }

        public void run() {
            try {
                channel.write(buffer);
                channel.close();
            } catch (IOException e) {
                exception = e;
            }
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_LByteBuffer_NonBlocking_ReadWriteRealLargeData()
            throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.bind(null);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_64KB);
        for (int i = 0; i < CAPACITY_64KB; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(serverChannel.getLocalAddress());
        WriteChannelThread writeThread = new WriteChannelThread(clientChannel, buf);
        writeThread.start();
        Socket socket = serverChannel.accept().socket();
        InputStream in = socket.getInputStream();
        assertReadResult(in,CAPACITY_64KB);
        writeThread.join();
        // check if the thread threw any exceptions
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_LByteBuffer_NonBlocking_ReadWriteRealLargeData()
            throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.bind(null);
        byte[] writeContent = new byte[CAPACITY_64KB];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(serverChannel.getLocalAddress());
        Socket socket = serverChannel.accept().socket();
        WriteSocketThread writeThread = new WriteSocketThread(socket, writeContent);
        writeThread.start();
        assertWriteResult(CAPACITY_64KB);
        writeThread.join();
        // check if the thread threw any exceptions
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    class WriteSocketThread extends Thread {
        Socket socket;
        byte[] buffer;
        IOException exception;

        public WriteSocketThread(Socket socket, byte[] buffer) {
            this.socket = socket;
            this.buffer = buffer;
        }

        public void run() {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(buffer);
                socket.close();
            } catch (IOException e) {
                exception = e;
            }
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_LByteBuffer_Blocking_ReadWriteRealLargeData()
            throws Exception {
        serverChannel.bind(null);
        byte[] writeContent = new byte[CAPACITY_64KB];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(serverChannel.getLocalAddress());
        Socket socket = serverChannel.accept().socket();
        WriteSocketThread writeThread = new WriteSocketThread(socket, writeContent);
        writeThread.start();
        assertWriteResult(CAPACITY_64KB);
        writeThread.join();
        // check if the thread threw any exceptions
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    /**
     * Uses SocketChannel.read(ByteBuffer) to verify write result.
     */
    private void assertWriteResult(int size) throws IOException{
        ByteBuffer buf = ByteBuffer.allocate(size + 1);
        int count = 0;
        int total = 0;
        long beginTime = System.currentTimeMillis();
        while ((count = clientChannel.read(buf)) != -1) {
            total = total + count;
            // 10s timeout to avoid dead loop
            if (System.currentTimeMillis() - beginTime > 10000){
                break;
            }
        }
        assertEquals(total, size);
        buf.flip();
        for (int i = 0; i < count; i++) {
            assertEquals((byte) i, buf.get(i));
        }
    }

    /**
     * @tests ServerSocketChannel#socket().getSoTimeout()
     */
    public void test_accept_SOTIMEOUT() throws IOException {
        // regression test for Harmony-707
        final int SO_TIMEOUT = 10;
        ServerSocketChannel sc = ServerSocketChannel.open();
        try {
            sc.bind(null);
            sc.configureBlocking(false);
            ServerSocket ss = sc.socket();
            ss.setSoTimeout(SO_TIMEOUT);
            SocketChannel client = sc.accept();
            // non blocking mode, returns null since there are no pending connections.
            assertNull(client);
            int soTimeout = ss.getSoTimeout();
            // Harmony fails here.
            assertEquals(SO_TIMEOUT, soTimeout);
        } finally {
            sc.close();
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Blocking_NotBound() throws IOException {
        // regression test for Harmony-748
        ServerSocket gotSocket = serverChannel.socket();
        serverChannel.configureBlocking(true);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException expected) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException expected) {
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Nonblocking_NotBound() throws IOException {
        // regression test for Harmony-748
        ServerSocket gotSocket = serverChannel.socket();
        serverChannel.configureBlocking(false);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException expected) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException expected) {
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Nonblocking_Bound() throws IOException {
        // regression test for Harmony-748
        serverChannel.configureBlocking(false);
        serverChannel.bind(null);
        ServerSocket gotSocket = serverChannel.socket();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException expected) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw a ClosedChannelException");
        } catch (ClosedChannelException expected) {
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Blocking_Bound() throws IOException {
        // regression test for Harmony-748
        serverChannel.configureBlocking(true);
        serverChannel.bind(null);
        serverChannel.close();
        try {
            serverChannel.socket().accept();
            fail("Should throw a ClosedChannelException");
        } catch (ClosedChannelException expected) {
        }
    }
    /**
     * Regression test for HARMONY-4961
     */
    public void test_socket_getLocalPort() throws IOException {
        serverChannel.bind(null);
        clientChannel.connect(serverChannel.getLocalAddress());
        SocketChannel myChannel = serverChannel.accept();
        int port = myChannel.socket().getLocalPort();
        assertEquals(serverChannel.socket().getLocalPort(), port);
        myChannel.close();
        clientChannel.close();
        serverChannel.close();
    }

    /**
     * Regression test for HARMONY-6375
     */
    public void test_accept_configureBlocking() throws Exception {
        InetSocketAddress localAddr = new InetSocketAddress("localhost", 0);
        serverChannel.bind(localAddr);

        // configure the channel non-blocking
        // when it is accepting in main thread
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    serverChannel.configureBlocking(false);
                    serverChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        try {
            serverChannel.accept();
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException expected) {
        }
        serverChannel.close();
    }
}
