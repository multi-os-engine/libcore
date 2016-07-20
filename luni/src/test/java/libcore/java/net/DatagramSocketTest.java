/*
 * Copyright (C) 2014 The Android Open Source Project
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

package libcore.java.net;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DatagramSocketTest extends TestCase {

  public void testInitialState() throws Exception {
    DatagramSocket ds = new DatagramSocket();
    try {
      assertTrue(ds.isBound());
      assertTrue(ds.getBroadcast()); // The RI starts DatagramSocket in broadcast mode.
      assertFalse(ds.isClosed());
      assertFalse(ds.isConnected());
      assertTrue(ds.getLocalPort() > 0);
      assertTrue(ds.getLocalAddress().isAnyLocalAddress());
      InetSocketAddress socketAddress = (InetSocketAddress) ds.getLocalSocketAddress();
      assertEquals(ds.getLocalPort(), socketAddress.getPort());
      assertEquals(ds.getLocalAddress(), socketAddress.getAddress());
      assertNull(ds.getInetAddress());
      assertEquals(-1, ds.getPort());
      assertNull(ds.getRemoteSocketAddress());
      assertFalse(ds.getReuseAddress());
      assertNull(ds.getChannel());
    } finally {
      ds.close();
    }
  }

  public void testStateAfterClose() throws Exception {
    DatagramSocket ds = new DatagramSocket();
    ds.close();
    assertTrue(ds.isBound());
    assertTrue(ds.isClosed());
    assertFalse(ds.isConnected());
    assertNull(ds.getLocalAddress());
    assertEquals(-1, ds.getLocalPort());
    assertNull(ds.getLocalSocketAddress());
  }

  public void testSendWithoutConnection() throws IOException {
    // According to RFC3849 the 2001:db8::/32 range is reserved for documentation. Let's use
    // one of those addresses (2001:db8:dead:beef::f00) to make sure connect() fails.
    final byte[] addr = new byte[] {
      (byte) 0x20, (byte) 0x01,
      (byte) 0x0d, (byte) 0xb8,
      (byte) 0xde, (byte) 0xad,
      (byte) 0xbe, (byte) 0xef,
      (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00,
      (byte) 0x0f, (byte) 0x00,
    };
    final int port = 7;
    DatagramSocket s = new DatagramSocket();
    s.connect(InetAddress.getByAddress(addr), port);

    try {
      byte[] data = new byte[100];
      DatagramPacket p = new DatagramPacket(data, data.length);
      s.send(p);
      fail("Send succeeded unexpectedly");
    } catch (NullPointerException unexpected) {
      fail("Send did not handle pending connection exception");
    } catch (SocketException expected) {
       // This is not the exception you are looking for
    } finally {
      s.close();
    }
  }
}
