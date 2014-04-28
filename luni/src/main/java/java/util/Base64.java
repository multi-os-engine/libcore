package java.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.NioUtils;
import java.nio.charset.StandardCharsets;
import libcore.util.Base64DataException;
import libcore.util.Base64InputStream;
import libcore.util.Base64OutputStream;
import libcore.util.EmptyArray;

/**
 * This class provides static factory methods for {@link java.util.Base64.Encoder} and
 * {@link java.util.Base64.Decoder} instances.
 *
 * <p>Base64 encoding is specified by <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a>.
 *
 * <p>This class provides the following types of encoders / decoders:
 * <table>
 *   <tr>
 *     <th>Type</th>
 *     <th>Alphabet</th>
 *     <th>Encoder rules</th>
 *     <th>Decoder rules</th>
 *   </tr>
 *   <tr>
 *     <td>Basic</td>
 *     <td>Table 1 in RFC 4648</td>
 *     <td>No line separators added.</td>
 *     <td>Any bytes outside of the alphabet are rejected. Padding bytes must be completely absent
 *     or entirely present.</td>
 *   </tr>
 *   <tr>
 *     <td>URL</td>
 *     <td>Table 2 in RFC 4648</td>
 *     <td>No line separators added.</td>
 *     <td>Any bytes outside of the alphabet are rejected. Padding bytes must be completely absent
 *     or entirely present.</td>
 *   </tr>
 *   <tr>
 *     <td>Mime</td>
 *     <td>Table 1 in RFC 4648</td>
 *     <td>Line separators of "\r\n" every 76 bytes. No trailing line separator.</td>
 *     <td>Any bytes outside of the alphabet are ignored. Padding bytes must be completely absent or
 *     entirely present.</td>
 *   </tr>
 * </table>
 *
 * @since 1.8
 * @hide Hidden until it is to be made part of the public API
 */
public class Base64 {

  /** The encoder returned by {@link #getUrlEncoder()} */
  private static final Encoder URL_ENCODER =
      new Encoder(
          libcore.util.Base64.Encoder.FLAG_URL_SAFE /* RFC 4648 table 2 alphabet */,
          0 /* no line breaks */,
          EmptyArray.BYTE /* lineBreak */);

  /** The decoder returned by {@link #getUrlDecoder()} */
  private static final Decoder URL_DECODER = new Decoder(
      libcore.util.Base64.Decoder.FLAG_URL_SAFE /* RFC 4648 table 2 alphabet */
          | libcore.util.Base64.Decoder.FLAG_REJECT_NON_ALPHABET_BYTES);

  /** The default line length, in bytes, for the mime encoder. Specified by RFC 4648 */
  private static final int MIME_LINE_LENGTH = 76;

  /** The default line separator for the mime encoder. Specified  by RFC 4648 */
  private static final byte[] MIME_LINE_SEPARATOR = new byte[] { '\r', '\n' };

  /** The flags to use for libcore.util.Base64.Encoder instances used by the mime encoder. */
  private static final int MIME_ENCODER_FLAGS = libcore.util.Base64.Encoder.FLAG_DEFAULT;

  /** The encoder returned by {@link #getMimeEncoder()} */
  private static final Encoder STANDARD_MIME_ENCODER =
      new Encoder(MIME_ENCODER_FLAGS, MIME_LINE_LENGTH, MIME_LINE_SEPARATOR);

  /** The decoder returned by {@link #getMimeDecoder()} */
  private static final Decoder MIME_DECODER = new Decoder(libcore.util.Base64.Decoder.FLAG_DEFAULT);

  /** The encoder returned by {@link #getEncoder()} */
  private static final Encoder BASIC_ENCODER =
      new Encoder(libcore.util.Base64.Encoder.FLAG_DEFAULT,
          0 /* no line breaks */,
          EmptyArray.BYTE /* lineBreak */);

  /** The decoder returned by {@link #getDecoder()} */
  private static final Decoder BASIC_DECODER =
      new Decoder(libcore.util.Base64.Decoder.FLAG_REJECT_NON_ALPHABET_BYTES);

  private Base64() {
  }

  /**
   * A decoder of byte data encoded using the Base64 scheme specified by
   * <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a>.
   *
   * <p>Instances are thread-safe.
   *
   * <p>See {@link Base64} for more information.
   */
  public static class Decoder {

    /** Flags to pass when creating new {@link libcore.util.Base64.Decoder} instances. */
    private final int flags;

    private Decoder(int flags) {
      this.flags = flags;
    }

    /**
     * Decodes the {@code src} bytes returning them as a new array of the exact length of the
     * decoded bytes.
     *
     * <p>If the data cannot be decoded according to the rules of the decoder an
     * {@link java.lang.IllegalArgumentException} is thrown. See {@link Base64} for the decoder
     * rules.
     */
    public byte[] decode(byte[] src) {
      if (src.length == 0) {
        return EmptyArray.BYTE;
      }

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

        // Copy the decoded bytes into a new array.
        byte[] temp = new byte[bytesDecoded];
        System.arraycopy(output, 0, temp, 0, bytesDecoded);
        return temp;
      } catch (Base64DataException e) {
        throw new IllegalArgumentException(e);
      }
    }

    /**
     * Decodes the {@code src} Base64-encoded string, returning the bytes.
     *
     * <p>Equivalent to {@code decode(src.getBytes(StandardCharsets.ISO_8859_1))}.
     */
    public byte[] decode(String src) {
      return decode(src.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Decodes the {@code src} bytes writing them into the {@code dst} array, returning the number
     * of bytes decoded.
     *
     * <p>If {@code dst} is not big enough to contain the decoded bytes an
     * {@link IllegalArgumentException} is thrown and no bytes are written.
     *
     * <p>If the data cannot be decoded according to the rules of the decoder an
     * {@link java.lang.IllegalArgumentException} is thrown. See {@link Base64} for the decoder
     * rules. In this case some bytes may be written to {@code dst}.
     */
    public int decode(byte[] src, byte[] dst) {
      if (src.length == 0) {
        return 0;
      }

      byte[] temp = decode(src);
      if (temp.length > dst.length) {
        throw new IllegalArgumentException("dst is not big enough");
      }

      System.arraycopy(temp, 0, dst, 0, temp.length);
      return temp.length;
    }

    /**
     * Decodes the remaining Base64-encoded bytes from {@code buffer}. Returning a new
     * {@link ByteBuffer} containing the decoded bytes.
     *
     * <p>If the decode is successful the source buffer's position is updated to its limit,
     * the returned {@link ByteBuffer} has a position of 0 and a limit set to the number of decoded
     * bytes.
     *
     * <p>If the decode is unsuccessful an {@link IllegalArgumentException} is thrown and the
     * buffer's position is not modified.
     */
    public ByteBuffer decode(ByteBuffer buffer) {
      if (buffer.remaining() == 0) {
        return ByteBuffer.wrap(EmptyArray.BYTE);
      }

      byte[] input;
      int inputOffset;
      int inputLength;
      if (buffer.isDirect()) {
        input = new byte[buffer.remaining()];
        inputOffset = 0;
        inputLength = input.length;
        buffer.get(input, buffer.position(), buffer.remaining());
      } else {
        input = NioUtils.unsafeArray(buffer);
        inputOffset = NioUtils.unsafeArrayOffset(buffer) + buffer.position();
        inputLength = buffer.remaining();
      }

      libcore.util.Base64.Decoder decoder = createLibcoreDecoder();
      byte[] encodedBytes = new byte[decoder.maxOutputSize(buffer.remaining())];
      try {
        int encodedByteCount =
            decoder.process(input, inputOffset, inputLength, encodedBytes, true);
        buffer.position(buffer.limit());

        ByteBuffer outBuffer = ByteBuffer.allocate(encodedByteCount);
        outBuffer.put(encodedBytes, 0, encodedByteCount);
        outBuffer.position(0);

        return outBuffer;
      } catch (Base64DataException e) {
        throw new IllegalArgumentException(e);
      }
    }

    /**
     * Wraps the supplied {@link InputStream} in a Base64 decoder. Any bytes read from the
     * returned stream are the result of Base64 decoding the wrapped stream according to the
     * decoder's rules.
     *
     * <p>The stream will throw an {@link java.io.IOException} if the underlying {@link InputStream}
     * throws one or if the bytes read cannot be decoded according to the decoder's rules.
     */
    public InputStream wrap(InputStream is) {
      libcore.util.Base64.Decoder decoder = createLibcoreDecoder();
      return new Base64InputStream(is, decoder);
    }

    /**
     * Returns a new {@link libcore.util.Base64.Decoder}. Note, unlike {@link Decoder},
     * {@link libcore.util.Base64.Decoder} is not thread safe so each {@link Decoder} method
     * construct a new {@link libcore.util.Base64.Decoder}.
     */
    private libcore.util.Base64.Decoder createLibcoreDecoder() {
      return new libcore.util.Base64.Decoder(flags);
    }
  }

  /**
   * An encoder of byte data encoded using the Base64 scheme specified by
   * <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a>.
   *
   * <p>Instances are thread-safe.
   *
   * <p>See {@link Base64} for more information.
   */
  public static class Encoder {

    /** Flags to pass when creating new {@link libcore.util.Base64.Encoder} instances. */
    private final int flags;

    /** lineLength in bytes. 0 means no line breaks. */
    private final int lineLength;

    /** The lineSeparator to use when encoding. Never null, may be empty when lineLength == 0. */
    private final byte[] lineSeparator;

    private Encoder(int flags, int lineLength, byte[] lineSeparator) {
      this.flags = flags;
      if (lineLength < 0) {
        lineLength = 0;
      }

      // Validate lineSeparator in all cases, even though it may not be used.
      if (lineSeparator == null) {
        throw new NullPointerException("lineSeparator must not be null");
      }
      if (lineSeparator.length == 0) {
        lineLength = 0;
      } else if (lineSeparator.length > 0) {
        boolean webSafe = (flags & libcore.util.Base64.Encoder.FLAG_URL_SAFE) > 0;
        if (!libcore.util.Base64.Decoder.isValidLineSeparator(webSafe, lineSeparator)) {
          throw new IllegalArgumentException("lineSeparator " + Arrays.toString(lineSeparator) +
              " is not a valid Base64 line separator");
        }
      }

      this.lineLength = lineLength - (lineLength % 4);
      this.lineSeparator = lineSeparator;
    }

    /**
     * Encodes the {@code src} bytes returning them as a new array of the exact length of the
     * encoded bytes.
     */
    public byte[] encode(byte[] src) {
      if (src.length == 0) {
        return EmptyArray.BYTE;
      }

      libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
      int outputLength = encoder.calculateEncodedLength(src.length);
      byte[] output = new byte[outputLength];
      encoder.process(src, 0, src.length, output, true);
      return output;
    }

    /**
     * Encodes the {@code src} bytes writing them into the {@code dst} array, returning the number
     * of bytes encoded.
     *
     * <p>If {@code dst} is not big enough to contain the encoded bytes an
     * {@link IllegalArgumentException} is thrown and no bytes are written.
     */
    public int encode(byte[] src, byte[] dst) {
      if (src.length == 0) {
        return 0;
      }

      libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
      int outputLength = encoder.calculateEncodedLength(src.length);
      if (dst.length < outputLength) {
        throw new IllegalArgumentException("dst is not big enough");
      }
      return encoder.process(src, 0, src.length, dst, true);
    }

    /**
     * Encodes the {@code src} bytes, returning the encoded bytes as a {@link String}.
     *
     * <p>Equivalent to {@code new String(encode(src), StandardCharsets.ISO_8859_1)}.
     */
    public String encodeToString(byte[] src) {
      return new String(encode(src), StandardCharsets.ISO_8859_1);
    }

    /**
     * Encodes the remaining Base64-encoded bytes from {@code buffer}. Returning a new
     * {@link ByteBuffer} containing the encoded bytes.
     *
     * <p>On return the source buffer's position is updated to its limit, the returned
     * {@link ByteBuffer} has a position of 0 and a limit set to the number of encoded
     * bytes.
     */
    public ByteBuffer encode(ByteBuffer buffer) {
      if (buffer.remaining() == 0) {
        return ByteBuffer.wrap(EmptyArray.BYTE);
      }

      byte[] input;
      int inputOffset;
      int inputLength;
      if (buffer.isDirect()) {
        input = new byte[buffer.remaining()];
        inputOffset = 0;
        inputLength = input.length;
        buffer.get(input, buffer.position(), buffer.remaining());
      } else {
        input = NioUtils.unsafeArray(buffer);
        inputOffset = NioUtils.unsafeArrayOffset(buffer) + buffer.position();
        inputLength = buffer.remaining();
      }

      libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
      byte[] output = new byte[encoder.calculateEncodedLength(inputLength)];
      encoder.process(input, inputOffset, inputLength, output, true);
      buffer.position(buffer.limit());
      return ByteBuffer.wrap(output);
    }

    /**
     * Wraps the supplied {@link OutputStream} in a Base64 encoder. Any bytes written to the
     * returned stream are Base64 encoded according to the encoder's rules.
     *
     * <p>The stream will throw an {@link java.io.IOException} if the underlying
     * {@link OutputStream} throws one.
     */
    public OutputStream wrap(OutputStream os) {
      libcore.util.Base64.Encoder encoder = createLibcoreEncoder();
      return new Base64OutputStream(os, encoder, true /* mustCloseOut */);
    }

    /**
     * Returns an {@link Encoder} with the same rules as this one except that no-padding will be
     * added to the end of the encoded bytes. If the encoder already does not add padding then
     * {@code this} is returned.
     */
    public Encoder withoutPadding() {
      if ((flags & libcore.util.Base64.Encoder.FLAG_NO_PADDING) > 0) {
        return this;
      }
      int newFlags = flags | libcore.util.Base64.Encoder.FLAG_NO_PADDING;
      return new Encoder(newFlags, lineLength, lineSeparator);
    }

    /**
     * Returns a new {@link libcore.util.Base64.Encoder}. Note, unlike {@link Encoder},
     * {@link libcore.util.Base64.Encoder} is not thread safe so each {@link Encoder} method
     * construct a new {@link libcore.util.Base64.Encoder}.
     */
    private libcore.util.Base64.Encoder createLibcoreEncoder() {
      libcore.util.Base64.Encoder encoder;
      if (lineLength <= 0) {
        encoder = new libcore.util.Base64.Encoder(flags, 0, null);
      } else {
        encoder = new libcore.util.Base64.Encoder(flags, lineLength, lineSeparator);
      }
      return encoder;
    }
  }

  /**
   * Returns a "Basic" decoder. See {@link Base64}.
   */
  public static Base64.Decoder getDecoder() {
    return BASIC_DECODER;
  }

  /**
   * Returns a "Basic" encoder. See {@link Base64}.
   */
  public static Base64.Encoder getEncoder() {
    return BASIC_ENCODER;
  }

  /**
   * Returns a "Mime" decoder. See {@link Base64}.
   */
  public static Base64.Decoder getMimeDecoder() {
    return MIME_DECODER;
  }

  /**
   * Returns a "Mime" encoder. See {@link Base64}.
   */
  public static Base64.Encoder getMimeEncoder() {
    return STANDARD_MIME_ENCODER;
  }

  /**
   * Returns a "Mime" encoder with the specified line-wrapping behavior. See {@link Base64} for
   * general "Mime" encoder behavior.
   *
   * @throws IllegalArgumentException if the line separator contains alphabet characters
   */
  public static Base64.Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
    return new Encoder(MIME_ENCODER_FLAGS, lineLength, lineSeparator);
  }

  /**
   * Returns a "URL" decoder. See {@link Base64}.
   */
  public static Base64.Decoder getUrlDecoder() {
    return URL_DECODER;
  }

  /**
   * Returns a "URL" encoder. See {@link Base64}.
   */
  public static Base64.Encoder getUrlEncoder() {
    return URL_ENCODER;
  }

}
