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

package libcore.java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import tests.io.MockOs;

import static libcore.io.OsConstants.*;

public class SocketChannelTest extends junit.framework.TestCase {

    private final MockOs mockOs = new MockOs();

    @Override
    public void setUp() throws Exception {
        mockOs.install();
    }

    @Override
    protected void tearDown() throws Exception {
        mockOs.uninstall();
    }

    public void test_read_intoReadOnlyByteArrays() throws Exception {
        ByteBuffer readOnly = ByteBuffer.allocate(1).asReadOnlyBuffer();
        ServerSocket ss = new ServerSocket(0);
        ss.setReuseAddress(true);
        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
        try {
            sc.read(readOnly);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            sc.read(new ByteBuffer[]{readOnly});
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            sc.read(new ByteBuffer[] { readOnly }, 0, 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_56684() throws Exception {
        mockOs.enqueueFault("connect", ENETUNREACH);

        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);

        Selector selector = Selector.open();
        SelectionKey selectionKey = sc.register(selector, SelectionKey.OP_CONNECT);

        try {
            sc.connect(new InetSocketAddress(
                    InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 }), 0));
            fail();
        } catch (ConnectException ex) {
        }

        try {
            sc.finishConnect();
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_channelSocketOutputStreamClosureState() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
        sc.configureBlocking(true);

        Socket scSocket = sc.socket();
        OutputStream os = scSocket.getOutputStream();

        assertTrue(sc.isOpen());
        assertFalse(scSocket.isClosed());

        os.close();

        assertFalse(sc.isOpen());
        assertTrue(scSocket.isClosed());

        ss.close();
    }

    public void test_channelSocketInputStreamClosureState() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
        sc.configureBlocking(true);

        Socket scSocket = sc.socket();
        InputStream is = scSocket.getInputStream();

        assertTrue(sc.isOpen());
        assertFalse(scSocket.isClosed());

        is.close();

        assertFalse(sc.isOpen());
        assertTrue(scSocket.isClosed());

        ss.close();
    }

    public void test_open_initialState() throws Exception {
        SocketChannel sc = SocketChannel.open();
        try {
            assertNull(sc.getLocalAddress());

            Socket socket = sc.socket();
            assertFalse(socket.isBound());
            assertFalse(socket.isClosed());
            assertFalse(socket.isConnected());
            assertEquals(-1, socket.getLocalPort());
            assertTrue(socket.getLocalAddress().isAnyLocalAddress());
            assertNull(socket.getLocalSocketAddress());
            assertNull(socket.getInetAddress());
            assertEquals(0, socket.getPort());
            assertNull(socket.getRemoteSocketAddress());
            assertFalse(socket.getReuseAddress());

            assertSame(sc, socket.getChannel());
        } finally {
            sc.close();
        }
    }

    public void test_bind_socketStateSync() throws IOException {
        SocketChannel sc = SocketChannel.open();
        assertNull(sc.getLocalAddress());

        Socket socket = sc.socket();
        assertNull(socket.getLocalSocketAddress());
        assertFalse(socket.isBound());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        sc.bind(bindAddr);

        InetSocketAddress actualAddr = (InetSocketAddress) sc.getLocalAddress();
        assertEquals(actualAddr, socket.getLocalSocketAddress());
        assertEquals(bindAddr.getHostName(), actualAddr.getHostName());
        assertTrue(socket.isBound());
        assertFalse(socket.isConnected());
        assertFalse(socket.isClosed());

        sc.close();

        assertFalse(sc.isOpen());
        assertTrue(socket.isClosed());
    }

    public void test_bind_socketObjectCreationAfterBind() throws IOException {
        SocketChannel sc = SocketChannel.open();
        assertNull(sc.getLocalAddress());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        sc.bind(bindAddr);

        // Socket object creation after bind().
        Socket socket = sc.socket();
        InetSocketAddress actualAddr = (InetSocketAddress) sc.getLocalAddress();
        assertEquals(actualAddr, socket.getLocalSocketAddress());
        assertEquals(bindAddr.getHostName(), actualAddr.getHostName());
        assertTrue(socket.isBound());
        assertFalse(socket.isConnected());
        assertFalse(socket.isClosed());

        sc.close();

        assertFalse(sc.isOpen());
        assertTrue(socket.isClosed());
    }

    public void test_connect_blocking() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open();
        assertTrue(sc.isBlocking());

        assertTrue(sc.connect(ss.getLocalSocketAddress()));

        assertTrue(sc.socket().isBound());
        assertTrue(sc.isConnected());
        assertTrue(sc.socket().isConnected());
        assertFalse(sc.socket().isClosed());
        assertTrue(sc.isBlocking());

        ss.close();
        sc.close();
    }

    public void test_connect_nonBlocking() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open();
        assertTrue(sc.isBlocking());
        sc.configureBlocking(false);
        assertFalse(sc.isBlocking());

        if (!sc.connect(ss.getLocalSocketAddress())) {
            do {
                assertTrue(sc.socket().isBound());
                assertFalse(sc.isConnected());
                assertFalse(sc.socket().isConnected());
                assertFalse(sc.socket().isClosed());
            } while (!sc.finishConnect());
        }
        assertTrue(sc.socket().isBound());
        assertTrue(sc.isConnected());
        assertTrue(sc.socket().isConnected());
        assertFalse(sc.socket().isClosed());
        assertFalse(sc.isBlocking());

        ss.close();
        sc.close();
    }
}
