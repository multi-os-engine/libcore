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
package libcore.tzdata.update;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import libcore.io.IoUtils;

/**
 * Tests for {@link ConfigBundle}.
 */
public class ConfigBundleTest extends TestCase {

    private List<File> testFiles = new ArrayList<>();

    @Override
    public void tearDown() throws Exception {
        // Delete files / directories in reverse order.
        Collections.reverse(testFiles);
        for (File tempFile : testFiles) {
            tempFile.delete();
        }
        super.tearDown();
    }

    public void testExtractZipSafely_goodZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
            addZipEntry(zipOutputStream, "/leadingSlash");
            addZipEntry(zipOutputStream, "absolute");
            addZipEntry(zipOutputStream, "one/../two");
        }
        File dir = createTempDir();
        TestInputStream inputStream = new TestInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ConfigBundle.extractZipSafely(inputStream, dir, true /* makeWorldReadable */);
        inputStream.assertClosed();
        assertFilesExist(
                new File(dir, "leadingSlash"),
                new File(dir, "absolute"),
                new File(dir, "two"));
    }

    public void testExtractZipSafely_badZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
            addZipEntry(zipOutputStream, "../one");
        }
        File dir = createTempDir();
        TestInputStream inputStream = new TestInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            ConfigBundle.extractZipSafely(inputStream, dir, true /* makeWorldReadable */);
            fail();
        } catch (IOException expected) {
        }
        inputStream.assertClosed();
    }

    private static void addZipEntry(ZipOutputStream zipOutputStream, String name) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(name));
        zipOutputStream.write('a');
    }

    private File createTempDir() {
        final String tempPrefix = getClass().getSimpleName();
        File tempDir = IoUtils.createTemporaryDirectory(tempPrefix);
        testFiles.add(tempDir);
        return tempDir;
    }

    private static void assertFilesExist(File... files) {
        for (File f : files) {
            assertTrue(f + " expected to exist", f.exists());
        }
    }

    private static class TestInputStream extends FilterInputStream {

        private boolean closed;

        public TestInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

        public void assertClosed() {
            assertTrue(closed);
        }
    }
}
