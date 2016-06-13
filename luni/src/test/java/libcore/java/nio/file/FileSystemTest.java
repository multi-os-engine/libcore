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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sun.nio.fs.LinuxFileSystemProvider;

public class FileSystemTest extends TestCase {

    FileSystem fileSystem = FileSystems.getDefault();

    public void test_provider() {
        assertTrue(fileSystem.provider() instanceof LinuxFileSystemProvider);
    }

    public void test_isOpen() throws IOException {
        assertTrue(fileSystem.isOpen());
    }

    public void test_close() throws IOException {
        // Close is not supported.
        try {
            fileSystem.close();
        } catch (UnsupportedOperationException expected) {}
    }

    public void test_isReadOnly() {
        assertFalse(fileSystem.isReadOnly());
    }

    public void test_getSeparator() {
        assertEquals("/", fileSystem.getSeparator());
    }

    public void test_getRootDirectories() {
        Iterable<Path> rootDirectories = fileSystem.getRootDirectories();
        // assertEquals(1, rootDirectories.size());
        Map<Path, Boolean> pathMap = new HashMap<>();
        rootDirectories.forEach(path -> pathMap.put(path, true));
        assertEquals(1, pathMap.size());
        assertTrue(pathMap.get(Paths.get("/")));
    }

    public void test_getFileStores() {
        Iterable<FileStore> fileStores = fileSystem.getFileStores();
        // Asserting if the the list has non zero number stores.
        assertTrue(fileStores.iterator().hasNext());
    }

    public void test_supportedFileAttributeViews() {
        Set<String> supportedFileAttributeViewsList = fileSystem.supportedFileAttributeViews();
        Map<String, Boolean> fileAttributeMap = new HashMap<>();
        supportedFileAttributeViewsList.forEach(attribute -> fileAttributeMap.put(attribute, true));
        assertEquals(6, fileAttributeMap.size());
        assertTrue(fileAttributeMap.get("posix"));
        assertTrue(fileAttributeMap.get("user"));
        assertTrue(fileAttributeMap.get("owner"));
        assertTrue(fileAttributeMap.get("unix"));
        assertTrue(fileAttributeMap.get("basic"));
        assertTrue(fileAttributeMap.get("dos"));
    }

    public void test_getPath() {
        assertEquals("f1/f2/f3", fileSystem.getPath("f1", "f2", "f3").toString());
        assertEquals("f1/../f3", fileSystem.getPath("f1", "..", "f3").toString());
        assertEquals("f1/.*./f3", fileSystem.getPath("f1", ".*.", "f3").toString());
    }

    public void test_getPathMatcher() {
        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + "*.java");
        assertTrue(pathMatcher.matches(Paths.get("f.java")));
        assertFalse(pathMatcher.matches(Paths.get("f")));
    }

    public void test_getUserPrincipalLookupService() {
        assertNotNull(fileSystem.getUserPrincipalLookupService());
    }

    public void test_newWatchService() throws IOException {
        assertNotNull(fileSystem.newWatchService());
    }
}