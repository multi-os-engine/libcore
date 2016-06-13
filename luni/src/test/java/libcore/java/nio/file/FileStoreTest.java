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
 * limitations under the License
 */

package libcore.java.nio.file;

import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;

public class FileStoreTest extends TestCase {

    @Override
    protected void tearDown() throws IOException {
        FilesTest.clearAll();
    }

    public void test_name() throws IOException, InterruptedException {
        Path path = Paths.get("dir");
        Files.createDirectory(path);
        Process p = FilesTest.execCmdAndWaitForTermination("df", path.toAbsolutePath().toString());
        String shellOutput = FilesTest.readFromInputStream(p.getInputStream()).split("\n")[1];
        String storeTypeFromShell = shellOutput.split("\\s")[0];
        assertEquals(storeTypeFromShell, Files.getFileStore(path).name());
    }

    public void test_type() throws IOException, InterruptedException {
        Path path = Paths.get("dir");
        Files.createDirectory(path);
        String fileStore = Files.getFileStore(path).name();
        Process p = FilesTest.execCmdAndWaitForTermination("mount");
        String mountOuput[] = FilesTest.readFromInputStream(p.getInputStream()).split("\n");
        for (String mountInfo : mountOuput) {
            if (mountInfo.contains(fileStore)
                    && mountInfo.contains(Files.getFileStore(path).type())) {
                return;
            }
        }
        fail();
    }

    public void test_isReadOnly() throws IOException {
        Path path = Paths.get("/system");
        assertTrue(Files.getFileStore(path).isReadOnly());

        path = Paths.get("/data");
        assertFalse(Files.getFileStore(path).isReadOnly());
    }

    public void test_getTotalSpace() throws IOException {
        Path path = Paths.get("/data");
        assertTrue(Files.getFileStore(path).getTotalSpace() > 0);
    }

    public void test_getUsableSpace() throws IOException {
        Path path = Paths.get("/data");
        long usableSpace = Files.getFileStore(path).getUsableSpace();
        long totalSpace = Files.getFileStore(path).getTotalSpace();
        assertTrue(usableSpace <= totalSpace && usableSpace > 0);
    }

    public void test_getUnallocatedSpace() throws IOException {
        Path path = Paths.get("/data");
        long unallocatedSpace = Files.getFileStore(path).getUnallocatedSpace();
        assertTrue(unallocatedSpace >= 0);
    }

    public void test_supportsFileAttributeView$Class() throws IOException {
        Path path = Paths.get("dir");
        Files.createDirectories(path);
        assertTrue(Files.getFileStore(path).supportsFileAttributeView(BasicFileAttributeView.class));
        assertTrue(Files.getFileStore(path).supportsFileAttributeView(FileOwnerAttributeView.class));
        assertTrue(Files.getFileStore(path).supportsFileAttributeView(PosixFileAttributeView.class));
        assertFalse(Files.getFileStore(path).supportsFileAttributeView(DosFileAttributeView.class));
        assertFalse(Files.getFileStore(path).
                supportsFileAttributeView(UserDefinedFileAttributeView.class));
        assertFalse(Files.getFileStore(path).
                supportsFileAttributeView(NonStandardFileAttributeView.class));
    }

    public void test_supportsFileAttributeView$String() throws IOException {
        Path path = Paths.get("dir");
        Files.createDirectories(path);
        assertTrue(Files.getFileStore(path).supportsFileAttributeView("basic"));
        assertTrue(Files.getFileStore(path).supportsFileAttributeView("unix"));
        assertTrue(Files.getFileStore(path).supportsFileAttributeView("posix"));
        assertTrue(Files.getFileStore(path).supportsFileAttributeView("owner"));
        assertFalse(Files.getFileStore(path).supportsFileAttributeView("user"));
        assertFalse(Files.getFileStore(path).supportsFileAttributeView("dos"));
        assertFalse(Files.getFileStore(path).supportsFileAttributeView("nonStandardView"));
    }

    public void test_getFileStoreAttributeView() throws IOException {
        Path path = Paths.get("dir");
        Files.createDirectories(path);
        assertNull(Files.getFileStore(path).getFileStoreAttributeView(FileStoreAttributeView.class));
        try {
            Files.getFileStore(path).getFileStoreAttributeView(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_getAttribute() throws IOException {
        Path p = Paths.get("dir");
        Files.createDirectories(p);
        FileStore store = Files.getFileStore(p);
        assertEquals(store.getTotalSpace(), store.getAttribute("totalSpace"));
        assertEquals(store.getUnallocatedSpace(), store.getAttribute("unallocatedSpace"));
        assertEquals(store.getUsableSpace(), store.getAttribute("usableSpace"));
        try {
            store.getAttribute("test");
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    private static class NonStandardFileAttributeView implements FileAttributeView {
        @Override
        public String name() {
            return null;
        }
    }
}