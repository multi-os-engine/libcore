package libcore.java.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;
import junit.framework.TestCase;

public final class InflaterDeflaterStreamClosingBehaviourTest extends TestCase {

    public void testClosingInflaterInputStreamDoesNotEndConstructorSuppliedInflater() throws IOException {
        Inflater inflater = new Inflater();

        assertCloseDoesNotEndInflater(new InflaterInputStream(sampleInput(), inflater), inflater);
    }

    public void testClosingInflaterOutputStreamDoesNotEndConstructorSuppliedInflater() throws IOException {
        Inflater inflater = new Inflater();

        assertCloseDoesNotEndInflater(new InflaterOutputStream(sampleOutput(), inflater), inflater);
    }

    public void testClosingDeflaterInputStreamDoesNotEndConstructorSuppliedDeflater() throws IOException {
        Deflater deflater = new Deflater();

        assertCloseDoesNotEndDeflater(new DeflaterInputStream(sampleInput(), deflater), deflater);
    }

    public void testClosingDeflaterOutputStreamDoesNotEndConstructorSuppliedDeflater() throws IOException {
        Deflater deflater = new Deflater();

        assertCloseDoesNotEndDeflater(new DeflaterOutputStream(sampleOutput(), deflater), deflater);
    }

    public void testClosingInflaterInputStreamEndsInternalInflater() throws IOException {
        new InflaterInputStream(sampleInput()) {
            public void close() throws IOException {
                super.close();
                assertInflaterEnded(inf);
            }
        }.close();
    }

    public void testClosingInflaterOutputStreamEndsInternalInflater() throws IOException {
        new InflaterOutputStream(sampleOutput()) {
            public void close() throws IOException {
                super.close();
                assertInflaterEnded(inf);
            }
        }.close();
    }

    public void testClosingDeflaterInputStreamEndsInternalDeflater() throws IOException {
        new DeflaterInputStream(sampleInput()) {
            public void close() throws IOException {
                super.close();
                assertDeflaterEnded(def);
            }
        }.close();
    }

    public void testClosingDeflaterOutputStreamEndsInternalDeflater() throws IOException {
        new DeflaterOutputStream(sampleOutput()) {
            public void close() throws IOException {
                super.close();
                assertDeflaterEnded(def);
            }
        }.close();
    }


    private void assertCloseDoesNotEndInflater(Closeable stream, Inflater inflater) throws IOException {
        stream.close(); // should not end() inflater, permitting re-use

        inflater.reset(); // breaks if inflater.end() has been invoked
    }

    private void assertCloseDoesNotEndDeflater(Closeable stream, Deflater deflater) throws IOException {
        stream.close(); // should not end() deflater, permitting re-use

        deflater.reset(); // breaks if deflater.end() has been invoked
    }

    private void assertInflaterEnded(Inflater inflater) {
        try {
            inflater.reset(); // invoke an inflater method to check that it fails
            fail();
        } catch (RuntimeException expected) {
        }
    }

    private void assertDeflaterEnded(Deflater deflater) {
        try {
            deflater.reset(); // invoke a deflater method to check that it fails
            fail();
        } catch (RuntimeException expected) {
        }
    }

    private InputStream sampleInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private OutputStream sampleOutput() {
        return new ByteArrayOutputStream();
    }

}
