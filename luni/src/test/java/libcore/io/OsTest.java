/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.InetUnixAddress;
import java.net.ServerSocket;
import junit.framework.TestCase;

import static libcore.io.OsConstants.*;

public class OsTest extends TestCase {
  public void testIsSocket() throws Exception {
    File f = new File("/dev/null");
    FileInputStream fis = new FileInputStream(f);
    assertFalse(S_ISSOCK(Libcore.os.fstat(fis.getFD()).st_mode));
    fis.close();

    ServerSocket s = new ServerSocket();
    assertTrue(S_ISSOCK(Libcore.os.fstat(s.getImpl$().getFD$()).st_mode));
    s.close();
  }

  public void testUnixDomainSockets_in_file_system() throws Exception {
    String path = System.getProperty("java.io.tmpdir") + "/test_unix_socket";
    new File(path).delete();
    checkUnixDomainSocket(new InetUnixAddress(path));
  }

  public void testUnixDomainSocket_abstract_name() throws Exception {
    // Linux treats a sun_path starting with a NUL byte as an abstract name. See unix(7).
    byte[] path = "/abstract_name_unix_socket".getBytes("UTF-8");
    path[0] = 0;
    checkUnixDomainSocket(new InetUnixAddress(path));
  }

  private void checkUnixDomainSocket(InetUnixAddress address) throws Exception {
    final FileDescriptor server_fd = Libcore.os.socket(AF_UNIX, SOCK_STREAM, 0);
    Libcore.os.bind(server_fd, address, 0);
    Libcore.os.listen(server_fd, 5);

    Thread server = new Thread(new Runnable() {
      public void run() {
        try {
          FileDescriptor client_fd = Libcore.os.accept(server_fd, null);

          StructUcred credentials = Libcore.os.getsockoptUcred(client_fd, SOL_SOCKET, SO_PEERCRED);
          assertEquals(Libcore.os.getpid(), credentials.pid);
          assertEquals(Libcore.os.getuid(), credentials.uid);
          assertEquals(Libcore.os.getgid(), credentials.gid);

          byte[] request = new byte[256];
          int requestLength = Libcore.os.read(client_fd, request, 0, request.length);

          String s = new String(request, "UTF-8");
          byte[] response = s.toUpperCase().getBytes("UTF-8");
          Libcore.os.write(client_fd, response, 0, response.length);

          Libcore.os.close(client_fd);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    });
    server.start();

    FileDescriptor client_fd = Libcore.os.socket(AF_UNIX, SOCK_STREAM, 0);

    Libcore.os.connect(client_fd, address, 0);

    String string = "hello, world!";

    byte[] request = string.getBytes("UTF-8");
    assertEquals(request.length, Libcore.os.write(client_fd, request, 0, request.length));

    byte[] response = new byte[request.length];
    assertEquals(response.length, Libcore.os.read(client_fd, response, 0, response.length));

    assertEquals(string.toUpperCase(), new String(response, "UTF-8"));

    Libcore.os.close(client_fd);
  }
}
