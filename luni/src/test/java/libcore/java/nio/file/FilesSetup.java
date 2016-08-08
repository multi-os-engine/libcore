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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class FilesSetup {

    final String TEST_DIR = "testDir";

    final String DATA_FILE = "dataFile";

    final String NON_EXISTENT_FILE = "nonExistentFile";

    final String TEST_FILE_DATA = "hello";

    final String TEST_FILE_DATA_2 = "test";

    final Path DATA_FILE_PATH = Paths.get(TEST_DIR, DATA_FILE);

    final Path TEST_PATH = Paths.get(TEST_DIR, NON_EXISTENT_FILE);

    final Path TEST_DIR_PATH = Paths.get(TEST_DIR);

    public void setUp() throws Exception {
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

    public void tearDown() throws Exception {
        clearAll();
    }

    void clearAll() throws IOException {
        Path root = Paths.get(TEST_DIR);
        delete(root);
    }

    void reset() throws IOException {
        clearAll();
        initializeFiles();
    }

    private void delete(Path path) throws IOException {
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

    void writeToFile(Path file, String data, OpenOption... option) throws IOException {
        OutputStream os = Files.newOutputStream(file, option);
        os.write(data.getBytes());
        os.close();
    }

    String readFromFile(Path file) throws IOException {
        InputStream is = Files.newInputStream(file);
        return readFromInputStream(is);
    }

    String readFromInputStream(InputStream is) throws IOException {
        byte[] input = new byte[10000];
        is.read(input);
        return new String(input, "UTF-8").trim();
    }

    Process execCmdAndWaitForTermination(String... cmdList)
            throws InterruptedException, IOException {
        Process process = Runtime.getRuntime().exec(cmdList);
        // Wait for the process to terminate.
        process.waitFor();
        return process;
    }

    /**
     * Non Standard CopyOptions.
     */
    enum NonStandardOption implements CopyOption, OpenOption {
        OPTION1,
    }

    static Object getFileKey(Path file) throws IOException {
        return Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)
                .fileKey();
    }

}
