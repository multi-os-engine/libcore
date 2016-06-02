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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;
import static java.nio.file.StandardOpenOption.DSYNC;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.SPARSE;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class FileSystemProviderTest extends FilesSetup {

    private FileSystemProvider provider;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        provider = DATA_FILE_PATH.getFileSystem().provider();
    }

    public void test_newInputStream() throws IOException {
        InputStream is = provider.newInputStream(DATA_FILE_PATH, READ);
        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
    }

    public void test_newInputStream_openOption() throws IOException {
        // Write and Append are not supported.
        try {
            provider.newInputStream(DATA_FILE_PATH, WRITE);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            provider.newInputStream(DATA_FILE_PATH, APPEND);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            provider.newInputStream(DATA_FILE_PATH, NonStandardOption.OPTION1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // Supported options.
        InputStream is = provider.newInputStream(DATA_FILE_PATH, DELETE_ON_CLOSE, CREATE_NEW,
                TRUNCATE_EXISTING, SPARSE, SYNC, DSYNC);
        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
    }

    public void test_newInputStream_twice() throws IOException {
        InputStream is = provider.newInputStream(DATA_FILE_PATH, READ);

        // Open the same file again.
        InputStream is2 = provider.newInputStream(DATA_FILE_PATH, READ);

        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
        assertEquals(TEST_FILE_DATA, readFromInputStream(is2));
    }

    public void test_newInputStream_NPE() throws IOException {
        try {
            provider.newInputStream(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.newInputStream(DATA_FILE_PATH, (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newOutputStream() throws IOException {
        OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH);
        os.write(TEST_FILE_DATA.getBytes());
        os.close();

        InputStream is = provider.newInputStream(NON_EXISTENT_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromInputStream(is));
    }

    public void test_newOutputStream_openOption_READ() throws IOException {
        try {
            OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, READ);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_newOutputStream_openOption_APPEND() throws IOException {
        // When file exists and it contains data.
        OutputStream os = provider.newOutputStream(DATA_FILE_PATH, APPEND);
        os.write(TEST_FILE_DATA.getBytes());
        os.close();

        InputStream is = provider.newInputStream(DATA_FILE_PATH);
        assertEquals(TEST_FILE_DATA + TEST_FILE_DATA, readFromInputStream(is));

        // When file doesn't exist.
        try {
            provider.newOutputStream(NON_EXISTENT_FILE_PATH, APPEND);
            fail();
        } catch (NoSuchFileException expected) {
        }
    }

    public void test_newOutputStream_openOption_TRUNCATE() throws IOException {
        // When file exists.
        try (OutputStream os = provider.newOutputStream(DATA_FILE_PATH, TRUNCATE_EXISTING)) {
            os.write(TEST_FILE_DATA_2.getBytes());
            os.close();

            try (InputStream is = provider.newInputStream(DATA_FILE_PATH)) {
                assertEquals(TEST_FILE_DATA_2, readFromInputStream(is));
            }
        }

        // When file doesn't exist.
        try (OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, TRUNCATE_EXISTING)) {
            fail();
        } catch (NoSuchFileException expected) {
        }
    }

    public void test_newOutputStream_openOption_WRITE() throws IOException {
        // When file exists.
        try (OutputStream os = provider.newOutputStream(DATA_FILE_PATH, WRITE)) {
            os.write(TEST_FILE_DATA_2.getBytes());
            os.close();

            try (InputStream is = provider.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }

        // When file doesn't exist.
        try (OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, WRITE)) {
            fail();
        } catch (NoSuchFileException expected) {
        }
    }

    public void test_newOutputStream_openOption_CREATE() throws IOException {
        // When file exists.
        try (OutputStream os = provider.newOutputStream(DATA_FILE_PATH, CREATE)) {
            os.write(TEST_FILE_DATA_2.getBytes());
            os.close();

            try (InputStream is = provider.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }

        // When file doesn't exist.
        try (OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, CREATE)) {
            os.write(TEST_FILE_DATA.getBytes());
            os.close();

            try (InputStream is = provider.newInputStream(NON_EXISTENT_FILE_PATH)) {
                assertEquals(TEST_FILE_DATA, readFromInputStream(is));
            }
        }
    }

    public void test_newOutputStream_openOption_CREATE_NEW() throws IOException {
        // When file exists.
        try (OutputStream os = provider.newOutputStream(DATA_FILE_PATH, CREATE_NEW)) {
            fail();
        } catch (FileAlreadyExistsException expected) {
        }

        // When file doesn't exist.
        try (OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, CREATE_NEW)) {
            os.write(TEST_FILE_DATA.getBytes());
            os.close();

            try (InputStream is = provider.newInputStream(NON_EXISTENT_FILE_PATH)) {
                assertEquals(TEST_FILE_DATA, readFromInputStream(is));
            }
        }
    }

    public void test_newOutputStream_openOption_CREATE_SYNC() throws IOException {
        try (OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, SYNC, CREATE)) {
            try (InputStream is = provider.newInputStream(NON_EXISTENT_FILE_PATH, SYNC)) {
                os.write(TEST_FILE_DATA.getBytes());
                assertEquals(TEST_FILE_DATA, readFromInputStream(is));
            }
        }
    }

    public void test_newOutputStream_NPE() throws IOException {
        try (OutputStream os = provider.newOutputStream(null)) {
            fail();
        } catch (NullPointerException expected) {}

        try (OutputStream os = provider.newOutputStream(NON_EXISTENT_FILE_PATH, (OpenOption[]) null)) {
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_newByteChannel() throws IOException {
        Set<OpenOption> set = new HashSet<OpenOption>();

        // When file doesn't exist
        try (SeekableByteChannel sbc = provider.newByteChannel(NON_EXISTENT_FILE_PATH, set)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // When file exists.

        // File opens in READ mode by default. The channel is non writable by default.
        try (SeekableByteChannel sbc = provider.newByteChannel(DATA_FILE_PATH, set)) {
            sbc.write(ByteBuffer.allocate(10));
            fail();
        } catch (NonWritableChannelException expected) {
        }

        // Read a file.
        try (SeekableByteChannel sbc = provider.newByteChannel(DATA_FILE_PATH, set)) {
            ByteBuffer readBuffer = ByteBuffer.allocate(10);
            int bytesReadCount = sbc.read(readBuffer);

            String readData = new String(Arrays.copyOf(readBuffer.array(), bytesReadCount),
                    "UTF-8");
            assertEquals(TEST_FILE_DATA, readData);
        }
    }

    public void test_newByteChannel_openOption_WRITE() throws IOException {
        Set<OpenOption> set = new HashSet<OpenOption>();
        set.add(WRITE);

        // When file doesn't exist
        try (SeekableByteChannel sbc = provider.newByteChannel(NON_EXISTENT_FILE_PATH, set)) {
            fail();
        } catch (NoSuchFileException expected) {
        }


        // When file exists.
        try (SeekableByteChannel sbc = provider.newByteChannel(DATA_FILE_PATH, set)) {
            sbc.read(ByteBuffer.allocate(10));
            fail();
        } catch (NonReadableChannelException expected) {
        }

        // Write in file.
        try (SeekableByteChannel sbc = provider.newByteChannel(DATA_FILE_PATH, set)) {
            sbc.write(ByteBuffer.wrap(TEST_FILE_DATA_2.getBytes()));
            sbc.close();

            try (InputStream is = provider.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }
    }

    public void test_newByteChannel_openOption_WRITE_READ() throws IOException {
        Set<OpenOption> set = new HashSet<OpenOption>();
        set.add(WRITE);
        set.add(READ);
        set.add(SYNC);

        try (SeekableByteChannel sbc = provider.newByteChannel(DATA_FILE_PATH, set)) {
            ByteBuffer readBuffer = ByteBuffer.allocate(10);
            int bytesReadCount = sbc.read(readBuffer);

            String readData = new String(Arrays.copyOf(readBuffer.array(), bytesReadCount),
                    "UTF-8");
            assertEquals(TEST_FILE_DATA, readData);

            // Pointer will move to the end of the file after read operation. The write should
            // append the data at the end of the file.
            sbc.write(ByteBuffer.wrap(TEST_FILE_DATA_2.getBytes()));
            try (InputStream is = provider.newInputStream(DATA_FILE_PATH)) {
                String expectedFileData = TEST_FILE_DATA + TEST_FILE_DATA_2;
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }
    }

    public void test_newByteChannel_NPE() throws IOException {
        Set<OpenOption> set = new HashSet<OpenOption>();
        try (SeekableByteChannel sbc = provider.newByteChannel(null, set)) {
            fail();
        } catch(NullPointerException expected) {}

        try (SeekableByteChannel sbc = provider.newByteChannel(DATA_FILE_PATH, null)) {
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_createDirectory() throws IOException {
        Path newDirectory = Paths.get(TEST_DIR, "newDir");
        assertFalse(Files.exists(newDirectory));
        assertFalse(Files.isDirectory(newDirectory));

        provider.createDirectory(newDirectory);

        assertTrue(Files.exists(newDirectory));
        assertTrue(Files.isDirectory(newDirectory));

        try {
            provider.createDirectory(newDirectory);
            fail();
        } catch (FileAlreadyExistsException expected) {
        }

        // File with unicode name.
        Path unicodeFilePath = Paths.get(TEST_DIR, "टेस्ट डायरेक्टरी");
        provider.createDirectory(unicodeFilePath);
        assertTrue(Files.exists(unicodeFilePath));
    }

    public void test_createDirectory$String$FileAttr() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        provider.createDirectory(NON_EXISTENT_FILE_PATH, attr);
        assertEquals(attr.value(), Files.getAttribute(NON_EXISTENT_FILE_PATH, attr.name()));

        // Creating a new file and passing multiple attribute of the same name.
        perm = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr1 = PosixFilePermissions.asFileAttribute(perm);
        Path dirPath2 = Paths.get(TEST_DIR, "new_file");
        provider.createDirectory(dirPath2, attr, attr1);
        // Value should be equal to the last attribute passed.
        assertEquals(attr1.value(), Files.getAttribute(dirPath2, attr.name()));
    }

    public void test_createDirectory$String$FileAttr_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            provider.createDirectory(null, attr);
            fail();
        } catch(NullPointerException expected) {}

        try {
            provider.createDirectory(NON_EXISTENT_FILE_PATH, (FileAttribute<?>[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_createDirectory_NPE() throws IOException {
        try {
            provider.createDirectory(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createSymbolicLink() throws IOException {
        provider.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                /* Path of the target of the symbolic link */ DATA_FILE_PATH.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(NON_EXISTENT_FILE_PATH));

        // When file exists at the sym link location.
        try {
            provider.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                    /* Path of the target of the symbolic link */ DATA_FILE_PATH.toAbsolutePath());
            fail();
        } catch (FileAlreadyExistsException expected) {} finally {
            Files.deleteIfExists(NON_EXISTENT_FILE_PATH);
        }

        // Sym link to itself
        provider.createSymbolicLink(/* Path of the symbolic link */ NON_EXISTENT_FILE_PATH,
                /* Path of the target of the symbolic link */ NON_EXISTENT_FILE_PATH.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(NON_EXISTENT_FILE_PATH.toAbsolutePath()));
    }

    public void test_createSymbolicLink_NPE() throws IOException {
        try {
            provider.createSymbolicLink(null, DATA_FILE_PATH.toAbsolutePath());
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.createSymbolicLink(NON_EXISTENT_FILE_PATH, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_createSymbolicLink$Path$Attr() throws IOException {
        try {
            Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
                    .asFileAttribute(perm);
            provider.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH.toAbsolutePath(), attr);
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public void test_createSymbolicLink$Path$Attr_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
                .asFileAttribute(perm);

        try {
            provider.createSymbolicLink(null, DATA_FILE_PATH.toAbsolutePath(), attr);
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.createSymbolicLink(NON_EXISTENT_FILE_PATH, null, attr);
            fail();

        } catch (NullPointerException expected) {}

        try {
            provider.createSymbolicLink(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH,
                    (FileAttribute<?>[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_delete() throws IOException {
        // Delete existing file.
        provider.delete(DATA_FILE_PATH);
        assertFalse(Files.exists(DATA_FILE_PATH));

        // Delete non existing files.
        try {
            provider.delete(NON_EXISTENT_FILE_PATH);
            fail();
        } catch (NoSuchFileException expected) {}

        // Delete a directory.
        Path dirPath = Paths.get(TEST_DIR, "dir");
        Files.createDirectory(dirPath);
        provider.delete(dirPath);
        assertFalse(Files.exists(dirPath));


        // Delete a non empty directory.
        Files.createDirectory(dirPath);
        Files.createFile(Paths.get(TEST_DIR, "dir/file"));
        try {
            provider.delete(dirPath);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    public void test_delete_NPE() throws IOException {
        try {
            provider.delete(null);
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
            provider.deleteIfExists(dirPath);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    public void test_deleteIfExist_NPE() throws IOException {
        try {
            provider.deleteIfExists(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_copy() throws IOException {
        provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));
        // The original file should also exists.
        assertEquals(TEST_FILE_DATA, readFromFile(DATA_FILE_PATH));

        // When target file exists.
        try {
            provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        // Copy to existing target file with REPLACE_EXISTING copy option.
        writeToFile(DATA_FILE_PATH, TEST_FILE_DATA_2);
        provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, REPLACE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(NON_EXISTENT_FILE_PATH));


        // Copy to the same file. Should not fail.
        reset();
        provider.copy(DATA_FILE_PATH, DATA_FILE_PATH);
        assertEquals(TEST_FILE_DATA, readFromFile(DATA_FILE_PATH));

        // With target is a symbolic link file.
        try {
            reset();
            Path symlink = Paths.get(TEST_DIR, "symlink");
            Path newFile = Paths.get(TEST_DIR, "newDir");
            Files.createFile(newFile);
            assertTrue(Files.exists(newFile));
            Files.createSymbolicLink(symlink, DATA_FILE_PATH);
            provider.copy(DATA_FILE_PATH, symlink);
            fail();
        } catch (FileAlreadyExistsException expected) {}

        reset();
        try {
            provider.copy(NON_EXISTENT_FILE_PATH, DATA_FILE_PATH, REPLACE_EXISTING);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    public void test_copy_NPE() throws IOException {
        try {
            provider.copy((Path)null, NON_EXISTENT_FILE_PATH);
            fail();
        } catch(NullPointerException expected) {}

        try {
            provider.copy(DATA_FILE_PATH, (Path)null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, (CopyOption[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_copy_CopyOption() throws IOException {
        // COPY_ATTRIBUTES
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        Files.setAttribute(DATA_FILE_PATH, "basic:lastModifiedTime", fileTime);
        provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, COPY_ATTRIBUTES);
        assertEquals(fileTime.to(TimeUnit.SECONDS),
                ((FileTime)Files.getAttribute(NON_EXISTENT_FILE_PATH,
                        "basic:lastModifiedTime")).to(TimeUnit.SECONDS));
        assertEquals(TEST_FILE_DATA, readFromFile(NON_EXISTENT_FILE_PATH));

        // ATOMIC_MOVE
        Files.deleteIfExists(NON_EXISTENT_FILE_PATH);
        try {
            provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, ATOMIC_MOVE);
            fail();
        } catch (UnsupportedOperationException expected) {}

        Files.deleteIfExists(NON_EXISTENT_FILE_PATH);
        try {
            provider.copy(DATA_FILE_PATH, NON_EXISTENT_FILE_PATH, NonStandardOption.OPTION1);
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
        provider.copy(DATA_FILE_PATH, Paths.get(TEST_DIR, "dir1/" + DATA_FILE));
        provider.copy(dirPath, dirPath2);

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
            provider.copy(dirPath, dirPath4, REPLACE_EXISTING);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
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
        DirectoryStream<Path> directoryStream = provider.newDirectoryStream(path_root,
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
        try (DirectoryStream<Path> directoryStream = provider.newDirectoryStream(path_dir1,
                fileFilter)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // File instead of directory.
        Path path_file1 = Paths.get(TEST_DIR, "newFile1");
        Files.createFile(path_file1);
        try (DirectoryStream<Path> directoryStream = provider.newDirectoryStream(path_file1,
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
        try (DirectoryStream<Path> directoryStream = provider.newDirectoryStream(null,
                fileFilter)) {
            fail();
        } catch (NullPointerException expected) {
        }

        try (DirectoryStream<Path> directoryStream = provider.newDirectoryStream(path_dir1,
                (DirectoryStream.Filter<? super Path>) null)) {
            fail();
        } catch (NullPointerException expected) {
        }
    }

    // -- Utilities --
    private static class DirectoryStreamFilter implements DirectoryStream.Filter {

        @Override
        public boolean accept(Object entry) throws IOException {
            return true;
        }
    }
}
