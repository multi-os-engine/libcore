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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjDoubleConsumer;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.FileVisitResult.*;

public class FilesTest extends TestCase {

    private final static String TEST_DIR = "testDir";

    private final static String DATA_FILE = "dataFile";

    private final static String NON_EXISTENT_FILE = "nonExistentFile";

    private final static String TEST_FILE_DATA = "hello";

    private final static String TEST_FILE_DATA_2 = "test";

    private final static Path DATA_FILE_PATH = Paths.get(TEST_DIR, DATA_FILE);

    private final static Path NON_EXISTENT_FILE_PATH = Paths.get(TEST_DIR, NON_EXISTENT_FILE);

    private final static Path TEST_DIR_PATH = Paths.get(TEST_DIR);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initializeFiles();
    }

    private void initializeFiles() throws IOException {
        Files.createDirectory(TEST_DIR_PATH);
        File testInputFile = new File(TEST_DIR, DATA_FILE);
        if (!testInputFile.exists()) {
            testInputFile.createNewFile();
        }
        FileWriter fw = new FileWriter(testInputFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(TEST_FILE_DATA);
        bw.close();
    }

    @Override
    public void tearDown() throws Exception {
        clearAll();
        super.tearDown();
    }

    static void clearAll() throws IOException {
        Path root = Paths.get(TEST_DIR);
        delete(root);
    }

    private void reset() throws IOException {
        clearAll();
        initializeFiles();
    }

    private static void delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(path);
            dirStream.forEach(
                    p -> {
                        try {
                            delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            dirStream.close();
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // Do nothing
        }
    }

    public void test_newInputStream() throws IOException {
        InputStream is = Files.newInputStream(DATA_FILE_PATH, READ);
        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
    }

    public void test_newInputStream_openOption() throws IOException {
        // Write and Append are not supported.
        try {
            Files.newInputStream(DATA_FILE_PATH, WRITE);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            Files.newInputStream(DATA_FILE_PATH, APPEND);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            Files.newInputStream(DATA_FILE_PATH, NonStandardOption.OPTION1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // Supported options.
        InputStream is = Files.newInputStream(DATA_FILE_PATH, DELETE_ON_CLOSE, CREATE_NEW,
                TRUNCATE_EXISTING, SPARSE, SYNC, DSYNC);
        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
    }

    public void test_newInputStream_twice() throws IOException {
        InputStream is = Files.newInputStream(DATA_FILE_PATH, READ);

        // Open the same file again.
        InputStream is2 = Files.newInputStream(DATA_FILE_PATH, READ);

        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
        assertEquals(TEST_FILE_DATA, readFromInputStream(is2));
    }

    public void test_newInputStream_NPE() throws IOException {
        try {
            Files.newInputStream(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.newInputStream(DATA_FILE_PATH, (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newOutputStream() throws IOException {
        OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH);
        os.write(TEST_FILE_DATA.getBytes());
        os.close();

        InputStream is = Files.newInputStream(NON_EXISTENT_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
    }

    public void test_newOutputStream_openOption_READ() throws IOException {
        try {
            OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, READ);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_newOutputStream_openOption_APPEND() throws IOException {
        // When file exists and it contains data.
        OutputStream os = Files.newOutputStream(DATA_FILE_PATH, APPEND);
        os.write(TEST_FILE_DATA.getBytes());
        os.close();

        InputStream is = Files.newInputStream(DATA_FILE_PATH);
        assertEquals(TEST_FILE_DATA + TEST_FILE_DATA, readFromInputStream(is));

        // When file doesn't exist.
        try {
            Files.newOutputStream(NON_EXISTENT_FILE_PATH, APPEND);
            fail();
        } catch (NoSuchFileException expected) {
        }
    }

    public void test_newOutputStream_openOption_TRUNCATE() throws IOException {
        // When file exists.
        try (OutputStream os = Files.newOutputStream(DATA_FILE_PATH, TRUNCATE_EXISTING)) {
            os.write(TEST_FILE_DATA_2.getBytes());
            os.close();

            try (InputStream is = Files.newInputStream(DATA_FILE_PATH)) {
                assertEquals(TEST_FILE_DATA_2, readFromInputStream(is));
            }
        }

        // When file doesn't exist.
        try (OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, TRUNCATE_EXISTING)) {
            fail();
        } catch (NoSuchFileException expected) {
        }
    }

    public void test_newOutputStream_openOption_WRITE() throws IOException {
        // When file exists.
        try (OutputStream os = Files.newOutputStream(DATA_FILE_PATH, WRITE)) {
            os.write(TEST_FILE_DATA_2.getBytes());
            os.close();

            try (InputStream is = Files.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }

        // When file doesn't exist.
        try (OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, WRITE)) {
            fail();
        } catch (NoSuchFileException expected) {
        }
    }

    public void test_newOutputStream_openOption_CREATE() throws IOException {
        // When file exists.
        try (OutputStream os = Files.newOutputStream(DATA_FILE_PATH, CREATE)) {
            os.write(TEST_FILE_DATA_2.getBytes());
            os.close();

            try (InputStream is = Files.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }

        // When file doesn't exist.
        try (OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, CREATE)) {
            os.write(TEST_FILE_DATA.getBytes());
            os.close();

            try (InputStream is = Files.newInputStream(NON_EXISTENT_FILE_PATH)) {
                assertEquals(TEST_FILE_DATA, readFromInputStream(is));
            }
        }
    }

    public void test_newOutputStream_openOption_CREATE_NEW() throws IOException {
        // When file exists.
        try (OutputStream os = Files.newOutputStream(DATA_FILE_PATH, CREATE_NEW)) {
            fail();
        } catch (FileAlreadyExistsException expected) {
        }

        // When file doesn't exist.
        try (OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, CREATE_NEW)) {
            os.write(TEST_FILE_DATA.getBytes());
            os.close();

            try (InputStream is = Files.newInputStream(NON_EXISTENT_FILE_PATH)) {
                assertEquals(TEST_FILE_DATA, readFromInputStream(is));
            }
        }
    }

    public void test_newOutputStream_openOption_CREATE_SYNC() throws IOException {
        try (OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, SYNC, CREATE)) {
            try (InputStream is = Files.newInputStream(NON_EXISTENT_FILE_PATH, SYNC)) {
                os.write(TEST_FILE_DATA.getBytes());
                assertEquals(TEST_FILE_DATA, readFromInputStream(is));
            }
        }
    }

    public void test_newOutputStream_NPE() throws IOException {
        try (OutputStream os = Files.newOutputStream(null)) {
            fail();
        } catch (NullPointerException expected) {}

        try (OutputStream os = Files.newOutputStream(NON_EXISTENT_FILE_PATH, (OpenOption[]) null)) {
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newByteChannel() throws IOException {
        // When file doesn't exist
        try (SeekableByteChannel sbc = Files.newByteChannel(NON_EXISTENT_FILE_PATH)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // When file exists.

        // File opens in READ mode by default. The channel is non writable by default.
        try (SeekableByteChannel sbc = Files.newByteChannel(DATA_FILE_PATH)) {
            sbc.write(ByteBuffer.allocate(10));
            fail();
        } catch (NonWritableChannelException expected) {
        }

        // Read a file.
        try (SeekableByteChannel sbc = Files.newByteChannel(DATA_FILE_PATH)) {
            ByteBuffer readBuffer = ByteBuffer.allocate(10);
            int bytesReadCount = sbc.read(readBuffer);

            String readData = new String(Arrays.copyOf(readBuffer.array(), bytesReadCount),
                    "UTF-8");
            assertEquals(TEST_FILE_DATA, readData);
        }
    }

    public void test_newByteChannel_openOption_WRITE() throws IOException {
        // When file doesn't exist
        try (SeekableByteChannel sbc = Files.newByteChannel(NON_EXISTENT_FILE_PATH, WRITE)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // When file exists.

        try (SeekableByteChannel sbc = Files.newByteChannel(DATA_FILE_PATH, WRITE)) {
            sbc.read(ByteBuffer.allocate(10));
            fail();
        } catch (NonReadableChannelException expected) {
        }

        // Write in file.
        try (SeekableByteChannel sbc = Files.newByteChannel(DATA_FILE_PATH, WRITE)) {
            sbc.write(ByteBuffer.wrap(TEST_FILE_DATA_2.getBytes()));
            sbc.close();

            try (InputStream is = Files.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }
    }

    public void test_newByteChannel_openOption_WRITE_READ() throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(DATA_FILE_PATH, WRITE, READ,
                SYNC)) {
            ByteBuffer readBuffer = ByteBuffer.allocate(10);
            int bytesReadCount = sbc.read(readBuffer);

            String readData = new String(Arrays.copyOf(readBuffer.array(), bytesReadCount),
                    "UTF-8");
            assertEquals(TEST_FILE_DATA, readData);

            // Pointer will move to the end of the file after read operation. The write should
            // append the data at the end of the file.
            sbc.write(ByteBuffer.wrap(TEST_FILE_DATA_2.getBytes()));
            try (InputStream is = Files.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA + TEST_FILE_DATA_2;
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }
    }

    public void test_newByteChannel_NPE() throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(null)) {
            fail();
        } catch(NullPointerException expected) {}

        try (SeekableByteChannel sbc = Files.newByteChannel(DATA_FILE_PATH, (OpenOption[]) null)) {
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_createFile() throws IOException {
        assertFalse(Files.exists(NON_EXISTENT_FILE_PATH));
        Files.createFile(NON_EXISTENT_FILE_PATH);
        assertTrue(Files.exists(NON_EXISTENT_FILE_PATH));

        try {
            Files.createFile(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (FileAlreadyExistsException expected) {
        }

        // File with unicode name.
        Path unicodeFilePath = Paths.get(TEST_DIR, "परीक्षण फ़ाइल");
        Files.createFile(unicodeFilePath);
        Files.exists(unicodeFilePath);

        // When file exists.
        try {
            Files.createFile(DATA_FILE_PATH);
            fail();
        } catch(FileAlreadyExistsException expected) {}
    }

    public void test_createFile_NPE() throws IOException {
        try {
            Files.createFile(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createFile$String$Attr() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.createFile(NON_EXISTENT_FILE_PATH, attr);
        assertEquals(attr.value(), Files.getAttribute(NON_EXISTENT_FILE_PATH, attr.name()));

        // Creating a new file and passing multiple attribute of the same name.
        perm = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr1 = PosixFilePermissions.asFileAttribute(perm);
        Path filePath2 = Paths.get(TEST_DIR, "new_file");
        Files.createFile(filePath2, attr, attr1);
        // Value should be equal to the last attribute passed.
        assertEquals(attr1.value(), Files.getAttribute(filePath2, attr.name()));

        // When file exists.
        try {
            Files.createFile(DATA_FILE_PATH, attr);
            fail();
        } catch(FileAlreadyExistsException expected) {}
    }

    public void test_createDirectory() throws IOException {
        Path newDirectory = Paths.get(TEST_DIR, "newDir");
        assertFalse(Files.exists(newDirectory));
        assertFalse(Files.isDirectory(newDirectory));

        Files.createDirectory(newDirectory);

        assertTrue(Files.exists(newDirectory));
        assertTrue(Files.isDirectory(newDirectory));

        try {
            Files.createDirectory(newDirectory);
            fail();
        } catch (FileAlreadyExistsException expected) {
        }

        // File with unicode name.
        Path unicodeFilePath = Paths.get(TEST_DIR, "टेस्ट डायरेक्टरी");
        Files.createDirectory(unicodeFilePath);
        Files.exists(unicodeFilePath);
    }

    public void test_createDirectory_NPE() throws IOException {
        try {
            Files.createDirectory(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createDirectory$String$FileAttr() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.createDirectory(NON_EXISTENT_FILE_PATH, attr);
        assertEquals(attr.value(), Files.getAttribute(NON_EXISTENT_FILE_PATH, attr.name()));

        // Creating a new file and passing multiple attribute of the same name.
        perm = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr1 = PosixFilePermissions.asFileAttribute(perm);
        Path dirPath2 = Paths.get(TEST_DIR, "new_file");
        Files.createDirectory(dirPath2, attr, attr1);
        // Value should be equal to the last attribute passed.
        assertEquals(attr1.value(), Files.getAttribute(dirPath2, attr.name()));
    }

    public void test_createDirectory$String$FileAttr_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            Files.createDirectory(null, attr);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.createDirectory(NON_EXISTENT_FILE_PATH, (FileAttribute<?>[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_newDirectoryStream() throws IOException {
        Path path_dir1 = Paths.get(TEST_DIR, "newDir1");
        Path path_dir2 = Paths.get(TEST_DIR, "newDir1/newDir2");
        Path path_dir3 = Paths.get(TEST_DIR, "newDir1/newDir3");

        Path path_file1 = Paths.get(TEST_DIR, "newDir1/newFile1");
        Path path_file2 = Paths.get(TEST_DIR, "newDir1/newFile2");
        Path path_file3 = Paths.get(TEST_DIR, "newDir1/newDir2/newFile3");

        Map<Path, Boolean> pathMap = new HashMap<>();
        pathMap.put(path_dir1, false);
        pathMap.put(path_dir2, false);
        pathMap.put(path_dir3, false);
        pathMap.put(path_file1, false);
        pathMap.put(path_file2, false);
        pathMap.put(path_file3, false);

        Files.createDirectory(path_dir1);
        Files.createDirectory(path_dir2);
        Files.createDirectory(path_dir3);
        Files.createFile(path_file1);
        Files.createFile(path_file2);
        Files.createFile(path_file3);

        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1);

        directoryStream.forEach(k -> pathMap.replace(k, true));
        assertEquals(6, pathMap.size());

        assertFalse(pathMap.get(path_dir1));
        assertTrue(pathMap.get(path_dir2));
        assertTrue(pathMap.get(path_dir3));
        assertTrue(pathMap.get(path_file1));
        assertTrue(pathMap.get(path_file2));
        assertFalse(pathMap.get(path_file3));
    }

    public void test_newDirectoryStream_Exception() throws IOException {

        // Non existent directory.
        Path path_dir1 = Paths.get(TEST_DIR, "newDir1");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // File instead of directory.
        Path path_file1 = Paths.get(TEST_DIR, "newFile1");
        Files.createFile(path_file1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1)) {
            fail();
        } catch (NotDirectoryException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(null)) {
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_newDirectoryStream$Path$String() throws IOException {
        Path path_root = Paths.get(TEST_DIR, "dir");
        Path path_java1 = Paths.get(TEST_DIR, "dir/f1.java");
        Path path_java2 = Paths.get(TEST_DIR, "dir/f2.java");
        Path path_java3 = Paths.get(TEST_DIR, "dir/f3.java");

        Path path_txt1 = Paths.get(TEST_DIR, "dir/f1.txt");
        Path path_txt2 = Paths.get(TEST_DIR, "dir/f2.txt");
        Path path_txt3 = Paths.get(TEST_DIR, "dir/f3.txt");

        Map<Path, Boolean> pathMap = new HashMap<>();
        pathMap.put(path_java1, false);
        pathMap.put(path_java2, false);
        pathMap.put(path_java3, false);
        pathMap.put(path_txt1, false);
        pathMap.put(path_txt2, false);
        pathMap.put(path_txt3, false);

        Files.createDirectory(path_root);
        // A directory with .java extension.
        Files.createDirectory(path_java1);
        Files.createFile(path_java2);
        Files.createFile(path_java3);
        Files.createFile(path_txt1);
        Files.createFile(path_txt2);
        Files.createFile(path_txt3);

        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_root, "*.java");

        directoryStream.forEach(k -> pathMap.replace(k, true));
        assertEquals(6, pathMap.size());

        assertTrue(pathMap.get(path_java1));
        assertTrue(pathMap.get(path_java2));
        assertTrue(pathMap.get(path_java3));
        assertFalse(pathMap.get(path_txt1));
        assertFalse(pathMap.get(path_txt2));
        assertFalse(pathMap.get(path_txt3));
    }

    public void test_newDirectoryStream$Path$String_Exception() throws IOException {

        // Non existent directory.
        Path path_dir1 = Paths.get(TEST_DIR, "newDir1");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1, "*.c")) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // File instead of directory.
        Path path_file1 = Paths.get(TEST_DIR, "newFile1");
        Files.createFile(path_file1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1, "*.c")) {
            fail();
        } catch (NotDirectoryException expected) {
        }

        Files.createFile(path_dir1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1, "[a")) {
            fail();
        } catch (PatternSyntaxException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(null, "[a")) {
            fail();
        } catch (NullPointerException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1,
                (String)null)) {
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_newDirectoryStream$Path$Filter() throws IOException {

        Path path_root = Paths.get(TEST_DIR, "dir");
        Path path_dir1 = Paths.get(TEST_DIR, "dir/dir1");
        Path path_dir2 = Paths.get(TEST_DIR, "dir/dir2");
        Path path_dir3 = Paths.get(TEST_DIR, "dir/dir3");

        Path path_f1 = Paths.get(TEST_DIR, "dir/f1");
        Path path_f2 = Paths.get(TEST_DIR, "dir/f2");
        Path path_f3 = Paths.get(TEST_DIR, "dir/f3");

        Map<Path, Boolean> pathMap = new HashMap<>();
        pathMap.put(path_dir1, false);
        pathMap.put(path_dir2, false);
        pathMap.put(path_dir3, false);
        pathMap.put(path_f1, false);
        pathMap.put(path_f2, false);
        pathMap.put(path_f3, false);

        Files.createDirectory(path_root);
        Files.createDirectory(path_dir1);
        Files.createDirectory(path_dir2);
        Files.createDirectory(path_dir3);
        Files.createFile(path_f1);
        Files.createFile(path_f2);
        Files.createFile(path_f3);

        // Filter all the directories.
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_root,
                file -> Files.isDirectory(file));

        directoryStream.forEach(k -> pathMap.replace(k, true));
        assertEquals(6, pathMap.size());

        assertTrue(pathMap.get(path_dir1));
        assertTrue(pathMap.get(path_dir2));
        assertTrue(pathMap.get(path_dir3));
        assertFalse(pathMap.get(path_f1));
        assertFalse(pathMap.get(path_f2));
        assertFalse(pathMap.get(path_f3));
    }

    public void test_newDirectoryStream$Filter_Exception() throws IOException {

        // Non existent directory.
        Path path_dir1 = Paths.get(TEST_DIR, "newDir1");
        DirectoryStream.Filter<Path> fileFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isDirectory(entry);
            }
        };
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1,
                fileFilter)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // File instead of directory.
        Path path_file1 = Paths.get(TEST_DIR, "newFile1");
        Files.createFile(path_file1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1,
                fileFilter)) {
            fail();
        } catch (NotDirectoryException expected) {
        }
    }

    public void test_newDirectoryStream$Filter_NPE() throws IOException {
        // Non existent directory.
        Path path_dir1 = Paths.get(TEST_DIR, "newDir1");
        DirectoryStream.Filter<Path> fileFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isDirectory(entry);
            }
        };
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(null,
                fileFilter)) {
            fail();
        } catch (NullPointerException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1,
                (DirectoryStream.Filter<? super Path>) null)) {
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_createDirectories() throws IOException {
        // Should be able to create parent directories.
        Path dirPath = Paths.get(TEST_DIR, "dir1/dir2/dir3");
        assertFalse(Files.exists(dirPath));
        Files.createDirectories(dirPath);
        assertTrue(Files.isDirectory(dirPath));

        // Creating an existing directory. Should not throw any error.
        Files.createDirectories(dirPath);
    }

    public void test_createDirectories_NPE() throws IOException {
        try {
            Files.createDirectories(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createDirectories$Path$Attr() throws IOException {
        Path dirPath = Paths.get(TEST_DIR, "dir1/dir2/dir3");
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        assertFalse(Files.exists(dirPath));
        Files.createDirectories(dirPath, attr);
        assertEquals(attr.value(), Files.getAttribute(dirPath, attr.name()));

        // Creating an existing directory with new permissions.
        perm = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr1 =  PosixFilePermissions.asFileAttribute(perm);
        Files.createDirectories(dirPath, attr);

        // Value should not change as the directory exists.
        assertEquals(attr.value(), Files.getAttribute(dirPath, attr.name()));

        // Creating a new directory and passing multiple attribute of the same name.
        Path dirPath2 = Paths.get(TEST_DIR, "dir1/dir2/dir4");
        Files.createDirectories(dirPath2, attr, attr1);
        // Value should be equal to the last attribute passed.
        assertEquals(attr1.value(), Files.getAttribute(dirPath2, attr.name()));
    }

    public void test_createDirectories$Path$Attr_NPE() throws IOException {
        Path dirPath = Paths.get(TEST_DIR, "dir1/dir2/dir3");
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            Files.createDirectories(null, attr);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.createDirectories(dirPath, (FileAttribute<?>[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_createSymbolicLink() throws IOException {
        Files.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                /* Path of the target of the symbolic link */ DATA_FILE_PATH.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(NON_EXISTENT_FILE_PATH));

        // When file exists at the sym link location.
        try {
            Files.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                    /* Path of the target of the symbolic link */ DATA_FILE_PATH.toAbsolutePath());
            fail();
        } catch (FileAlreadyExistsException expected) {} finally {
            Files.deleteIfExists(NON_EXISTENT_FILE_PATH);
        }

        // Sym link to itself
        Files.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                /* Path of the target of the symbolic link */ NON_EXISTENT_FILE_PATH.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(NON_EXISTENT_FILE_PATH.toAbsolutePath()));
    }

    public void test_createSymbolicLink_NPE() throws IOException {
        try {
            Files.createSymbolicLink(null, DATA_FILE_PATH.toAbsolutePath());
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createSymbolicLink$Path$Attr() throws IOException {
        try {
            Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
                    .asFileAttribute(perm);
            Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH.toAbsolutePath(), attr);
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public void test_createSymbolicLink$Path$Attr_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
                .asFileAttribute(perm);

        try {
            Files.createSymbolicLink(null, DATA_FILE_PATH.toAbsolutePath(), attr);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, null, attr);
            fail();

        } catch (NullPointerException expected) {}

        try {
            Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH,
                    (FileAttribute<?>[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createLink() throws IOException {
        // TODO: Unable to create hardlinks on Android.
    }

    public void test_delete() throws IOException {
        // Delete existing file.
        Files.delete(DATA_FILE_PATH);
        assertFalse(Files.exists(DATA_FILE_PATH));

        // Delete non existing files.
        try {
            Files.delete(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (NoSuchFileException expected) {}

        // Delete a directory.
        Path dirPath = Paths.get(TEST_DIR, "dir");
        Files.createDirectory(dirPath);
        Files.delete(dirPath);
        assertFalse(Files.exists(dirPath));


        // Delete a non empty directory.
        Files.createDirectory(dirPath);
        Files.createFile(Paths.get(TEST_DIR, "dir/file"));
        try {
            Files.delete(dirPath);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    public void test_delete_NPE() throws IOException {
        try {
            Files.delete(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_deleteIfExist() throws IOException {
        // Delete existing file.
        assertTrue(Files.deleteIfExists(DATA_FILE_PATH));
        assertFalse(Files.exists(DATA_FILE_PATH));

        // Delete non existing files.
        assertFalse(Files.deleteIfExists(NON_EXISTENT_FILE_PATH));

        // Delete a directory.
        Path dirPath = Paths.get(TEST_DIR, "dir");
        Files.createDirectory(dirPath);
        assertTrue(Files.deleteIfExists(dirPath));
        assertFalse(Files.exists(dirPath));

        // Delete a non empty directory.
        Files.createDirectory(dirPath);
        Files.createFile(Paths.get(TEST_DIR, "dir/file"));
        try {
            Files.deleteIfExists(dirPath);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    public void test_deleteIfExist_NPE() throws IOException {
        try {
            Files.deleteIfExists(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_copy() throws IOException {
        Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));
        // The original file should also exists.
        assertEquals(TEST_FILE_DATA, readFromFile(DATA_FILE_PATH));

        // When target file exists.
        try {
            Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        // Copy to existing target file with REPLACE_EXISTING copy option.
        writeToFile(DATA_FILE_PATH, TEST_FILE_DATA_2);
        Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, REPLACE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(NON_EXISTENT_FILE_PATH));


        // Copy to the same file. Should not fail.
        reset();
        Files.copy(DATA_FILE_PATH, DATA_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromFile(DATA_FILE_PATH));

        // With target is a symbolic link file.
        try {
            reset();
            Path symlink = Paths.get(TEST_DIR, "symlink");
            Path newFile = Paths.get(TEST_DIR, "newDir");
            Files.createFile(newFile);
            assertTrue(Files.exists(newFile));
            Files.createSymbolicLink(symlink, DATA_FILE_PATH);
            Files.copy(DATA_FILE_PATH, symlink);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        reset();
        try {
            Files.copy(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH, REPLACE_EXISTING);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_copy_NPE() throws IOException {
        try {
            Files.copy((Path)null, NON_EXISTENT_FILE_PATH);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.copy(DATA_FILE_PATH, (Path)null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, (CopyOption[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_copy_CopyOption() throws IOException {
        // COPY_ATTRIBUTES
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        Files.setAttribute(DATA_FILE_PATH, "basic:lastModifiedTime", fileTime);
        Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, COPY_ATTRIBUTES);
        assertEquals(fileTime.to(TimeUnit.SECONDS),
                ((FileTime)Files.getAttribute(NON_EXISTENT_FILE_PATH,
                        "basic:lastModifiedTime")).to(TimeUnit.SECONDS));
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // ATOMIC_MOVE
        Files.deleteIfExists(NON_EXISTENT_FILE_PATH);
        try {
            Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, ATOMIC_MOVE);
            fail();
        } catch (UnsupportedOperationException expected) {}

        Files.deleteIfExists(NON_EXISTENT_FILE_PATH);
        try {
            Files.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, NonStandardOption.OPTION1);
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public void test_copy_directory() throws IOException {
        final Path dirPath = Paths.get(TEST_DIR, "dir1");
        final Path dirPath2 = Paths.get(TEST_DIR, "dir2");
        // Nested directory.
        final Path dirPath3 = Paths.get(TEST_DIR, "dir1/dir");

        Files.createDirectory(dirPath);
        Files.createDirectory(dirPath3);
        Files.copy(DATA_FILE_PATH, Paths.get(TEST_DIR, "dir1/" + DATA_FILE));
        Files.copy(dirPath, dirPath2);

        Map<Path, Boolean> pathMap = new HashMap<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath2);
        directoryStream.forEach(file -> pathMap.put(file, true));

        // The files are not copied. The command is equivalent of creating a new directory.
        assertEquals(0, pathMap.size());

        Path dirPath4 = Paths.get(TEST_DIR, "dir4");
        Files.createDirectories(dirPath4);
        Path file = Paths.get("file");
        Files.createFile(Paths.get(dirPath.toString(), file.toString()));
        Files.createFile(Paths.get(dirPath4.toString(), file.toString()));

        try {
            Files.copy(dirPath, dirPath4, REPLACE_EXISTING);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    public void test_move() throws IOException {
        Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));
        assertFalse(Files.exists(DATA_FILE_PATH));

        reset();
        Files.createFile(NON_EXISTENT_FILE_PATH);
        // When target file exists.
        try {
            Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        // Move to existing target file with REPLACE_EXISTING copy option.
        reset();
        Files.createFile(NON_EXISTENT_FILE_PATH);
        writeToFile(DATA_FILE_PATH, TEST_FILE_DATA_2);
        Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, REPLACE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(NON_EXISTENT_FILE_PATH));

        // Copy from a non existent file.
        reset();
        try {
            Files.move(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH, REPLACE_EXISTING);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_move_CopyOption() throws IOException {
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        Files.setAttribute(DATA_FILE_PATH, "basic:lastModifiedTime", fileTime);
        Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
        assertEquals(fileTime.to(TimeUnit.SECONDS),
                ((FileTime)Files.getAttribute(NON_EXISTENT_FILE_PATH,
                        "basic:lastModifiedTime")).to(TimeUnit.SECONDS));
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // ATOMIC_MOVE
        reset();
        Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, ATOMIC_MOVE);
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        reset();
        try {
            Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, NonStandardOption.OPTION1);
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public void test_move_NPE() throws IOException {
        try {
            Files.move(null, NON_EXISTENT_FILE_PATH);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.move(DATA_FILE_PATH, null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.move(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, (CopyOption[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_move_directory() throws IOException {
        final Path dirPath = Paths.get(TEST_DIR, "dir1");
        final Path nestedDirPath = Paths.get(TEST_DIR, "dir1/dir");
        final Path dirPath2 = Paths.get(TEST_DIR, "dir2");

        Files.createDirectory(dirPath);
        Files.createDirectory(nestedDirPath);
        Files.copy(DATA_FILE_PATH, Paths.get(TEST_DIR, "dir1/" + DATA_FILE));
        Files.move(dirPath, dirPath2);

        Map<Path, Boolean> pathMap = new HashMap<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath2);
        directoryStream.forEach(file -> pathMap.put(file, true));

        // The files are not copied. The command is equivalent of creating a new directory.
        assertEquals(2, pathMap.size());
        assertEquals(TEST_FILE_DATA, readFromFile(Paths.get(TEST_DIR, "dir2/" + DATA_FILE)));
        assertFalse(Files.exists(dirPath));

        reset();
        Path dirPath4 = Paths.get(TEST_DIR, "dir4");
        Files.createDirectory(dirPath);
        Files.createDirectory(dirPath4);
        Files.createFile(Paths.get(dirPath.toString(), DATA_FILE));
        Files.createFile(Paths.get(dirPath4.toString(), DATA_FILE));
        try {
            Files.copy(dirPath, dirPath4, REPLACE_EXISTING);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    public void test_readSymbolicLink() throws IOException {
        Files.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                /* Path of the target of the symbolic link */ DATA_FILE_PATH.toAbsolutePath());
        assertEquals(DATA_FILE_PATH.toAbsolutePath(), Files.readSymbolicLink(NON_EXISTENT_FILE_PATH));

        // Sym link to itself
        reset();
        Files.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                /* Path of the target of the symbolic link */ NON_EXISTENT_FILE_PATH.toAbsolutePath());
        assertEquals(NON_EXISTENT_FILE_PATH.toAbsolutePath(), Files.readSymbolicLink(NON_EXISTENT_FILE_PATH));

        reset();
        try {
            Files.readSymbolicLink(DATA_FILE_PATH);
            fail();
        } catch (NotLinkException expected) {
        }
    }

    public void test_readSymbolicLink_NPE() throws IOException {
        try {
            Files.readSymbolicLink(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_isSameFile() throws IOException {
        // When both the files exists.
        assertTrue(Files.isSameFile(DATA_FILE_PATH, DATA_FILE_PATH));

        // When the files doesn't exist.
        assertTrue(Files.isSameFile(NON_EXISTENT_FILE_PATH, NON_EXISTENT_FILE_PATH));

        // With two different files.
        try {
            assertFalse(Files.isSameFile(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_isSameFile_NPE() throws IOException {
        try {
            Files.isSameFile(null, DATA_FILE_PATH);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.isSameFile(DATA_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_getFileStore() throws IOException {
        FileStore fileStore = Files.getFileStore(DATA_FILE_PATH);
        assertNotNull(fileStore);
    }

    public void test_getFileStore_NPE() throws IOException {
        try {
            Files.getFileStore(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_isHidden() throws IOException {
        assertFalse(Files.isHidden(DATA_FILE_PATH));
        Files.setAttribute(DATA_FILE_PATH, "dos:hidden", true);

        // Files can't be hid.
        assertFalse(Files.isHidden(DATA_FILE_PATH));
    }

    public void test_isHidden_NPE() throws IOException {
        try {
            Files.isHidden(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_probeContentType() throws IOException {
        assertEquals("text/plain", Files.probeContentType(Paths.get(TEST_DIR, "file.txt")));
        assertEquals("text/x-java", Files.probeContentType(Paths.get(TEST_DIR, "file.java")));
    }

    public void test_probeContentType_NPE() throws IOException {
        try {
            Files.probeContentType(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_getFileAttributeView() throws IOException {
        BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(DATA_FILE_PATH,
                BasicFileAttributeView.class);

        assertTrue(fileAttributeView.readAttributes().isRegularFile());
        assertFalse(fileAttributeView.readAttributes().isDirectory());
        assertEquals(getFileKey(DATA_FILE_PATH), fileAttributeView.readAttributes().fileKey());
    }

    public void test_getFileAttributeView_NPE() throws IOException {
        try {
            Files.getFileAttributeView(null, BasicFileAttributeView.class);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.getFileAttributeView(DATA_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_readAttributes() throws IOException {
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        Files.setAttribute(DATA_FILE_PATH, "basic:lastModifiedTime", fileTime);
        BasicFileAttributes basicFileAttributes = Files.readAttributes(DATA_FILE_PATH,
                BasicFileAttributes.class);
        FileTime lastModifiedTime = basicFileAttributes.lastModifiedTime();
        assertEquals(fileTime.to(TimeUnit.SECONDS), lastModifiedTime.to(TimeUnit.SECONDS));

        // When file is NON_EXISTENT.
        try {
            Files.readAttributes(NON_EXISTENT_FILE_PATH, BasicFileAttributes.class);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_readAttributes_NPE() throws IOException {
        try {
            Files.readAttributes(DATA_FILE_PATH, (Class<BasicFileAttributes>) null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.readAttributes(null, BasicFileAttributes.class);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_setAttribute() throws IOException {
        // Other tests are covered in test_readAttributes.
        // When file is NON_EXISTENT.
        try {
            FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis());
            Files.setAttribute(NON_EXISTENT_FILE_PATH, "basic:lastModifiedTime", fileTime);
            fail();
        } catch (NoSuchFileException expected) {}

        // ClassCastException
        try {
            Files.setAttribute(DATA_FILE_PATH, "basic:lastModifiedTime", 10);
            fail();
        } catch (ClassCastException expected) {}

        // IllegalArgumentException
        try {
            Files.setAttribute(DATA_FILE_PATH, "xyz", 10);
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            Files.setAttribute(null, "xyz", 10);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.setAttribute(DATA_FILE_PATH, null, 10);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_getAttribute() throws IOException {
        // Other tests are covered in test_readAttributes.
        // When file is NON_EXISTENT.
        try {
           Files.getAttribute(NON_EXISTENT_FILE_PATH, "basic:lastModifiedTime");
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_getAttribute_Exception() throws IOException {
        // IllegalArgumentException
        try {
            Files.getAttribute(DATA_FILE_PATH, "xyz");
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            Files.getAttribute(null, "xyz");
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.getAttribute(DATA_FILE_PATH, null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_getPosixFilePermissions() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.createFile(NON_EXISTENT_FILE_PATH, attr);
        assertEquals(attr.value(), Files.getPosixFilePermissions(NON_EXISTENT_FILE_PATH));
    }

    public void test_getPosixFilePermissions_NPE() throws IOException {
        try {
            Files.getPosixFilePermissions(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_setPosixFilePermissions() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.setPosixFilePermissions(DATA_FILE_PATH, perm);
        assertEquals(attr.value(), Files.getPosixFilePermissions(DATA_FILE_PATH));
    }

    public void test_setPosixFilePermissions_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            Files.setPosixFilePermissions(null, perm);
        } catch(NullPointerException expected) {}

        try {
            Files.setPosixFilePermissions(DATA_FILE_PATH, null);
        } catch(NullPointerException expected) {}
    }

    public void test_getOwner() throws IOException, InterruptedException {
        String[] statCmd = {"stat", "-c", "%U", TEST_DIR + "/" + DATA_FILE};
        Process statProcess = execCmdAndWaitForTermination(statCmd);
        String owner = readFromInputStream(statProcess.getInputStream()).trim();
        assertEquals(owner, Files.getOwner(DATA_FILE_PATH).getName());
    }

    public void test_getOwner_NPE() throws IOException, InterruptedException {
        try {
            Files.getOwner(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_setOwner() throws IOException {
        // TODO: unable to set the owner due to insufficient permissions.
    }

    public void test_isSymbolicLink() throws IOException, InterruptedException {
        assertFalse(Files.isSymbolicLink(NON_EXISTENT_FILE_PATH));
        assertFalse(Files.isSymbolicLink(DATA_FILE_PATH));

        // Creating a symbolic link.
        String[] symLinkCmd = {"ln", "-s", DATA_FILE, TEST_DIR + "/" + NON_EXISTENT_FILE};
        execCmdAndWaitForTermination(symLinkCmd);
        assertTrue(Files.isSymbolicLink(NON_EXISTENT_FILE_PATH));
    }

    public void test_isSymbolicLink_NPE() throws IOException, InterruptedException {
        try {
            Files.isSymbolicLink(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_isDirectory() throws IOException, InterruptedException {
        assertFalse(Files.isDirectory(DATA_FILE_PATH));
        // When file doesn't exist.
        assertFalse(Files.isDirectory(NON_EXISTENT_FILE_PATH));

        // Creating a directory.
        String dirName = "newDir";
        Path dirPath = Paths.get(TEST_DIR, dirName);
        String mkdir[] = {"mkdir", TEST_DIR + "/" + dirName};
        execCmdAndWaitForTermination(mkdir);
        assertTrue(Files.isDirectory(dirPath));
    }

    public void test_isDirectory_NPE() throws IOException {
        try {
            Files.isDirectory(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_isRegularFile() throws IOException, InterruptedException {
        assertTrue(Files.isRegularFile(DATA_FILE_PATH));
        // When file doesn't exist.
        assertFalse(Files.isRegularFile(NON_EXISTENT_FILE_PATH));

        // Check directories.
        Path dirPath = Paths.get(TEST_DIR, "dir");
        Files.createDirectory(dirPath);
        assertFalse(Files.isRegularFile(dirPath));

        // Check symbolic link.
        // When linked to itself.
        Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, NON_EXISTENT_FILE_PATH.toAbsolutePath());
        assertFalse(Files.isRegularFile(NON_EXISTENT_FILE_PATH));

        // When linked to some other file.
        reset();
        Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH.toAbsolutePath());
        assertTrue(Files.isRegularFile(NON_EXISTENT_FILE_PATH));

        // When asked to not follow the link.
        assertFalse(Files.isRegularFile(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));

        // Device file.
        Path deviceFilePath = Paths.get("/dev/null");
        assertTrue(Files.exists(deviceFilePath));
        assertFalse(Files.isRegularFile(deviceFilePath));
    }

    public void test_isRegularFile_NPE() throws IOException {
        try {
            Files.isReadable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_getLastModifiedTime() throws IOException, InterruptedException {
        String touchCmd[] = {"touch", "-d", "2015-10-09T00:00:00", TEST_DIR + "/" + DATA_FILE};
        execCmdAndWaitForTermination(touchCmd);
        assertEquals("2015-10-09T00:00:00Z", Files.getLastModifiedTime(DATA_FILE_PATH).toString());

        // Non existent file.
        try {
            Files.getLastModifiedTime(NON_EXISTENT_FILE_PATH).toString();
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_getLastModifiedTime_NPE() throws IOException {
        try {
            Files.getLastModifiedTime(null, LinkOption.NOFOLLOW_LINKS);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.getLastModifiedTime(DATA_FILE_PATH, (LinkOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_setLastModifiedTime() throws IOException, InterruptedException {
        long timeInMillisToBeSet = System.currentTimeMillis() - 10000;
        Files.setLastModifiedTime(DATA_FILE_PATH, FileTime.fromMillis(timeInMillisToBeSet));
        assertEquals(timeInMillisToBeSet/1000,
                Files.getLastModifiedTime(DATA_FILE_PATH).to(TimeUnit.SECONDS));

        // Non existent file.
        try {
            Files.setLastModifiedTime(NON_EXISTENT_FILE_PATH,
                    FileTime.fromMillis(timeInMillisToBeSet));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_setLastModifiedTime_NPE() throws IOException, InterruptedException {
        try {
            Files.setLastModifiedTime(null, FileTime.fromMillis(System.currentTimeMillis()));
            fail();
        } catch (NullPointerException expected) {}

        // No NullPointerException.
        Files.setLastModifiedTime(DATA_FILE_PATH, null);
    }

    public void test_size() throws IOException, InterruptedException {
        int testSizeInBytes = 5000;
        String ddCmd[] = {"dd", "if=/dev/zero", "of=" + TEST_DIR + "/" + DATA_FILE, "bs="
                + testSizeInBytes, "count=1"};
        execCmdAndWaitForTermination(ddCmd);

        assertEquals(testSizeInBytes, Files.size(DATA_FILE_PATH));

        try {
            Files.size(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_size_NPE() throws IOException, InterruptedException {
        try {
            Files.size(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_exists() throws IOException {
        // When file exists.
        assertTrue(Files.exists(DATA_FILE_PATH));

        // When file doesn't exist.
        assertFalse(Files.exists(NON_EXISTENT_FILE_PATH));

        // SymLink
        Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH.toAbsolutePath());
        assertTrue(Files.exists(NON_EXISTENT_FILE_PATH));

        // When link shouldn't be followed
        assertTrue(Files.exists(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));

        // When the target file doesn't exist.
        Files.delete(DATA_FILE_PATH);
        assertTrue(Files.exists(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(NON_EXISTENT_FILE_PATH));

        // Symlink to itself
        reset();
        Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, NON_EXISTENT_FILE_PATH.toAbsolutePath());
        assertFalse(Files.exists(NON_EXISTENT_FILE_PATH));
        assertTrue(Files.exists(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));
    }

    public void test_exists_NPE() throws IOException {
        try {
            Files.exists(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_notExists() throws IOException {
        // When file exists.
        assertFalse(Files.notExists(DATA_FILE_PATH));

        // When file doesn't exist.
        assertTrue(Files.notExists(NON_EXISTENT_FILE_PATH));

        // SymLink
        Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH.toAbsolutePath());
        assertFalse(Files.notExists(NON_EXISTENT_FILE_PATH));

        // When link shouldn't be followed
        assertFalse(Files.notExists(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));

        // When the target file doesn't exist.
        Files.delete(DATA_FILE_PATH);
        assertFalse(Files.notExists(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));
        assertTrue(Files.notExists(NON_EXISTENT_FILE_PATH));

        // Symlink to itself
        reset();
        Files.createSymbolicLink(NON_EXISTENT_FILE_PATH, NON_EXISTENT_FILE_PATH.toAbsolutePath());
        assertFalse(Files.notExists(NON_EXISTENT_FILE_PATH));
        assertFalse(Files.notExists(NON_EXISTENT_FILE_PATH, LinkOption.NOFOLLOW_LINKS));
    }

    public void test_notExists_NPE() throws IOException {
        try {
            Files.notExists(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.notExists(DATA_FILE_PATH, (LinkOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_isReadable() throws IOException {
        // When a readable file is available.
        assertTrue(Files.isReadable(DATA_FILE_PATH));

        // When a file doesn't exist.
        assertFalse(Files.isReadable(NON_EXISTENT_FILE_PATH));

        // Setting non readable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("-wxrwxrwx");
        Files.setPosixFilePermissions(DATA_FILE_PATH, perm);
        assertFalse(Files.isReadable(DATA_FILE_PATH));
    }

    public void test_isReadable_NPE() throws IOException {
        try {
            Files.isReadable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_isWritable() throws IOException {
        // When a readable file is available.
        assertTrue(Files.isWritable(DATA_FILE_PATH));

        // When a file doesn't exist.
        assertFalse(Files.isWritable(NON_EXISTENT_FILE_PATH));

        // Setting non writable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("r-xrwxrwx");
        Files.setPosixFilePermissions(DATA_FILE_PATH, perm);
        assertFalse(Files.isWritable(DATA_FILE_PATH));
    }

    public void test_isWritable_NPE() {
        try {
            Files.isWritable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_isExecutable() throws IOException {
        // When a readable file is available.
        assertFalse(Files.isExecutable(DATA_FILE_PATH));

        // When a file doesn't exist.
        assertFalse(Files.isExecutable(NON_EXISTENT_FILE_PATH));

        // Setting non executable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rw-rwxrwx");
        Files.setPosixFilePermissions(DATA_FILE_PATH, perm);
        assertFalse(Files.isExecutable(DATA_FILE_PATH));
    }

    public void test_isExecutable_NPE() {
        try {
            Files.isExecutable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_walkFileTree$Path$Set$int$FileVisitor_symbolicLinkFollow()
            throws IOException, InterruptedException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   └── dir2 ─ file1
        //        │
        //        └── file2
        //
        // depth will be 1. file1 cannot be reached

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path file1 = Paths.get(TEST_DIR, "root/dir1/dir2/file1");
        Path file2 = Paths.get(TEST_DIR, "root/file2");

        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createSymbolicLink(file2, file1.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(file2));

        Map<Object, Boolean> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(rootDir, option, 2, new TestFileVisitor(dirMap, option));

        assertTrue(dirMap.get(getFileKey(file1)));
    }

    public void test_walkFileTree$Path$FileVisitor() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path dir3 = Paths.get(TEST_DIR, "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(TEST_DIR, "root/dir1/dir4");
        Path dir5 = Paths.get(TEST_DIR, "root/dir5");
        Path file1 = Paths.get(TEST_DIR, "root/file1");
        Path file3 = Paths.get(TEST_DIR, "root/dir1/file3");
        Path file5 = Paths.get(TEST_DIR, "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file3);
        Files.createFile(file5);
        Files.createSymbolicLink(file1, DATA_FILE_PATH.toAbsolutePath());

        Map<Object, Boolean> dirMap = new HashMap<>();
        Path returnedPath = Files.walkFileTree(rootDir, new TestFileVisitor(dirMap));

        assertEquals(rootDir, returnedPath);

        assertTrue(dirMap.get(getFileKey(rootDir)));
        assertTrue(dirMap.get(getFileKey(dir1)));
        assertTrue(dirMap.get(getFileKey(dir2)));
        assertTrue(dirMap.get(getFileKey(dir3)));
        assertTrue(dirMap.get(getFileKey(file5)));
        assertTrue(dirMap.get(getFileKey(dir4)));
        assertTrue(dirMap.get(getFileKey(file3)));
        assertTrue(dirMap.get(getFileKey(dir5)));
        assertTrue(dirMap.get(getFileKey(file1)));
        assertFalse(dirMap.containsKey(getFileKey(DATA_FILE_PATH)));
    }

    public void test_walkFileTree_depthFirst() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        ├── dir1 ── file1
        //        └── dir2 ── file2

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir2");
        Path file1 = Paths.get(TEST_DIR, "root/dir1/file1");
        Path file2 = Paths.get(TEST_DIR, "root/dir2/file2");

        Files.createDirectories(dir1);
        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createFile(file2);

        Map<Object, Boolean> dirMap = new HashMap<>();
        List<Object> keyList = new ArrayList<>();
        Files.walkFileTree(rootDir, new TestFileVisitor(dirMap, keyList));
        assertEquals(getFileKey(rootDir), keyList.get(0));
        if (keyList.get(1).equals(getFileKey(dir1))) {
            assertEquals(getFileKey(file1), keyList.get(2));
            assertEquals(getFileKey(dir2), keyList.get(3));
            assertEquals(getFileKey(file2), keyList.get(4));
        } else if (keyList.get(1).equals(getFileKey(dir2))){
            assertEquals(getFileKey(file2), keyList.get(2));
            assertEquals(getFileKey(dir1), keyList.get(3));
            assertEquals(getFileKey(file1), keyList.get(4));
        } else {
            fail();
        }
    }

    public void test_walkFileTree_negativeDepth() throws IOException {
        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");

        Files.createDirectories(dir1);

        Map<Object, Boolean> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(rootDir, option, -1, new TestFileVisitor(dirMap));
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_walkFileTree_maximumDepth() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //
        // depth will be 2. file5, dir3 is not reachable.

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path dir3 = Paths.get(TEST_DIR, "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(TEST_DIR, "root/dir1/dir4");
        Path dir5 = Paths.get(TEST_DIR, "root/dir5");
        Path file1 = Paths.get(TEST_DIR, "root/file1");
        Path file3 = Paths.get(TEST_DIR, "root/dir1/file3");
        Path file5 = Paths.get(TEST_DIR, "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Map<Object, Boolean> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(rootDir, option, 2, new TestFileVisitor(dirMap));

        assertEquals(7, dirMap.size());
        assertTrue(dirMap.getOrDefault(getFileKey(rootDir), false));
        assertTrue(dirMap.getOrDefault(getFileKey(dir1), false));
        assertTrue(dirMap.getOrDefault(getFileKey(dir2), false));
        assertTrue(dirMap.getOrDefault(getFileKey(file1), false));
        assertTrue(dirMap.getOrDefault(getFileKey(file3), false));
        assertFalse(dirMap.getOrDefault(getFileKey(file5), false));
        assertFalse(dirMap.getOrDefault(getFileKey(dir3), false));
        assertTrue(dirMap.getOrDefault(getFileKey(dir5), false));
        assertTrue(dirMap.getOrDefault(getFileKey(dir4), false));
    }

    public void test_walkFileTree$Path$FileVisitor_NPE() throws IOException {
        Path rootDir = Paths.get(TEST_DIR, "root");
        try {
            Files.walkFileTree(null, new TestFileVisitor(new HashMap<>()));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.walkFileTree(rootDir, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_walkFileTree$Path$FileVisitor_FileSystemLoopException() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        └── dir1
        //             └── file1
        //
        // file1 is symlink to dir1

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path file1 = Paths.get(TEST_DIR, "root/dir1/file1");

        Files.createDirectories(dir1);
        Files.createSymbolicLink(file1, dir1.toAbsolutePath());
        assertEquals(getFileKey(dir1), getFileKey(Files.readSymbolicLink(file1)));

        Map<Object, Boolean> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(rootDir, option, Integer.MAX_VALUE, new TestFileVisitor(dirMap));
            fail();
        } catch (FileSystemLoopException expected) {}
    }

    public void test_newBufferedReader() throws IOException {
        // When file doesn't exists.
        try {
            Files.newBufferedReader(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (NoSuchFileException expected) {}

        BufferedReader bufferedReader = Files.newBufferedReader(DATA_FILE_PATH);
        assertEquals(TEST_FILE_DATA, bufferedReader.readLine());

        // When the file has unicode characters.
        writeToFile(DATA_FILE_PATH, "परीक्षण");
        bufferedReader = Files.newBufferedReader(DATA_FILE_PATH);
        assertEquals("परीक्षण", bufferedReader.readLine());
        bufferedReader.close();

        // When file is write-only.
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("-w-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.setPosixFilePermissions(DATA_FILE_PATH, perm);
        try {
            Files.newBufferedReader(DATA_FILE_PATH);
            fail();
        } catch (AccessDeniedException expected) {}
    }

    public void test_newBufferedReader_NPE() throws IOException {
        try {
            Files.newBufferedReader(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newBufferedReader$Path$Charset() throws IOException {
        BufferedReader bufferedReader = Files.newBufferedReader(DATA_FILE_PATH,
                Charset.forName("US-ASCII"));
        assertEquals(TEST_FILE_DATA, bufferedReader.readLine());

        // When the file has unicode characters.
        writeToFile(DATA_FILE_PATH, "परीक्षण");
        bufferedReader = Files.newBufferedReader(DATA_FILE_PATH, Charset.forName("US-ASCII"));
        try {
            bufferedReader.readLine();
            fail();
        } catch (MalformedInputException expected) {}
    }

    public void test_newBufferedReader$Path$Charset_NPE() throws IOException {
        try {
            Files.newBufferedReader(null, Charset.forName("US-ASCII"));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.newBufferedReader(DATA_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newBufferedWriter() throws IOException {
        BufferedWriter bufferedWriter = Files.newBufferedWriter(NON_EXISTENT_FILE_PATH);
        bufferedWriter.write(TEST_FILE_DATA);
        bufferedWriter.close();
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // When file exists, it should start writing from the beginning.
        bufferedWriter = Files.newBufferedWriter(DATA_FILE_PATH);
        bufferedWriter.write(TEST_FILE_DATA_2);
        bufferedWriter.close();
        assertEquals(TEST_FILE_DATA_2, readFromFile(DATA_FILE_PATH));

        // When file is read-only.
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("r--------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.setPosixFilePermissions(DATA_FILE_PATH, perm);
        try {
            Files.newBufferedWriter(DATA_FILE_PATH);
            fail();
        } catch (AccessDeniedException expected) {}
    }

    public void test_newBufferedWriter_NPE() throws IOException {
        try {
            Files.newBufferedWriter(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newBufferedWriter$Path$Charset() throws IOException {
        BufferedWriter bufferedWriter = Files.newBufferedWriter(NON_EXISTENT_FILE_PATH,
                Charset.forName("US-ASCII"));
        bufferedWriter.write(TEST_FILE_DATA);
        bufferedWriter.close();
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // Writing unicode characters when charset is US-ASCII
        bufferedWriter = Files.newBufferedWriter(NON_EXISTENT_FILE_PATH,
                Charset.forName("US-ASCII"));
        bufferedWriter.write("परीक्षण" + TEST_FILE_DATA);
        assertEquals("", readFromFile(NON_EXISTENT_FILE_PATH));
    }

    public void test_newBufferedWriter$Path$Charset_NPE() throws IOException {
        try {
            Files.newBufferedWriter(null, Charset.forName("US-ASCII"));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.newBufferedWriter(NON_EXISTENT_FILE_PATH, (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_copy$InputStream$Path$CopyOption() throws IOException {
        InputStream is = new ByteArrayInputStream(TEST_FILE_DATA.getBytes());
        Files.copy(is, NON_EXISTENT_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // When file exists.
        try {
            Files.copy(is, NON_EXISTENT_FILE_PATH);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        // With COPYOPTION replace.
        assertTrue(Files.exists(NON_EXISTENT_FILE_PATH));
        is = new ByteArrayInputStream(TEST_FILE_DATA_2.getBytes());
        Files.copy(is, NON_EXISTENT_FILE_PATH, REPLACE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(NON_EXISTENT_FILE_PATH));

        // Non Standard options
        try {
            Files.copy(is, NON_EXISTENT_FILE_PATH, REPLACE_EXISTING, ATOMIC_MOVE);
            fail();
        } catch (UnsupportedOperationException expected) {}

        // Symbolic links

        Path symLink = Paths.get(TEST_DIR, "symlink");
        Files.createSymbolicLink(symLink, DATA_FILE_PATH.toAbsolutePath());
        try {
            Files.copy(is, symLink);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        // With REPLACE_EXISTING
        is = new ByteArrayInputStream(TEST_FILE_DATA_2.getBytes());
        Files.copy(is, symLink, REPLACE_EXISTING);
        assertFalse(Files.isSymbolicLink(symLink));
    }

    public void test_copy$InputStream$Path$CopyOption_NPE() throws IOException {
        InputStream is = new ByteArrayInputStream(TEST_FILE_DATA.getBytes());
        try {
            Files.copy((InputStream) null, NON_EXISTENT_FILE_PATH);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.copy(is, null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_copy$Path$OutputStream() throws IOException {
        Files.copy(DATA_FILE_PATH, Files.newOutputStream(NON_EXISTENT_FILE_PATH));
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // When file doesn't exist.
        reset();
        try {
            Files.copy(NON_EXISTENT_FILE_PATH, Files.newOutputStream(DATA_FILE_PATH));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_copy$Path$OutputStream_NPE() throws IOException {
        try {
            Files.copy(null, Files.newOutputStream(NON_EXISTENT_FILE_PATH));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.copy(DATA_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_readAllByte() throws IOException {
        String holder = new String(Files.readAllBytes(DATA_FILE_PATH));
        assertEquals(TEST_FILE_DATA, holder);

        // When file doesn't exist.
        try {
            Files.readAllBytes(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (NoSuchFileException expected) {}

        // When file is too long.
        Path devZero = Paths.get("/dev/zero");
        try {
            Files.readAllBytes(devZero);
            fail();
        } catch (OutOfMemoryError expected) {}
    }

    public void test_readAllByte_NPE() throws IOException {
        try {
            Files.readAllBytes(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_readAllLine() throws IOException {
        // Multi-line file.
        assertTrue(Files.exists(DATA_FILE_PATH));
        writeToFile(DATA_FILE_PATH, "\n" + TEST_FILE_DATA_2, APPEND);
        List<String> out = Files.readAllLines(DATA_FILE_PATH);
        assertEquals(2, out.size());
        assertEquals(TEST_FILE_DATA, out.get(0));
        assertEquals(TEST_FILE_DATA_2, out.get(1));

        reset();
        try {
            Files.readAllLines(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_readAllLine_NPE() throws IOException {
        try {
            Files.readAllLines(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_readAllLine$Path$Charset() throws IOException {
        assertTrue(Files.exists(DATA_FILE_PATH));
        writeToFile(DATA_FILE_PATH, "\n" + TEST_FILE_DATA_2, APPEND);
        List<String> out = Files.readAllLines(DATA_FILE_PATH, Charset.forName("UTF-8"));
        assertEquals(2, out.size());
        assertEquals(TEST_FILE_DATA, out.get(0));
        assertEquals(TEST_FILE_DATA_2, out.get(1));

        // With UTF-16.
        out = Files.readAllLines(DATA_FILE_PATH, Charset.forName("UTF-16"));
        assertEquals(1, out.size());
        // UTF-8 data read as UTF-16
        assertEquals("桥汬漊瑥獴", out.get(0));


        // When file doesn't exist.
        reset();
        try {
            Files.readAllLines(NON_EXISTENT_FILE_PATH, Charset.forName("UTF-8"));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_readAllLine$Path$Charset_NPE() throws IOException {
        try {
            Files.readAllLines(null, Charset.forName("UTF-16"));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.readAllLines(DATA_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_write$Path$byte$OpenOption() throws IOException {
        Files.write(DATA_FILE_PATH, TEST_FILE_DATA_2.getBytes());
        assertEquals(TEST_FILE_DATA_2, readFromFile(DATA_FILE_PATH));
    }

    public void test_write$Path$byte$OpenOption_OpenOption() throws IOException {
        Files.write(NON_EXISTENT_FILE_PATH, TEST_FILE_DATA_2.getBytes(), CREATE_NEW);
        assertEquals(TEST_FILE_DATA_2, readFromFile(NON_EXISTENT_FILE_PATH));

        reset();
        Files.write(DATA_FILE_PATH, TEST_FILE_DATA_2.getBytes(), TRUNCATE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(DATA_FILE_PATH));

        reset();
        Files.write(DATA_FILE_PATH, TEST_FILE_DATA_2.getBytes(), APPEND);
        assertEquals(TEST_FILE_DATA + TEST_FILE_DATA_2, readFromFile(DATA_FILE_PATH));

        reset();
        try {
            Files.write(DATA_FILE_PATH, TEST_FILE_DATA_2.getBytes(), READ);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_write$Path$byte$OpenOption_NPE() throws IOException {
        try {
            Files.write(null, TEST_FILE_DATA_2.getBytes(), CREATE_NEW);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(NON_EXISTENT_FILE_PATH, (byte[]) null, CREATE_NEW);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(NON_EXISTENT_FILE_PATH, TEST_FILE_DATA_2.getBytes(), (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_write$Path$Iterable$Charset$OpenOption() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(DATA_FILE_PATH, lines, Charset.forName("UTF-16"));
        List<String> readLines = Files.readAllLines(DATA_FILE_PATH, Charset.forName("UTF-16"));
        assertEquals(readLines, lines);
    }

    public void test_write$Path$Iterable$Charset$OpenOption_NPE() throws IOException {
        try {
            Files.write(null, new ArrayList<>(), Charset.forName("UTF-16"));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(DATA_FILE_PATH, null, Charset.forName("UTF-16"));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(DATA_FILE_PATH, new ArrayList<>(), (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_write$Path$Iterable$OpenOption() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(DATA_FILE_PATH, lines);
        List<String> readLines = Files.readAllLines(DATA_FILE_PATH);
        assertEquals(readLines, lines);
    }

    public void test_write$Path$Iterable$OpenOption_NPE() throws IOException {
        try {
            Files.write(null, new ArrayList<String>());
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(DATA_FILE_PATH, (Iterable<CharSequence>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_list() throws IOException {
        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path file1 = Paths.get(TEST_DIR, "root/file1");
        Path file2 = Paths.get(TEST_DIR, "root/dir1/file2");
        Path symLink = Paths.get(TEST_DIR, "root/symlink");
        Files.createDirectories(dir1);
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createSymbolicLink(symLink, file1.toAbsolutePath());
        Map<Path, Boolean> visitedFiles = new HashMap<>();
        try (Stream<Path> pathStream = Files.list(rootDir)) {
            pathStream.forEach(path -> visitedFiles.put(path, true));
        }
        assertEquals(3, visitedFiles.size());
        assertTrue(visitedFiles.get(dir1));
        assertTrue(visitedFiles.get(file1));
        assertTrue(visitedFiles.get(symLink));
        assertFalse(visitedFiles.containsKey(file2));

        // When directory doesn't exist.
        reset();
        try {
            Files.list(Paths.get(TEST_DIR, "newDir"));
            fail();
        } catch (NoSuchFileException expected) {}

        reset();
        try {
            Files.list(DATA_FILE_PATH);
            fail();
        } catch (NotDirectoryException expected) {}
    }

    public void test_list_NPE() throws IOException {
        try {
            Files.list(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_walk() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //
        // depth will be 2. file4, file5, dir3 is not reachable.

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path dir3 = Paths.get(TEST_DIR, "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(TEST_DIR, "root/dir1/dir4");
        Path dir5 = Paths.get(TEST_DIR, "root/dir5");
        Path file1 = Paths.get(TEST_DIR, "root/file1");
        Path file3 = Paths.get(TEST_DIR, "root/dir1/file3");
        Path file5 = Paths.get(TEST_DIR, "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Map<Path, Boolean> dirMap = new HashMap<>();
        try(Stream<Path> pathStream = Files.walk(rootDir, 2, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> dirMap.put(path, true));
        }

        assertEquals(7, dirMap.size());
        assertTrue(dirMap.get(rootDir));
        assertTrue(dirMap.get(dir1));
        assertTrue(dirMap.get(dir2));
        assertFalse(dirMap.containsKey(dir3));
        assertFalse(dirMap.containsKey(file5));
        assertTrue(dirMap.get(dir4));
        assertTrue(dirMap.get(file3));
        assertTrue(dirMap.get(dir5));
        assertTrue(dirMap.get(file1));
    }

    public void test_walk_FileSystemLoopException() throws IOException {
        // Directory structure.
        //        root
        //        └── dir1
        //            └── file1
        //
        // file1 is symbolic link to dir1

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir");
        Path file1 = Paths.get(TEST_DIR, "root/dir/file1");
        Files.createDirectories(dir1);
        Files.createSymbolicLink(file1, dir1.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(file1));
        try(Stream<Path> pathStream = Files.walk(rootDir, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> assertNotNull(path));
            fail();
        } catch (UncheckedIOException expected) {
            assertTrue(expected.getCause() instanceof FileSystemLoopException);
        }
    }

    public void test_walk_symbolicLinks() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   └── dir2 ─ file1
        //        │
        //        └── file2
        //
        // depth will be 1. file1 cannot be reached

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path file1 = Paths.get(TEST_DIR, "root/dir1/dir2/file1");
        Path file2 = Paths.get(TEST_DIR, "root/file2");

        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createSymbolicLink(file2, file1.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(file2));

        Map<Object, Boolean> dirMap = new HashMap<>();
        try (Stream<Path> pathStream = Files.walk(rootDir, 2, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> {
                try {
                    dirMap.put(getFileKey(path), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        assertEquals(4, dirMap.size());
        assertTrue(dirMap.getOrDefault(getFileKey(rootDir), false));
        assertTrue(dirMap.getOrDefault(getFileKey(dir1), false));
        assertTrue(dirMap.getOrDefault(getFileKey(dir2), false));
        assertFalse(dirMap.getOrDefault(getFileKey(file2), false));
        assertTrue(dirMap.getOrDefault(getFileKey(file1), false));
    }

    public void test_walk$Path$FileVisitOption() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path dir3 = Paths.get(TEST_DIR, "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(TEST_DIR, "root/dir1/dir4");
        Path dir5 = Paths.get(TEST_DIR, "root/dir5");
        Path file1 = Paths.get(TEST_DIR, "root/file1");
        Path file3 = Paths.get(TEST_DIR, "root/dir1/file3");
        Path file5 = Paths.get(TEST_DIR, "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Map<Path, Boolean> dirMap = new HashMap<>();
        try (Stream<Path> pathStream = Files.walk(rootDir)) {
            pathStream.forEach(path -> dirMap.put(path, true));
        }
        assertTrue(dirMap.get(rootDir));
        assertTrue(dirMap.get(dir1));
        assertTrue(dirMap.get(dir2));
        assertTrue(dirMap.containsKey(dir3));
        assertTrue(dirMap.containsKey(file5));
        assertTrue(dirMap.get(dir4));
        assertTrue(dirMap.get(file3));
        assertTrue(dirMap.get(dir5));
        assertTrue(dirMap.get(file1));
    }

    public void test_walk_depthFirst() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   └── file1
        //        └── dir2
        //            └── file2
        //

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path file1 = Paths.get(TEST_DIR, "root/dir1/file1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir2");
        Path file2 = Paths.get(TEST_DIR, "root/dir2/file2");
        Files.createDirectories(dir1);
        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createFile(file2);
        List<Object> fileKeyList = new ArrayList<>();
        try(Stream<Path> pathStream = Files.walk(rootDir, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> {
                try {
                    fileKeyList.add(getFileKey(path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        assertEquals(getFileKey(rootDir), fileKeyList.get(0));
        if (fileKeyList.get(1).equals(getFileKey(dir1))) {
            assertEquals(getFileKey(file1), fileKeyList.get(2));
            assertEquals(getFileKey(dir2), fileKeyList.get(3));
            assertEquals(getFileKey(file2), fileKeyList.get(4));
        } else if (fileKeyList.get(1).equals(getFileKey(dir2))) {
            assertEquals(getFileKey(file2), fileKeyList.get(2));
            assertEquals(getFileKey(dir1), fileKeyList.get(3));
            assertEquals(getFileKey(file1), fileKeyList.get(4));
        } else {
            fail();
        }
    }

    public void test_walk$Path$Int$LinkOption_IllegalArgumentException() throws IOException {
        Map<Path, Boolean> dirMap = new HashMap<>();
        Path rootDir = Paths.get(TEST_DIR, "rootDir");
        try (Stream<Path> pathStream = Files.walk(rootDir, -1, FileVisitOption.FOLLOW_LINKS)) {
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_walk$Path$FileVisitOption_NPE() throws IOException {
        try {
            Files.walk(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_find() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //
        // depth will be 2. file4, file5, dir3 is not reachable.

        Path rootDir = Paths.get(TEST_DIR, "root");
        Path dir1 = Paths.get(TEST_DIR, "root/dir1");
        Path dir2 = Paths.get(TEST_DIR, "root/dir1/dir2");
        Path dir3 = Paths.get(TEST_DIR, "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(TEST_DIR, "root/dir1/dir4");
        Path dir5 = Paths.get(TEST_DIR, "root/dir5");
        Path file1 = Paths.get(TEST_DIR, "root/file1");
        Path file3 = Paths.get(TEST_DIR, "root/dir1/file3");
        Path file5 = Paths.get(TEST_DIR, "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Map<Path, Boolean> dirMap = new HashMap<>();
        Stream<Path> pathStream = Files.find(rootDir, 2, (path, attr) -> Files.isDirectory(path));

        pathStream.forEach(path -> dirMap.put(path, true));
        assertEquals(5, dirMap.size());
        assertTrue(dirMap.get(rootDir));
        assertTrue(dirMap.get(dir1));
        assertTrue(dirMap.get(dir2));
        assertFalse(dirMap.containsKey(dir3));
        assertFalse(dirMap.containsKey(file5));
        assertTrue(dirMap.get(dir4));
        assertFalse(dirMap.containsKey(file3));
        assertTrue(dirMap.get(dir5));
        assertFalse(dirMap.containsKey(file1));
    }

    public void test_find_NPE() throws IOException {
        Path rootDir = Paths.get(TEST_DIR, "root");
        Files.createDirectories(rootDir);
        try {
            Files.find(null, 2, (path, attr) -> Files.isDirectory(path));
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.find(rootDir, (Integer)null, (path, attr) -> Files.isDirectory(path));
            fail();
        } catch(NullPointerException expected) {}

        try(Stream<Path> pathStream = Files.find(rootDir, 2, null)) {
            pathStream.forEach(path -> {/* do nothing */});
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_line$Path$Charset() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(DATA_FILE_PATH, lines, Charset.forName("UTF-16"));
        try (Stream<String> readLines = Files.lines(DATA_FILE_PATH, Charset.forName("UTF-16"))) {
            Iterator<String> lineIterator = lines.iterator();
            readLines.forEach(line -> assertEquals(line, lineIterator.next()));
        }
    }

    public void test_line$Path$Charset_NPE() throws IOException {
        try {
            Files.lines(null, Charset.forName("UTF-16"));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.lines(DATA_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_line$Path() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(DATA_FILE_PATH, lines, Charset.forName("UTF-8"));
        try (Stream<String> readLines = Files.lines(DATA_FILE_PATH)) {
            Iterator<String> lineIterator = lines.iterator();
            readLines.forEach(line -> assertEquals(line, lineIterator.next()));
        }
    }

    public void test_line$Path_NPE() throws IOException {
        try {
            Files.lines(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    // -- Mock Class --

    private static class TestFileVisitor implements FileVisitor<Path> {

        final Map<Object, Boolean> dirMap;
        LinkOption option[];
        List<Object> keyList;

        public TestFileVisitor(Map<Object, Boolean> dirMap) {
            this(dirMap, (List<Object>) null);
        }

        public TestFileVisitor(Map<Object, Boolean> dirMap, Set<FileVisitOption> option) {
            this.dirMap = dirMap;
            for (FileVisitOption fileVisitOption : option) {
                if (fileVisitOption.equals(FileVisitOption.FOLLOW_LINKS)) {
                    this.option = new LinkOption[0];
                }
            }

            if (this.option == null) {
                this.option = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
            }
        }

        public TestFileVisitor(Map<Object, Boolean> dirMap, List<Object> pathList) {
            this.dirMap = dirMap;
            this.option = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
            keyList = pathList;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            if (keyList != null) {
                keyList.add(getFileKey(dir));
            }
            dirMap.put(getFileKey(dir), false);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (keyList != null) {
                keyList.add(getFileKey(file));
            }
            dirMap.put(getFileKey(file), true);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            return TERMINATE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            if (dirMap.get(Files.readAttributes(dir, BasicFileAttributes.class, option).fileKey()))
            {
                return TERMINATE;
            } else {
                dirMap.put(Files.readAttributes(dir, BasicFileAttributes.class, option).fileKey(),
                        true);
                return CONTINUE;
            }
        }
    }

    //  -- Utilities --

    static void writeToFile(Path file, String data, OpenOption... option) throws IOException {
        OutputStream os = Files.newOutputStream(file, option);
        os.write(data.getBytes());
        os.close();
    }

    static String readFromFile(Path file) throws IOException {
        InputStream is = Files.newInputStream(file);
        return readFromInputStream(is);
    }

    static String readFromInputStream(InputStream is) throws IOException {
        byte[] input = new byte[10000];
        is.read(input);
        return new String(input, "UTF-8").trim();
    }

    static Process execCmdAndWaitForTermination(String... cmdList)
            throws InterruptedException, IOException {
        Process process = Runtime.getRuntime().exec(cmdList);
        // Wait for the process to terminate.
        process.waitFor();
        return process;
    }

    /**
     * Non Standard CopyOptions.
     */
    private enum NonStandardOption implements CopyOption, OpenOption {
        OPTION1,
    }

    static Object getFileKey(Path file) throws IOException {
        return Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)
                    .fileKey();
    }
}