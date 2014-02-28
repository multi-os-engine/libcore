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

package libcore.java.util.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import libcore.java.util.AbstractResourceLeakageDetectorTestCase;
import tests.support.resource.Support_Resources;

public final class ZipFileTest extends AbstractResourceLeakageDetectorTestCase {

    /**
     * Exercise Inflater's ability to refill the zlib's input buffer. As of this
     * writing, this buffer's max size is 64KiB compressed bytes. We'll write a
     * full megabyte of uncompressed data, which should be sufficient to exhaust
     * the buffer. http://b/issue?id=2734751
     */
    public void testInflatingFilesRequiringZipRefill() throws IOException {
        int originalSize = 1024 * 1024;
        byte[] readBuffer = new byte[8192];
        ZipFile zipFile = new ZipFile(createZipFile(1, originalSize));
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry zipEntry = e.nextElement();
            assertTrue("This test needs >64 KiB of compressed data to exercise Inflater",
                    zipEntry.getCompressedSize() > (64 * 1024));
            InputStream is = zipFile.getInputStream(zipEntry);
            drainStream(readBuffer, is);
            is.close();
        }
        zipFile.close();
    }

    private static void replaceBytes(byte[] buffer, byte[] original, byte[] replacement) {
        // Gotcha here: original and replacement must be the same length
        assertEquals(original.length, replacement.length);
        boolean found;
        for(int i=0; i < buffer.length - original.length; i++) {
            found = false;
            if (buffer[i] == original[0]) {
                found = true;
                for (int j=0; j < original.length; j++) {
                    if (buffer[i+j] != original[j]) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                System.arraycopy(replacement, 0, buffer, i, original.length);
            }
        }
    }

    private static void writeBytes(File f, byte[] bytes) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        out.write(bytes);
        out.close();
    }

    /**
     * Make sure we don't fail silently for duplicate entries.
     * b/8219321
     *
     * The reference implementation does not detect duplicate entries, it simply returns the second
     * entry and so would have the same security issue as this had.
     */
    public void testDuplicateEntries() throws Exception {
        String name1 = "test_file_name1";
        String name2 = "test_file_name2";

        // Create a valid zip file with no duplicates.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(baos);

        // The first entry is the bad one that is actually loaded.
        out.putNextEntry(new ZipEntry(name2));
        byte[] bytes;
        bytes = "bad".getBytes(Charset.forName("UTF-8"));
        int badSize = bytes.length;
        out.write(bytes);
        out.closeEntry();

        // The second entry is the good one that is verified.
        out.putNextEntry(new ZipEntry(name1));
        bytes = "good".getBytes(Charset.forName("UTF-8"));
        int goodSize = bytes.length;
        out.write(bytes);
        out.closeEntry();
        out.close();

        // Rewrite one of the filenames.
        byte[] buffer = baos.toByteArray();
        replaceBytes(buffer, name2.getBytes(), name1.getBytes());

        // Write the result to a file.
        File badZip = createTemporaryZipFile();
        writeBytes(badZip, buffer);

        // Check that we refuse to load the modified file.
        try {
            ZipFile bad = new ZipFile(badZip);

            // If duplicates are detected at this point then this code will never run but just in
            // case it does then try and see how it does behave.
            ZipEntry entry = bad.getEntry(name1);
            if (entry == null) {
                System.err.println("getEntry returns no entry");
            } else {
                long entrySize = entry.getSize();
                if (entrySize == badSize) {
                    System.err.println("getEntry returns the first entry it finds");
                } else if (entrySize == goodSize) {
                    System.err.println("getEntry returns the second entry it finds");
                }
                System.err.println("Size is: " + entrySize);
            }

            try {
                bad.close();
            } catch (Exception e) {
                // Ignore.
            }
            fail();
        } catch (ZipException expected) {
        }
    }

    /**
     * Make sure the size used for stored zip entries is the uncompressed size.
     * b/10227498
     */
    public void testStoredEntrySize() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(baos);

        // Set up a single stored entry.
        String name = "test_file";
        int expectedLength = 5;
        ZipEntry outEntry = new ZipEntry(name);
        byte[] buffer = new byte[expectedLength];
        outEntry.setMethod(ZipEntry.STORED);
        CRC32 crc = new CRC32();
        crc.update(buffer);
        outEntry.setCrc(crc.getValue());
        outEntry.setSize(buffer.length);

        out.putNextEntry(outEntry);
        out.write(buffer);
        out.closeEntry();
        out.close();

        // Write the result to a file.
        byte[] outBuffer = baos.toByteArray();
        File zipFile = createTemporaryZipFile();
        writeBytes(zipFile, outBuffer);

        ZipFile zip = new ZipFile(zipFile);
        try {
            // Set up the zip entry to have different compressed/uncompressed sizes.
            ZipEntry ze = zip.getEntry(name);
            ze.setCompressedSize(expectedLength - 1);
            // Read the contents of the stream and verify uncompressed size was used.
            InputStream stream = zip.getInputStream(ze);
            int count = 0;
            int read;
            while ((read = stream.read(buffer)) != -1) {
                count += read;
            }

            assertEquals(expectedLength, count);
        } finally {
            try {
                zip.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    public void testInflatingStreamsRequiringZipRefill() throws IOException {
        int originalSize = 1024 * 1024;
        byte[] readBuffer = new byte[8192];
        ZipInputStream in = new ZipInputStream(new FileInputStream(createZipFile(1, originalSize)));
        while (in.getNextEntry() != null) {
            drainStream(readBuffer, in);
        }
        in.close();
    }

    public void testZipFileWithLotsOfEntries() throws IOException {
        int expectedEntryCount = 64*1024 - 1;
        File f = createZipFile(expectedEntryCount, 0);
        ZipFile zipFile = new ZipFile(f);
        int entryCount = countEntries(zipFile);
        assertEquals(expectedEntryCount, entryCount);
        zipFile.close();
    }

    private void drainStream(byte[] readBuffer, InputStream in) throws IOException {
        int bytesRead;
        do {
            bytesRead = in.read(readBuffer, 0, readBuffer.length);
        } while (bytesRead != -1);
    }

    private int countEntries(ZipFile zipFile) {
        int entryCount = 0;
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();
                e.nextElement()) {
            ++entryCount;
        }
        return entryCount;
    }

    // http://code.google.com/p/android/issues/detail?id=36187
    public void testZipFileLargerThan2GiB() throws IOException {
        if (false) { // TODO: this test requires too much time and too much disk space!
            File f = createZipFile(1024, 3*1024*1024);
            ZipFile zipFile = new ZipFile(f);
            int entryCount = countEntries(zipFile);
            assertEquals(1024, entryCount);
            zipFile.close();
        }
    }

    /**
     * This test fails on reference implementation, i.e. the reference implementation appears to
     * already support Zip64, or at least more than 65535 entries.
     */
    public void testZip64Support() throws IOException {
        try {
            createZipFile(64*1024, 0);
            fail(); // Make this test more like testHugeZipFile when we have Zip64 support.
        } catch (ZipException expected) {
        }
    }

    /**
     * Compresses the given number of files, each of the given size, into a .zip archive.
     */
    private File createZipFile(int entryCount, int entrySize) throws IOException {
        File result = createTemporaryZipFile();

        byte[] writeBuffer = new byte[8192];
        Random random = new Random();

        ZipOutputStream out =
                new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(result)));
        try {
            for (int entry = 0; entry < entryCount; ++entry) {
                ZipEntry ze = new ZipEntry(Integer.toHexString(entry));
                out.putNextEntry(ze);

                for (int i = 0; i < entrySize; i += writeBuffer.length) {
                    random.nextBytes(writeBuffer);
                    int byteCount = Math.min(writeBuffer.length, entrySize - i);
                    out.write(writeBuffer, 0, byteCount);
                }

                out.closeEntry();
            }

            return result;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    private File createTemporaryZipFile() throws IOException {
        File result = File.createTempFile("ZipFileTest", "zip");
        result.deleteOnExit();
        return result;
    }

    private ZipOutputStream createZipOutputStream(File f) throws IOException {
        return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
    }

    public void testSTORED() throws IOException {
        ZipOutputStream out = createZipOutputStream(createTemporaryZipFile());
        CRC32 crc = new CRC32();

        // Missing CRC, size, and compressed size => failure.
        try {
            ZipEntry ze = new ZipEntry("a");
            ze.setMethod(ZipEntry.STORED);
            out.putNextEntry(ze);
            fail();
        } catch (ZipException expected) {
        }

        // Missing CRC and compressed size => failure.
        try {
            ZipEntry ze = new ZipEntry("a");
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(0);
            out.putNextEntry(ze);
            fail();
        } catch (ZipException expected) {
        }

        // Missing CRC and size => failure.
        try {
            ZipEntry ze = new ZipEntry("a");
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(0);
            ze.setCompressedSize(0);
            out.putNextEntry(ze);
            fail();
        } catch (ZipException expected) {
        }

        // Missing size and compressed size => failure.
        try {
            ZipEntry ze = new ZipEntry("a");
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(crc.getValue());
            out.putNextEntry(ze);
            fail();
        } catch (ZipException expected) {
        }

        // Missing size is copied from compressed size.
        {
            ZipEntry ze = new ZipEntry("okay1");
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(crc.getValue());

            assertEquals(-1, ze.getSize());
            assertEquals(-1, ze.getCompressedSize());

            ze.setCompressedSize(0);

            assertEquals(-1, ze.getSize());
            assertEquals(0, ze.getCompressedSize());

            out.putNextEntry(ze);

            assertEquals(0, ze.getSize());
            assertEquals(0, ze.getCompressedSize());
        }

        // Missing compressed size is copied from size.
        {
            ZipEntry ze = new ZipEntry("okay2");
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(crc.getValue());

            assertEquals(-1, ze.getSize());
            assertEquals(-1, ze.getCompressedSize());

            ze.setSize(0);

            assertEquals(0, ze.getSize());
            assertEquals(-1, ze.getCompressedSize());

            out.putNextEntry(ze);

            assertEquals(0, ze.getSize());
            assertEquals(0, ze.getCompressedSize());
        }

        // Mismatched size and compressed size => failure.
        try {
            ZipEntry ze = new ZipEntry("a");
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(crc.getValue());
            ze.setCompressedSize(1);
            ze.setSize(0);
            out.putNextEntry(ze);
            fail();
        } catch (ZipException expected) {
        }

        // Everything present => success.
        ZipEntry ze = new ZipEntry("okay");
        ze.setMethod(ZipEntry.STORED);
        ze.setCrc(crc.getValue());
        ze.setSize(0);
        ze.setCompressedSize(0);
        out.putNextEntry(ze);

        out.close();
    }

    private String makeString(int count, String ch) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            sb.append(ch);
        }
        return sb.toString();
    }

    public void testComments() throws Exception {
        String expectedFileComment = "1 \u0666 2";
        String expectedEntryComment = "a \u0666 b";

        File file = createTemporaryZipFile();
        ZipOutputStream out = createZipOutputStream(file);

        // Is file comment length checking done on bytes or characters? (Should be bytes.)
        out.setComment(null);
        out.setComment(makeString(0xffff, "a"));
        try {
            out.setComment(makeString(0xffff + 1, "a"));
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            out.setComment(makeString(0xffff, "\u0666"));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        ZipEntry ze = new ZipEntry("a");

        // Is entry comment length checking done on bytes or characters? (Should be bytes.)
        ze.setComment(null);
        ze.setComment(makeString(0xffff, "a"));
        try {
            ze.setComment(makeString(0xffff + 1, "a"));
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            ze.setComment(makeString(0xffff, "\u0666"));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        ze.setComment(expectedEntryComment);
        out.putNextEntry(ze);
        out.closeEntry();

        out.setComment(expectedFileComment);
        out.close();

        ZipFile zipFile = new ZipFile(file);
        assertEquals(expectedFileComment, zipFile.getComment());
        assertEquals(expectedEntryComment, zipFile.getEntry("a").getComment());
        zipFile.close();
    }

    public void test_getComment_unset() throws Exception {
        File file = createTemporaryZipFile();
        ZipOutputStream out = createZipOutputStream(file);
        ZipEntry ze = new ZipEntry("test entry");
        ze.setComment("per-entry comment");
        out.putNextEntry(ze);
        out.close();

        ZipFile zipFile = new ZipFile(file);
        try {
           assertEquals(null, zipFile.getComment());
        } finally {
            try {
                zipFile.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    // https://code.google.com/p/android/issues/detail?id=58465
    public void test_NUL_in_filename() throws Exception {
        File file = createTemporaryZipFile();

        // We allow creation of a ZipEntry whose name contains a NUL byte,
        // mainly because it's not likely to happen by accident and it's useful for testing.
        ZipOutputStream out = createZipOutputStream(file);
        out.putNextEntry(new ZipEntry("hello"));
        out.putNextEntry(new ZipEntry("hello\u0000"));
        out.close();

        // But you can't open a ZIP file containing such an entry, because we reject it
        // when we find it in the central directory.
        try {
            ZipFile zipFile = new ZipFile(file);
            try {
                zipFile.close();
            } catch (Exception e) {
                // Ignore.
            }
            fail();
        } catch (ZipException expected) {
        }
    }

    public void testCrc() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(4);

        // setCrc takes a long, not an int, so -1 isn't a valid CRC32 (because it's 64 bits).
        try {
            ze.setCrc(-1);
        } catch (IllegalArgumentException expected) {
        }

        // You can set the CRC32 to 0xffffffff if you're slightly more careful though...
        ze.setCrc(0xffffffffL);
        assertEquals(0xffffffffL, ze.getCrc());

        // And it actually works, even though we use -1L to mean "no CRC set"...
        ZipOutputStream out = createZipOutputStream(createTemporaryZipFile());
        out.putNextEntry(ze);
        out.write(-1);
        out.write(-1);
        out.write(-1);
        out.write(-1);
        out.closeEntry();
        out.close();
    }

    /**
     * RI does not allow reading of an empty zip using a {@link ZipFile}.
     */
    public void testConstructorFailsWhenReadingEmptyZipArchive() throws IOException {

        File resources = Support_Resources.createTempFolder();
        File emptyZip = Support_Resources.copyFile(
                resources, "java/util/zip", "EmptyArchive.zip");

        try {
            // The following should fail with an exception but if it doesn't then we need to clean
            // up the resource so we need a reference to it.
            ZipFile zipFile = new ZipFile(emptyZip);

            // Clean up the resource.
            try {
                zipFile.close();
            } catch (Exception e) {
                // Ignore
            }
            fail();
        } catch (ZipException expected) {
            // expected
        }
    }
}
