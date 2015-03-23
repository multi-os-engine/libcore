/*
 * Copyright (C) 2015 The Android Open Source Project
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
import org.mockftpserver.core.util.IoUtil;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link libcore.net.url.FtpURLConnection}.
 */
public class FtpURLConnectionTest extends TestCase {

    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String USER_HOME_DIR = "/home/user";

    private FakeFtpServer fakeFtpServer;
    private UnixFakeFileSystem fileSystem;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(0); // 0 means pick a free port.
        fakeFtpServer.addUserAccount(new UserAccount(USER, PASSWORD, USER_HOME_DIR));
        fileSystem = new UnixFakeFileSystem();
        fakeFtpServer.setFileSystem(fileSystem);
        fileSystem.add(new DirectoryEntry("/home/user"));
        fakeFtpServer.start();
    }

    @Override
    public void tearDown() throws Exception {
        fakeFtpServer.stop();
        ProxySelector.setDefault(null);
        super.tearDown();
    }

    public void testInputUrl() throws Exception {
        String fileContents = "abcdef 1234567890";
        String fileName = "file1.txt";
        fileSystem.add(createFileEntry(fileName, fileContents));

        URL url = new URL(createUrl(fileName));
        URLConnection connection = url.openConnection();
        assertEquals(fileContents, readContents(connection.getInputStream()));
    }

    public void testOutputUrl() throws Exception {
        String fileContents = "abcdef 1234567890";
        String fileName = "file1.txt";
        fileSystem.add(createFileEntry(fileName, fileContents));

        URL url = new URL(createUrl(fileName));
        URLConnection connection = url.openConnection();
        connection.setDoInput(false);
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        writeBytes(os, fileContents);

        assertEquals(fileContents, readFileSystemContents(fileName));
    }

    public void testInputUrlWithSpaces() throws Exception {
        String fileContents = "abcdef 1234567890";
        String fileName = "file with spaces.txt";
        fileSystem.add(createFileEntry(fileName, fileContents));

        URL url = new URL(createUrl(fileName));
        URLConnection connection = url.openConnection();
        assertEquals(fileContents, readContents(connection.getInputStream()));
    }

    // TODO Add bug number
    public void testInputUrlWithSpacesViaProxySelector() throws Exception {
        ProxySelector proxySelector = new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                assertNotNull(uri);
                return Arrays.asList(Proxy.NO_PROXY);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress address, IOException failure) {
            }
        };
        ProxySelector.setDefault(proxySelector);

        String fileContents = "abcdef 1234567890";
        String fileName = "file with spaces.txt";
        fileSystem.add(createFileEntry(fileName, fileContents));

        URL url = new URL(createUrl(fileName));
        URLConnection connection = url.openConnection();
        assertEquals(fileContents, readContents(connection.getInputStream()));
    }

    private String readFileSystemContents(String fileName) throws IOException {
        String fullFileName = USER_HOME_DIR + "/" + fileName;
        FileEntry entry = (FileEntry) fileSystem.getEntry(fullFileName);
        assertNotNull("File must exist with name " + fullFileName, entry);
        return readContents(entry.createInputStream());
    }

    private String createUrl(final String fileName) {
        return "ftp://" + USER + ":" + PASSWORD + "@localhost:" + fakeFtpServer.getServerControlPort() + "/" + fileName;
    }

    private static void writeBytes(OutputStream os, String fileContents) throws IOException {
        os.write(fileContents.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static String readContents(InputStream inputStream) throws IOException {
        byte[] contentBytes = IoUtil.readBytes(inputStream);
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

    private static FileEntry createFileEntry(String path, String fileContents) {
        FileEntry entry = new FileEntry(USER_HOME_DIR + "/" + path);
        entry.setContents(fileContents.getBytes(StandardCharsets.UTF_8));
        return entry;
    }
}
