package java.nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import junit.framework.Test;
import junit.framework.TestSuite;
import tests.support.Support_PortManager;

public class SelectorTest extends junit.framework.TestCase {
    private static final int WAIT_TIME = 100;
    private static final int PORT = Support_PortManager.getNextPort();
    private static final InetSocketAddress LOCAL_ADDRESS = new InetSocketAddress(
            "127.0.0.1", PORT);

    Selector selector;
    ServerSocketChannel ssc;

    protected void setUp() throws Exception {
        super.setUp();
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ServerSocket ss = ssc.socket();
        InetSocketAddress address = new InetSocketAddress(PORT);
        ss.bind(address);
        selector = Selector.open();
    }

    protected void tearDown() throws Exception {
        try {
            ssc.close();
        } catch (Exception e) {
            // do nothing
        }
        try {
            selector.close();
        } catch (Exception e) {
            // do nothing
        }
        super.tearDown();
    }

    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_connectFinish_fails()
            throws Exception {

        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);
        final Boolean[] fail = new Boolean[1];
        new Thread() {
            public void run() {
                try {
                    while (selector.isOpen()) {
                        if (selector.select() != 0) {
                            for (SelectionKey key : selector.selectedKeys()) {
                                if (key.isValid() && key.isConnectable()) {
                                    try {
                                        channel.finishConnect();
                                        synchronized (fail) {
                                            fail[0] = Boolean.FALSE;
                                            fail.notify();
                                        }
                                    }
                                    catch (NoConnectionPendingException _) {
                                        synchronized (fail) {
                                            fail[0] = Boolean.TRUE;
                                            fail.notify();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception _) {}
            }
        }.start();
        Thread.sleep(WAIT_TIME);
        channel.connect(LOCAL_ADDRESS);
        long time = System.currentTimeMillis();
        synchronized (fail) {
            while (System.currentTimeMillis() - time < WAIT_TIME || fail[0] == null) {
                fail.wait(WAIT_TIME);
            }
        }
        if (fail[0] == null) {
            System.out.println("test does not work");
        }
        else if (fail[0].booleanValue()) {
            fail();
        }
    }
}

