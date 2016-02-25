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

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AlreadyBoundException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.net.StandardSocketOptions;
import java.net.ServerSocket;
import java.net.BindException;
import java.nio.channels.UnresolvedAddressException;

public class AsynchronousServerSocketChannelTest extends junit.framework.TestCase {

    public void test_bind() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assertTrue(assc.isOpen());
        assc.bind(new InetSocketAddress(0));
        try {
            assc.bind(new InetSocketAddress(0));
            fail();
        } catch (AlreadyBoundException expected) {}

        assc.close();
        assertFalse(assc.isOpen());
    }

    public void test_bind_null() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assertTrue(assc.isOpen());
        assc.bind(null);
        try {
            assc.bind(null);
            fail();
        } catch (AlreadyBoundException expected) {}

        assc.close();
        assertFalse(assc.isOpen());
    }

    public void test_bind_unresolvedAddress() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        ServerSocket ss = new ServerSocket(0);

        try {
            assc.bind(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (UnresolvedAddressException expected) {}

        assertNull(assc.getLocalAddress());
        assertTrue(assc.isOpen());

        try {
            assc.bind(ss.getLocalSocketAddress());
            fail();
        } catch (BindException expected) {}

        ss.close();
        assc.close();
    }

    public void test_futureAccept() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        Future<AsynchronousSocketChannel> acceptFuture = assc.accept();

        Socket s = new Socket();
        s.connect(assc.getLocalAddress());

        AsynchronousSocketChannel asc = acceptFuture.get(1000, TimeUnit.MILLISECONDS);

        assertTrue(s.isConnected());
        assertNotNull(asc.getLocalAddress());
        assertNotNull(asc.getRemoteAddress());

        asc.close();
        s.close();
        assc.close();
    }

    public void test_completionHandlerAccept() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        FutureLikeCompletionHandler<AsynchronousSocketChannel> acceptCompletionHandler =
            new FutureLikeCompletionHandler<AsynchronousSocketChannel>();

        assc.accept(null, acceptCompletionHandler);

        Socket s = new Socket();
        s.connect(assc.getLocalAddress());
        AsynchronousSocketChannel asc = acceptCompletionHandler.get(1000);

        assertTrue(s.isConnected());
        assertNotNull(asc.getLocalAddress());
        assertNotNull(asc.getRemoteAddress());

        asc.close();
        s.close();
        assc.close();
    }

    public void test_completionHandlerAccept_npe() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        try {
            assc.accept(null, null);
            fail();
        } catch(NullPointerException expected) {}
        assc.close();
    }


    public void test_options() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();

        assc.setOption(StandardSocketOptions.SO_RCVBUF, 5000);
        assertEquals(5000, (long)assc.getOption(StandardSocketOptions.SO_RCVBUF));

        assc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        assertTrue(assc.getOption(StandardSocketOptions.SO_REUSEADDR));
    }
}
