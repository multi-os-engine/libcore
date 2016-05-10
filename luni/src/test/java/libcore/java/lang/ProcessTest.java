package libcore.java.lang;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessTest extends TestCase {

    private static final String SHELL = "sh";

    public void testNullStreams() throws IOException {
        Process process = new ProcessBuilder()
                .command(SHELL)
                .inheritIO()
                .start();
        try {
            assertNullInputStream(process.getInputStream());
            assertNullOutputStream(process.getOutputStream());
            assertNullInputStream(process.getErrorStream());
        } finally {
            process.destroy();
        }
    }

    public void testNullStream_redirectErrorStream_boolean() throws IOException {
        Process process = new ProcessBuilder()
                .command(SHELL)
                .redirectErrorStream(true)
                .start();
        try {
            assertNullInputStream(process.getErrorStream());
        } finally {
            process.destroy();
        }
    }

    /**
     * Asserts that inputStream is a
     * <a href="ProcessBuilder.html#redirect-input">null input stream</a>.
     */
    private static void assertNullInputStream(InputStream inputStream) throws IOException {
        assertEquals(-1, inputStream.read());
        assertEquals(0, inputStream.available());
        inputStream.close(); // should do nothing
    }

    /**
     * Asserts that outputStream is a
     * <a href="ProcessBuilder.html#redirect-output">null output stream</a>.
     */
    private static void assertNullOutputStream(OutputStream outputStream) throws IOException {
        try {
            outputStream.write(42);
            fail("NullOutputStream.write(int) must throw IOException: " + outputStream);
        } catch (IOException expected) {
            // expected
        }
        outputStream.close(); // should do nothing
    }

}
