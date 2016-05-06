/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.java.lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import libcore.io.Base64;
import libcore.io.IoUtils;
import libcore.java.util.AbstractResourceLeakageDetectorTestCase;

import static tests.support.Support_Exec.execAndCheckOutput;

public class ProcessBuilderTest extends AbstractResourceLeakageDetectorTestCase {
    private static final String TAG = ProcessBuilderTest.class.getSimpleName();

    private static String shell() {
        String deviceSh = System.getenv("ANDROID_ROOT") + "/bin/sh";
        String desktopSh = "/bin/sh";
        return new File(deviceSh).exists() ? deviceSh : desktopSh;
    }

    private static void assertRedirectErrorStream(boolean doRedirect,
            String expectedOut, String expectedErr) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2");
        pb.redirectErrorStream(doRedirect);
        execAndCheckOutput(pb, expectedOut, expectedErr);
    }

    public void test_redirectErrorStream_true() throws Exception {
        assertRedirectErrorStream(true, "out\nerr\n", "");
    }

    public void test_redirectErrorStream_false() throws Exception {
        assertRedirectErrorStream(false, "out\n", "err\n");
    }

    /**
     * Tests that input, output and error redirection from/to files works.
     * This test checks that "base64 -d" on input that starts validly, but has
     * trailing garbage, produces expected output and errors.
     */
    public void testRedirectAll_file() throws IOException {
        File inFile = File.createTempFile(TAG, "in");
        byte[] rawBytes = new byte[100000];
        new Random(0xc0ffee).nextBytes(rawBytes); // arbitrary but deterministic bytes
        try (OutputStream outputStream = new FileOutputStream(inFile)) {
            writeBase64AndRawBytesTo(rawBytes, outputStream);
        }
        File outFile = File.createTempFile(TAG, "out");
        File errFile = File.createTempFile(TAG, "err");
        try {
            ProcessBuilder pb = new ProcessBuilder()
                    .command("base64", "-d")
                    .redirectInput(inFile)
                    .redirectOutput(outFile)
                    .redirectError(errFile);
            Process process = pb.start();
            int result = process.waitFor();
            assertEquals(pb.command() + " exit value", 1, result);
            byte[] out = IoUtils.readFileAsByteArray(outFile.getAbsolutePath());
            byte[] err = IoUtils.readFileAsByteArray(errFile.getAbsolutePath());

            byte[] expectedErrSuffix = "invalid input\n".getBytes(); // platform default charset
            assertPrefix("out should start with the raw bytes", rawBytes, out);
            assertSuffix("err should end with error message", expectedErrSuffix, err);
        } catch (InterruptedException e) {
            fail("Unexpected interruption: " + e.getMessage());
        } finally {
            for (File file : Arrays.asList(inFile, outFile, errFile)) {
                boolean ignoredSuccess = file.delete();
            }
        }
    }

    /**
     * Tests that input, output and error redirection from/to PIPE works.
     * This is similar to {@link #testRedirectAll_file()} however in this
     * case we need to start the process before we can write to its input.
     */
    public void testRedirectAll_pipe() throws IOException {
        ProcessBuilder pb = new ProcessBuilder()
                .command("base64", "-d")
                .redirectInput(Redirect.PIPE)
                .redirectOutput(Redirect.PIPE)
                .redirectError(Redirect.PIPE);
        Process process = pb.start();
        // start reading so that the writer doesn't block
        FutureTask<byte[]> futureOut = asyncRead(process.getInputStream());
        FutureTask<byte[]> futureErr = asyncRead(process.getErrorStream());
        byte[] rawBytes = new byte[100000];
        new Random(0xc0ffee).nextBytes(rawBytes); // arbitrary but deterministic bytes
        try (OutputStream processStdIn = process.getOutputStream()) {
            try {
                writeBase64AndRawBytesTo(rawBytes, processStdIn);
            } catch (IOException e) {
                // ignore the pipe breaking once the reading process encounters the garbage
            }
        }
        try {
            int result = process.waitFor();
            assertEquals(pb.command() + " exit value", 1, result);
            byte[] out = futureOut.get();
            byte[] err = futureErr.get();
            byte[] expectedErrSuffix = "invalid input\n".getBytes(); // platform default charset
            assertPrefix("out should start with the raw bytes", rawBytes, out);
            assertSuffix("err should end with error message", expectedErrSuffix, err);
        } catch (InterruptedException e) {
            fail("Unexpected interruption: " + e.getMessage());
        } catch (ExecutionException e) {
            fail("Unexpected ExecutionException: " + e.getCause().getMessage());
        }
    }

    /**
     * Writes Base64 encoded bytes, followed by the raw bytes, to the given {@code outputStream}.
     */
    private static void writeBase64AndRawBytesTo(byte[] bytes, OutputStream outputStream)
            throws IOException {
        String base64OfBytes = Base64.encode(bytes);
        outputStream.write(base64OfBytes.getBytes()); // platform default charset
        outputStream.write(bytes); // trailing garbage
    }

    public void testEnvironment() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo $A");
        pb.environment().put("A", "android");
        execAndCheckOutput(pb, "android\n", "");
    }

    public void testDestroyClosesEverything() throws IOException {
        Process process = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2").start();
        InputStream in = process.getInputStream();
        InputStream err = process.getErrorStream();
        OutputStream out = process.getOutputStream();
        process.destroy();

        try {
            in.read();
            fail();
        } catch (IOException expected) {
        }
        try {
            err.read();
            fail();
        } catch (IOException expected) {
        }
        try {
            /*
             * We test write+flush because the RI returns a wrapped stream, but
             * only bothers to close the underlying stream.
             */
            out.write(1);
            out.flush();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testDestroyDoesNotLeak() throws IOException {
        Process process = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2").start();
        process.destroy();
    }

    public void testEnvironmentMapForbidsNulls() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo $A");
        Map<String, String> environment = pb.environment();
        Map<String, String> before = new HashMap<String, String>(environment);
        try {
            environment.put("A", null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            environment.put(null, "android");
            fail();
        } catch (NullPointerException expected) {
        }
        assertEquals(before, environment);
    }

    private static void assertPrefix(String message, byte[] expectedPrefix, byte[] data) {
        byte[] dataPrefix = Arrays.copyOfRange(
                data, 0, Math.min(expectedPrefix.length, data.length));
        String comparison = Arrays.toString(expectedPrefix) + " vs. " + Arrays.toString(dataPrefix);
        assertTrue(message + ": " + comparison, Arrays.equals(expectedPrefix, dataPrefix));
    }

    private static void assertSuffix(String message, byte[] expectedSuffix, byte[] data) {
        byte[] dataSuffix = Arrays.copyOfRange(
                data, Math.max(0, data.length - expectedSuffix.length), data.length);
        String comparison = Arrays.toString(expectedSuffix) + " vs. " + Arrays.toString(dataSuffix);
        assertTrue(message + ": " + comparison, Arrays.equals(expectedSuffix, dataSuffix));
    }

    /**
     * Reads the entire specified {@code inputStream} asynchronously.
     */
    static FutureTask<byte[]> asyncRead(final InputStream inputStream) {
        final FutureTask<byte[]> result = new FutureTask<>(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int numRead;
            while ((numRead = inputStream.read(data)) >= 0) {
                outputStream.write(data, 0, numRead);
            }
            return outputStream.toByteArray();
        });
        new Thread("read asynchronously from " + inputStream) {
            @Override
            public void run() {
                result.run();
            }
        }.start();
        return result;
    }

}
