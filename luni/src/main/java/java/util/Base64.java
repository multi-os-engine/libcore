package java.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import libcore.util.Base64DataException;
import libcore.util.Base64InputStream;
import libcore.util.Base64OutputStream;

/**
 * Created by nfuller on 4/23/14.
 */
/** @hide */
public class Base64 {

    private static final Decoder URL_DECODER =
            new Decoder(
                    libcore.util.Base64.Decoder.FLAG_URL_SAFE
                    | libcore.util.Base64.Decoder.FLAG_REJECT_NON_ALPHABET_BYTES);
    private static final Encoder URL_ENCODER =
            new Encoder(
                    libcore.util.Base64.Encoder.FLAG_URL_SAFE, Encoder.LINE_LENGTH_NO_WRAP,  null);

    private static final int MIME_LINE_LENGTH = 76;
    private static final byte[] MIME_LINE_SEPARATOR = new byte[] { '\r', '\n' };
    private static final int MIME_ENCODER_FLAGS = libcore.util.Base64.Encoder.FLAG_DEFAULT;
    private static final Encoder STANDARD_MIME_ENCODER =
            new Encoder(MIME_ENCODER_FLAGS, MIME_LINE_LENGTH, MIME_LINE_SEPARATOR);
    private static final Decoder MIME_DECODER = new Decoder(libcore.util.Base64.Decoder.FLAG_DEFAULT);

    private static final Encoder BASIC_ENCODER =
            new Encoder(libcore.util.Base64.Encoder.FLAG_DEFAULT, Encoder.LINE_LENGTH_NO_WRAP, null);
    private static final Decoder BASIC_DECODER =
            new Decoder(libcore.util.Base64.Decoder.FLAG_REJECT_NON_ALPHABET_BYTES);


    private Base64() {}

    public static class Decoder {
        private final int flags;

        private Decoder(int flags) {
            this.flags = flags;
        }

        public byte[] decode(byte[] src) {
            libcore.util.Base64.Decoder decoder = createLibcoreDecoder();
            // It is not possible (in advance) to be exact about the decoded size due to line
            // separators and particularly if being lenient. Instead, a maximally-sized buffer is
            // used and a copy is made if required when the exact number of decoded bytes is known.
            int maxOutputLength = decoder.maxOutputSize(src.length);
            byte[] output = new byte[maxOutputLength];
            try {
                int bytesDecoded = decoder.process(src, 0, src.length, output, true);
                if (bytesDecoded == maxOutputLength) {
                     return output;
                }
                // Copy the bytes.
                byte[] temp = new byte[bytesDecoded];
                System.arraycopy(output, 0, temp, 0, bytesDecoded);
                return temp;
            } catch (Base64DataException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public int decode(byte[] src, byte[] dst) {
            byte[] temp = decode(src);
            if (temp.length > dst.length) {
                throw new IllegalArgumentException("dst is not big enough");
            }

            System.arraycopy(temp, 0, dst, 0, src.length);
            return temp.length;
        }

        public ByteBuffer decode(ByteBuffer buffer) {
            throw new UnsupportedOperationException();
            // TODO: Change libcore.util.Base64 to decode into a buffer
        }

        public byte[] decode(String src) {
            byte[] bytes = src.getBytes(StandardCharsets.ISO_8859_1);
            return decode(bytes);
        }

        public InputStream wrap(InputStream is) {
            libcore.util.Base64.Decoder decoder = createLibcoreDecoder();
            return new Base64InputStream(is, decoder);
        }

        private libcore.util.Base64.Decoder createLibcoreDecoder() {
            return new libcore.util.Base64.Decoder(flags);
        }
    }

    // TODO(nfuller): Check thread safety guarantees are the result of multiple underlying encoders
    // or because everything is synchronized. Would matter for line-wrapping.
    public static class Encoder {

        /** @hide */
        static final int LINE_LENGTH_NO_WRAP = 0;

        /** The libcore.util.Base64 flags. */
        private final int flags;
        /** lineLength in bytes. <= 0 means no line breaks. */
        private final int lineLength;
        private final byte[] lineSeparator;

        private Encoder(int flags, int lineLength, byte[] lineSeparator) {
            this.flags = flags;
            // TODO(nfuller): Parameters have to be validated here, not at encode time.
            // TODO(nfuller): Should be linelength be rounded here to % 4 , or checked here and rejected?
            this.lineLength = lineLength;
            if ((lineLength > 0 || (flags & libcore.util.Base64.Encoder.FLAG_APPEND_LINE_BREAK) > 0)
                && (lineSeparator == null || lineSeparator.length == 0)) {
                throw new IllegalArgumentException("lineSeparator must not be null or empty");
            }
            this.lineSeparator = lineSeparator;
        }

        public byte[] encode(byte[] src) {
            libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
            int outputLength = encoder.calculateEncodedLength(src.length);
            byte[] output = new byte[outputLength];
            encoder.process(src, 0, src.length, output, true);
            return output;
        }

        public int encode(byte[] src, byte[] dst) {
            libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
            int outputLength = encoder.calculateEncodedLength(src.length);
            if (dst.length < outputLength) {
                throw new IllegalArgumentException("dst is not big enough");
            }
            return encoder.process(src, 0, src.length, dst, true);
        }

        public ByteBuffer encode(ByteBuffer buffer) {
            throw new UnsupportedOperationException();
            // TODO: Change libcore.util.Base64 to decode into a buffer?
        }

        public String encodeToString(byte[] src) {
            return new String(encode(src), StandardCharsets.ISO_8859_1);
        }

        public Encoder withoutPadding() {
            if ((flags & libcore.util.Base64.Encoder.FLAG_NO_PADDING) > 0) {
                return this;
            }
            int newFlags = flags | libcore.util.Base64.Encoder.FLAG_NO_PADDING;
            return new Encoder(newFlags, lineLength, lineSeparator);
        }

        public OutputStream wrap(OutputStream os) {
            libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
            return new Base64OutputStream(os, encoder, true /* mustCloseOut */);
        }

        private libcore.util.Base64.Encoder createLibcoreEncoder() {
            libcore.util.Base64.Encoder encoder;
            if (lineLength == LINE_LENGTH_NO_WRAP) {
                encoder = new libcore.util.Base64.Encoder(flags, 0, null);
            } else {
                encoder = new libcore.util.Base64.Encoder(flags, lineLength, lineSeparator);
            }
            return encoder;
        }
    }

    public static Base64.Decoder getDecoder() {
        return BASIC_DECODER;
    }

    public static Base64.Encoder getEncoder() {
        return BASIC_ENCODER;
    }

    public static Base64.Decoder getMimeDecoder() {
        return MIME_DECODER;
    }

    public static Base64.Encoder getMimeEncoder() {
        return STANDARD_MIME_ENCODER;
    }

    public static Base64.Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
        return new Encoder(MIME_ENCODER_FLAGS, lineLength, lineSeparator);
    }

    public static Base64.Decoder getUrlDecoder() {
        return URL_DECODER;
    }

    public static Base64.Encoder getUrlEncoder() {
        return URL_ENCODER;
    }

}
