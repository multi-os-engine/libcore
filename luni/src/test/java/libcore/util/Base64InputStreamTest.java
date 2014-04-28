package libcore.util;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import libcore.util.Base64;
import libcore.util.Base64InputStream;

/**
 * Tests for {@link Base64InputStream}.
 */
public class Base64InputStreamTest extends TestCase {

    static final String lipsum =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Quisque congue eleifend odio, eu ornare nulla facilisis eget. " +
            "Integer eget elit diam, sit amet laoreet nibh. Quisque enim " +
            "urna, pharetra vitae consequat eget, adipiscing eu ante. " +
            "Aliquam venenatis arcu nec nibh imperdiet tempor. In id dui " +
            "eget lorem aliquam rutrum vel vitae eros. In placerat ornare " +
            "pretium. Curabitur non fringilla mi. Fusce ultricies, turpis " +
            "eu ultrices suscipit, ligula nisi consectetur eros, dapibus " +
            "aliquet dui sapien a turpis. Donec ultricies varius ligula, " +
            "ut hendrerit arcu malesuada at. Praesent sed elit pretium " +
            "eros luctus gravida. In ac dolor lorem. Cras condimentum " +
            "convallis elementum. Phasellus vel felis in nulla ultrices " +
            "venenatis. Nam non tortor non orci convallis convallis. " +
            "Nam tristique lacinia hendrerit. Pellentesque habitant morbi " +
            "tristique senectus et netus et malesuada fames ac turpis " +
            "egestas. Vivamus cursus, nibh eu imperdiet porta, magna " +
            "ipsum mollis mauris, sit amet fringilla mi nisl eu mi. " +
            "Phasellus posuere, leo at ultricies vehicula, massa risus " +
            "volutpat sapien, eu tincidunt diam ipsum eget nulla. Cras " +
            "molestie dapibus commodo. Ut vel tellus at massa gravida " +
            "semper non sed orci.";

    public void testInputStream() throws Exception {
        Base64.Encoder[] encoders = {
                new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 76, new byte[]{ '\r', '\n' }),
                new Base64.Encoder(Base64.Encoder.FLAG_NO_PADDING, 0, null),
                new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 0, null),
                new Base64.Encoder(Base64.Encoder.FLAG_NO_PADDING, 0, null),
                new Base64.Encoder(Base64.Encoder.FLAG_DEFAULT, 76, new byte[]{ '\n' }),
                new Base64.Encoder(Base64.Encoder.FLAG_URL_SAFE, 76, new byte[]{ '\n' }),
        };

        Base64.Decoder standardDecoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);
        Base64.Decoder urlSafeDecoder = new Base64.Decoder(Base64.Decoder.FLAG_URL_SAFE);
        Base64.Decoder[] decoders = {
                standardDecoder,
                standardDecoder,
                standardDecoder,
                standardDecoder,
                standardDecoder,
                urlSafeDecoder,
        };

        int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };
        Random rng = new Random(32176L);

        // Test input needs to be at least 2048 bytes to fill up the
        // read buffer of Base64InputStream.
        byte[] plain = (lipsum + lipsum + lipsum + lipsum + lipsum).getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < encoders.length; i++) {
            Base64.Encoder encoder = encoders[i];
            Base64.Decoder decoder = decoders[i];

            byte[] encoded = new byte[encoder.calculateEncodedLength(plain.length)];
            int bytesEncoded = encoder.process(plain, 0, plain.length, encoded, true);
            assertEquals(encoded.length, bytesEncoded);

            ByteArrayInputStream bais;
            Base64InputStream b64is;
            byte[] actual = new byte[plain.length * 2];
            int ap;
            int b;

            // ----- test decoding ("encoded" -> "plain") -----

            // read as much as it will give us in one chunk
            bais = new ByteArrayInputStream(encoded);
            b64is = new Base64InputStream(bais, decoder);
            ap = 0;
            while ((b = b64is.read(actual, ap, actual.length-ap)) != -1) {
                ap += b;
            }
            assertEquals(actual, ap, plain);

            // read individual bytes
            bais = new ByteArrayInputStream(encoded);
            b64is = new Base64InputStream(bais, decoder);
            ap = 0;
            while ((b = b64is.read()) != -1) {
                actual[ap++] = (byte) b;
            }
            assertEquals(actual, ap, plain);

            // mix reads of variously-sized arrays with one-byte reads
            bais = new ByteArrayInputStream(encoded);
            b64is = new Base64InputStream(bais, decoder);
            ap = 0;
            readloop: while (true) {
                int l = writeLengths[rng.nextInt(writeLengths.length)];
                if (l >= 0) {
                    b = b64is.read(actual, ap, l);
                    if (b == -1) break readloop;
                    ap += b;
                } else {
                    for (int j = 0; j < -l; ++j) {
                        if ((b = b64is.read()) == -1) break readloop;
                        actual[ap++] = (byte) b;
                    }
                }
            }
            assertEquals(actual, ap, plain);

            // ----- test encoding ("plain" -> "encoded") -----

            // read as much as it will give us in one chunk
            bais = new ByteArrayInputStream(plain);
            b64is = new Base64InputStream(bais, encoder);
            ap = 0;
            while ((b = b64is.read(actual, ap, actual.length-ap)) != -1) {
                ap += b;
            }
            assertEquals(actual, ap, encoded);

            // read individual bytes
            bais = new ByteArrayInputStream(plain);
            b64is = new Base64InputStream(bais, encoder);
            ap = 0;
            while ((b = b64is.read()) != -1) {
                actual[ap++] = (byte) b;
            }
            assertEquals(actual, ap, encoded);

            // mix reads of variously-sized arrays with one-byte reads
            bais = new ByteArrayInputStream(plain);
            b64is = new Base64InputStream(bais, encoder);
            ap = 0;
            readloop: while (true) {
                int l = writeLengths[rng.nextInt(writeLengths.length)];
                if (l >= 0) {
                    b = b64is.read(actual, ap, l);
                    if (b == -1) break readloop;
                    ap += b;
                } else {
                    for (int j = 0; j < -l; ++j) {
                        if ((b = b64is.read()) == -1) break readloop;
                        actual[ap++] = (byte) b;
                    }
                }
            }
            assertEquals(actual, ap, encoded);
        }
    }

    /** http://b/3026478 */
    public void testSingleByteReads() throws IOException {
        Base64.Decoder decoder = new Base64.Decoder(Base64.Decoder.FLAG_DEFAULT);
        InputStream in = new Base64InputStream(
                new ByteArrayInputStream("/v8=".getBytes(StandardCharsets.US_ASCII)), decoder);
        assertEquals(254, in.read());
        assertEquals(255, in.read());
    }


    /** Assert that actual equals the first len bytes of expected. */
    private static void assertEquals(byte[] expected, int len, byte[] actual) {
        assertEquals(len, actual.length);
        for (int i = 0; i < len; ++i) {
            assertEquals(expected[i], actual[i]);
        }
    }

}
