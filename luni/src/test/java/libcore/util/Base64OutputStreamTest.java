package libcore.util;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Created by nfuller on 4/30/14.
 */
public class Base64OutputStreamTest extends TestCase {

    private static final String lipsum = Base64InputStreamTest.lipsum;

    /**
     * Tests that Base64OutputStream produces exactly the same results
     * as calling Base64.encode/.decode on an in-memory array.
     */
    public void testOutputStream() throws Exception {
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

        // Test input needs to be at least 1024 bytes to test filling
        // up the write(int) buffer of Base64OutputStream.
        byte[] plain = (lipsum + lipsum).getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < encoders.length; i++) {
            Base64.Encoder encoder = encoders[i];
            Base64.Decoder decoder = decoders[i];

            byte[] encoded = new byte[encoder.calculateEncodedLength(plain.length)];
            int bytesEncoded = encoder.process(plain, 0, plain.length, encoded, true);
            assertEquals(encoded.length, bytesEncoded);

            ByteArrayOutputStream baos;
            Base64OutputStream b64os;
            byte[] actual;
            int p;

            // ----- test encoding ("plain" -> "encoded") -----

            // one large write(byte[]) of the whole input
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, encoder, true /* mustCloseOut */);
            b64os.write(plain);
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(encoded, actual);

            // many calls to write(int)
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, encoder, true /* mustCloseOut */);
            for (byte aPlain : plain) {
                b64os.write(aPlain);
            }
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(encoded, actual);

            // intermixed sequences of write(int) with
            // write(byte[],int,int) of various lengths.
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, encoder, true /* mustCloseOut */);
            p = 0;
            while (p < plain.length) {
                int l = writeLengths[rng.nextInt(writeLengths.length)];
                l = Math.min(l, plain.length-p);
                if (l >= 0) {
                    b64os.write(plain, p, l);
                    p += l;
                } else {
                    l = Math.min(-l, plain.length-p);
                    for (int j = 0; j < l; ++j) {
                        b64os.write(plain[p+j]);
                    }
                    p += l;
                }
            }
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(encoded, actual);

            // ----- test decoding ("encoded" -> "plain") -----

            // one large write(byte[]) of the whole input
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, decoder, true /* mustCloseOut */);
            b64os.write(encoded);
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(plain, actual);

            // many calls to write(int)
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, decoder, true /* mustCloseOut */);
            for (byte anEncoded : encoded) {
                b64os.write(anEncoded);
            }
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(plain, actual);

            // intermixed sequences of write(int) with
            // write(byte[],int,int) of various lengths.
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, decoder, true /* mustCloseOut */);
            p = 0;
            while (p < encoded.length) {
                int l = writeLengths[rng.nextInt(writeLengths.length)];
                l = Math.min(l, encoded.length-p);
                if (l >= 0) {
                    b64os.write(encoded, p, l);
                    p += l;
                } else {
                    l = Math.min(-l, encoded.length-p);
                    for (int j = 0; j < l; ++j) {
                        b64os.write(encoded[p+j]);
                    }
                    p += l;
                }
            }
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(plain, actual);
        }
    }

}
