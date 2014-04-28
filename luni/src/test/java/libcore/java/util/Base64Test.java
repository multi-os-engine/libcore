package libcore.java.util;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

/**
 * Tests for {@link Base64}.
 */
public class Base64Test extends TestCase {

  public void testDecoder_extraChars_basic() throws Exception {
    Base64.Decoder basicDecoder = Base64.getDecoder();
    testDecoder_extraChars_strict(basicDecoder);

    // Table 1 chars
    assertBad(basicDecoder, "_aGVsbG8sIHdvcmx");
    assertBad(basicDecoder, "aGV_sbG8sIHdvcmx");
    assertBad(basicDecoder, "aGVsbG8sIHdvcmx_");
  }

  public void testDecoder_extraChars_url() throws Exception {
    Base64.Decoder urlDecoder = Base64.getUrlDecoder();
    testDecoder_extraChars_strict(urlDecoder);

    // Table 2 chars.
    assertBad(urlDecoder, "/aGVsbG8sIHdvcmx");
    assertBad(urlDecoder, "aGV/sbG8sIHdvcmx");
    assertBad(urlDecoder, "aGVsbG8sIHdvcmx/");
  }

  // Only tests decoding of strings with characters in both table 1 and table 2.
  private void testDecoder_extraChars_strict(Base64.Decoder decoder) throws Exception {
    // Characters outside alphabet before padding.
    assertBad(decoder, " aGVsbG8sIHdvcmx");
    assertBad(decoder, "aGV sbG8sIHdvcmx");
    assertBad(decoder, "aGVsbG8sIHdvcmx ");
    assertBad(decoder, "*aGVsbG8sIHdvcmx");
    assertBad(decoder, "aGV*sbG8sIHdvcmx");
    assertBad(decoder, "aGVsbG8sIHdvcmx*");
    assertBad(decoder, "\r\naGVsbG8sIHdvcmx");
    assertBad(decoder, "aGV\r\nsbG8sIHdvcmx");
    assertBad(decoder, "aGVsbG8sIHdvcmx\r\n");
    assertBad(decoder, "\naGVsbG8sIHdvcmx");
    assertBad(decoder, "aGV\nsbG8sIHdvcmx");
    assertBad(decoder, "aGVsbG8sIHdvcmx\n");

    // padding 0
    assertEquals("hello, world", decodeString(decoder, "aGVsbG8sIHdvcmxk"));
    // Extra padding
    assertBad(decoder, "aGVsbG8sIHdvcmxk=");
    assertBad(decoder, "aGVsbG8sIHdvcmxk==");
    // Characters outside alphabet intermixed with (too much) padding.
    assertBad(decoder, "aGVsbG8sIHdvcmxk =");
    assertBad(decoder, "aGVsbG8sIHdvcmxk = = ");

    // padding 1
    assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE="));
    // Missing padding
    assertEquals("hello, world?!", decodeString(decoder, "aGVsbG8sIHdvcmxkPyE"));
    // Characters outside alphabet before padding.
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE =");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE*=");
    // Trailing characters, otherwise valid.
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE= ");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=*");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=X");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XY");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=\n");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE=\r\n");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE= ");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE==");
    // Characters outside alphabet intermixed with (too much) padding.
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE ==");
    assertBad(decoder, "aGVsbG8sIHdvcmxkPyE = = ");

    // padding 2
    assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg=="));
    // Missing padding
    assertEquals("hello, world.", decodeString(decoder, "aGVsbG8sIHdvcmxkLg"));
    // Partially missing padding
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg=");
    // Characters outside alphabet before padding.
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg ==");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg*==");
    // Trailing characters, otherwise valid.
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg== ");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==*");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==X");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XY");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZ");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==XYZA");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==\n");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg==\r\n");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg== ");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg===");
    // Characters outside alphabet inside padding.
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg= =");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg=*=");
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg=\r\n=");
    // Characters inside alphabet inside padding.
    assertBad(decoder, "aGVsbG8sIHdvcmxkLg=X=");
  }

  public void testDecoder_extraChars_mime() throws Exception {
    Base64.Decoder mimeDecoder = Base64.getMimeDecoder();

    // Characters outside alphabet before padding.
    assertEquals("hello, world", decodeString(mimeDecoder, " aGVsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGV sbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxk "));
    assertEquals("hello, world", decodeString(mimeDecoder, "_aGVsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGV_sbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxk_"));
    assertEquals("hello, world", decodeString(mimeDecoder, "*aGVsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGV*sbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxk*"));
    assertEquals("hello, world", decodeString(mimeDecoder, "\r\naGVsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGV\r\nsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxk\r\n"));
    assertEquals("hello, world", decodeString(mimeDecoder, "\naGVsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGV\nsbG8sIHdvcmxk"));
    assertEquals("hello, world", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxk\n"));

    // padding 0
    assertEquals("hello, world", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxk"));
    // Extra padding
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxk=");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxk==");
    // Characters outside alphabet intermixed with (too much) padding.
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxk =");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxk = = ");

    // padding 1
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE="));
    // Missing padding
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE"));
    // Characters outside alphabet before padding.
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE ="));
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE*="));
    // Trailing characters, otherwise valid.
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE= "));
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=*"));
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=X");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=XY");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=\n"));
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=\r\n"));
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE= "));
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=="));
    // Characters outside alphabet intermixed with (too much) padding.
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE =="));
    assertEquals("hello, world?!", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkPyE = = "));

    // padding 2
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg=="));
    // Missing padding
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg"));
    // Partially missing padding
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkLg=");
    // Characters outside alphabet before padding.
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg =="));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg*=="));
    // Trailing characters, otherwise valid.
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg== "));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg==*"));
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkLg==X");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkLg==XY");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkLg==XYZ");
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkLg==XYZA");
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg==\n"));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg==\r\n"));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg== "));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg==="));
    // Characters outside alphabet inside padding. This is a difference from the RI which is not
    // as permissive.
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg= ="));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg=*="));
    assertEquals("hello, world.", decodeString(mimeDecoder, "aGVsbG8sIHdvcmxkLg=\r\n="));
    // Characters inside alphabet inside padding.
    assertBad(mimeDecoder, "aGVsbG8sIHdvcmxkLg=X=");
  }

  public void testDecoder_nonPrintableBytes_basic() throws Exception {
    testDecoder_nonPrintableBytes_table1(Base64.getDecoder());
  }

  public void testDecoder_nonPrintableBytes_mime() throws Exception {
    testDecoder_nonPrintableBytes_table1(Base64.getMimeDecoder());
  }

  private static final byte[] BYTES = { (byte) 0xff, (byte) 0xee, (byte) 0xdd,
      (byte) 0xcc, (byte) 0xbb, (byte) 0xaa,
      (byte) 0x99, (byte) 0x88, (byte) 0x77 };

  private void testDecoder_nonPrintableBytes_table1(Base64.Decoder decoder) throws Exception {
    assertEquals(BYTES, 0, decoder.decode(""));
    assertEquals(BYTES, 1, decoder.decode("/w=="));
    assertEquals(BYTES, 2, decoder.decode("/+4="));
    assertEquals(BYTES, 3, decoder.decode("/+7d"));
    assertEquals(BYTES, 4, decoder.decode("/+7dzA=="));
    assertEquals(BYTES, 5, decoder.decode("/+7dzLs="));
    assertEquals(BYTES, 6, decoder.decode("/+7dzLuq"));
    assertEquals(BYTES, 7, decoder.decode("/+7dzLuqmQ=="));
    assertEquals(BYTES, 8, decoder.decode("/+7dzLuqmYg="));
  }

  public void testDecoder_nonPrintableBytes_url() throws Exception {
    Base64.Decoder decoder = Base64.getUrlDecoder();
    assertEquals(BYTES, 0, decoder.decode(""));
    assertEquals(BYTES, 1, decoder.decode("_w=="));
    assertEquals(BYTES, 2, decoder.decode("_-4="));
    assertEquals(BYTES, 3, decoder.decode("_-7d"));
    assertEquals(BYTES, 4, decoder.decode("_-7dzA=="));
    assertEquals(BYTES, 5, decoder.decode("_-7dzLs="));
    assertEquals(BYTES, 6, decoder.decode("_-7dzLuq"));
    assertEquals(BYTES, 7, decoder.decode("_-7dzLuqmQ=="));
    assertEquals(BYTES, 8, decoder.decode("_-7dzLuqmYg="));
  }

  public void testEncoder_nonPrintableBytes() throws Exception {
    Base64.Encoder encoder = Base64.getUrlEncoder();
    assertEquals("", encoder.encodeToString(subArray(BYTES, 0, 0)));
    assertEquals("_w==", encoder.encodeToString(subArray(BYTES, 0, 1)));
    assertEquals("_-4=", encoder.encodeToString(subArray(BYTES, 0, 2)));
    assertEquals("_-7d", encoder.encodeToString(subArray(BYTES, 0, 3)));
    assertEquals("_-7dzA==", encoder.encodeToString(subArray(BYTES, 0, 4)));
    assertEquals("_-7dzLs=", encoder.encodeToString(subArray(BYTES, 0, 5)));
    assertEquals("_-7dzLuq", encoder.encodeToString(subArray(BYTES, 0, 6)));
    assertEquals("_-7dzLuqmQ==", encoder.encodeToString(subArray(BYTES, 0, 7)));
    assertEquals("_-7dzLuqmYg=", encoder.encodeToString(subArray(BYTES, 0, 8)));
  }

  private static byte[] subArray(byte[] bytes, int offset, int length) {
    byte[] out = new byte[length];
    System.arraycopy(bytes, offset, out, 0, length);
    return out;
  }

  public void testEncoder_simple_basic() throws Exception {
    testEncoder_simple(Base64.getEncoder(), Base64.getDecoder());
  }

  public void testEncoder_simple_mime() throws Exception {
    testEncoder_simple(Base64.getMimeEncoder(), Base64.getMimeDecoder());
  }

  public void testEncoder_simple_url() throws Exception {
    testEncoder_simple(Base64.getUrlEncoder(), Base64.getUrlDecoder());
  }

  private void testEncoder_simple(Base64.Encoder encoder, Base64.Decoder decoder) throws Exception {
    assertEquals("YQ==", encodeToString(encoder, decoder, "a"));
    assertEquals("YWI=", encodeToString(encoder, decoder, "ab"));
    assertEquals("YWJj", encodeToString(encoder, decoder, "abc"));
    assertEquals("YWJjZA==", encodeToString(encoder, decoder, "abcd"));
  }

  public void testEncoder_lineLength() throws Exception {
    String in_56 = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcd";
    String in_57 = in_56 + "e";
    String in_58 = in_56 + "ef";
    String in_59 = in_56 + "efg";
    String in_60 = in_56 + "efgh";
    String in_61 = in_56 + "efghi";

    String prefix = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXphYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5emFi";
    String out_56 = prefix + "Y2Q=";
    String out_57 = prefix + "Y2Rl";
    String out_58 = prefix + "Y2Rl\r\nZg==";
    String out_59 = prefix + "Y2Rl\r\nZmc=";
    String out_60 = prefix + "Y2Rl\r\nZmdo";
    String out_61 = prefix + "Y2Rl\r\nZmdoaQ==";

    Base64.Encoder encoder = Base64.getMimeEncoder();
    Base64.Decoder decoder = Base64.getMimeDecoder();
    assertEquals("", encodeToString(encoder, decoder, ""));
    assertEquals(out_56, encodeToString(encoder, decoder, in_56));
    assertEquals(out_57, encodeToString(encoder, decoder, in_57));
    assertEquals(out_58, encodeToString(encoder, decoder, in_58));
    assertEquals(out_59, encodeToString(encoder, decoder, in_59));
    assertEquals(out_60, encodeToString(encoder, decoder, in_60));
    assertEquals(out_61, encodeToString(encoder, decoder, in_61));

    encoder = Base64.getUrlEncoder();
    decoder = Base64.getUrlDecoder();
    assertEquals(out_56.replaceAll("\r\n", ""), encodeToString(encoder, decoder, in_56));
    assertEquals(out_57.replaceAll("\r\n", ""), encodeToString(encoder, decoder, in_57));
    assertEquals(out_58.replaceAll("\r\n", ""), encodeToString(encoder, decoder, in_58));
    assertEquals(out_59.replaceAll("\r\n", ""), encodeToString(encoder, decoder, in_59));
    assertEquals(out_60.replaceAll("\r\n", ""), encodeToString(encoder, decoder, in_60));
    assertEquals(out_61.replaceAll("\r\n", ""), encodeToString(encoder, decoder, in_61));
  }

  public void testGetMimeEncoder_withLineSeparator() {
    byte[] validLineBreak = { '*' };
    assertEncodesWithoutSeparator(Base64.getMimeEncoder(Integer.MIN_VALUE, validLineBreak));
    assertEncodesWithoutSeparator(Base64.getMimeEncoder(-1, validLineBreak));
    assertEncodesWithoutSeparator(Base64.getMimeEncoder(0, validLineBreak));
    assertEncodesWithoutSeparator(Base64.getMimeEncoder(1, validLineBreak));
    assertEncodesWithSeparator(4, validLineBreak, Base64.getMimeEncoder(4, validLineBreak));
    assertEncodesWithSeparator(20, validLineBreak, Base64.getMimeEncoder(20, validLineBreak));
    assertEncodesWithSeparator(76, validLineBreak, Base64.getMimeEncoder(76, validLineBreak));
    assertEncodesWithSeparator(Integer.MAX_VALUE, validLineBreak,
        Base64.getMimeEncoder(Integer.MAX_VALUE, validLineBreak));

    byte[] emptyLineBreak = new byte[0];
    assertEncodesWithoutSeparator(Base64.getMimeEncoder(0, emptyLineBreak));
    assertEncodesWithoutSeparator(Base64.getMimeEncoder(20, new byte[0]));

    byte[] invalidLineBreak = { 'A' };
    try {
      Base64.getMimeEncoder(20, invalidLineBreak);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      Base64.getMimeEncoder(0, invalidLineBreak);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      Base64.getMimeEncoder(20, null);
      fail();
    } catch (NullPointerException expected) {
    }

    try {
      Base64.getMimeEncoder(0, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  private static final String LONG_INPUT_STRING
      = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghi";

  private static final String LONG_ENCODED_STRING
      = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXphYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5emFiY2RlZmdoaQ==";

  private static void assertEncodesWithSeparator(
      int expectedLineBreakInterval, byte[] lineSeparator, Base64.Encoder encoder) {

    String lineSeparatorString = new String(lineSeparator, StandardCharsets.US_ASCII);
    String input = LONG_INPUT_STRING;
    String encodedWithoutLinebreaks = LONG_ENCODED_STRING;
    StringBuilder expectedWithLineBreaks = new StringBuilder();
    int i = 0;
    while (i + expectedLineBreakInterval < encodedWithoutLinebreaks.length()) {
      String line = encodedWithoutLinebreaks.substring(i, i + expectedLineBreakInterval);
      expectedWithLineBreaks.append(line);
      expectedWithLineBreaks.append(lineSeparatorString);
      i += expectedLineBreakInterval;
    }
    expectedWithLineBreaks.append(encodedWithoutLinebreaks.substring(i));

    String encoded = encoder.encodeToString(input.getBytes(StandardCharsets.US_ASCII));
    assertEquals(expectedWithLineBreaks.toString(), encoded);
  }

  private void assertEncodesWithoutSeparator(Base64.Encoder encoder) {
    String encoded = encoder.encodeToString(LONG_INPUT_STRING.getBytes(StandardCharsets.US_ASCII));
    assertEquals(LONG_ENCODED_STRING, encoded);
  }

  // Test the various methods on Encoder that encode arrays. Assumes all of them basically do the
  // same thing as tested extensively above but checks for quirks around parameters.
  public void testEncoder_encodeArrayMethods() {
    Base64.Encoder encoder = Base64.getEncoder();

    // Edge cases for empty input.
    byte[] emptyByteArray = new byte[0];
    assertEquals(emptyByteArray, encoder.encode(emptyByteArray));
    assertEquals(0, encoder.encode(emptyByteArray, emptyByteArray));
    assertEquals("", encoder.encodeToString(emptyByteArray));

    // Non-empty input.
    byte[] input = "abcefghi".getBytes(StandardCharsets.US_ASCII);
    String expectedString = "YWJjZWZnaGk=";
    byte[] expectedBytes = expectedString.getBytes(StandardCharsets.US_ASCII);

    byte[] encodedBytes = encoder.encode(input);
    assertEquals(expectedBytes, encodedBytes);

    byte[] tooShort = new byte[encodedBytes.length - 1];
    try {
      encoder.encode(input, tooShort);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    byte[] tooLong = new byte[encodedBytes.length + 1];
    int tooLongBytesEncoded = encoder.encode(input, tooLong);
    assertEquals(encodedBytes.length, tooLongBytesEncoded);
    assertEquals(0, tooLong[tooLong.length - 1]);
    assertEquals(tooLong, encodedBytes.length, encodedBytes);

    byte[] justRight = new byte[encodedBytes.length];
    int justRightBytesEncoded = encoder.encode(input, justRight);
    assertEquals(encodedBytes.length, justRightBytesEncoded);
    assertEquals(encodedBytes, justRight);

    String encodedAsString = encoder.encodeToString(input);
    assertEquals(expectedString, encodedAsString);
  }

  public void testEncoder_encodeByteBuffer() {
    Base64.Encoder encoder = Base64.getEncoder();

    byte[] emptyByteArray = new byte[0];
    ByteBuffer emptyByteBuffer = ByteBuffer.wrap(emptyByteArray);
    ByteBuffer emptyEncodedBuffer = encoder.encode(emptyByteBuffer);
    assertEquals(emptyByteBuffer, emptyEncodedBuffer);
    assertNotSame(emptyByteArray, emptyEncodedBuffer);

    // Test the two types of byte buffer.
    byte[] input = "abcefghi".getBytes(StandardCharsets.US_ASCII);
    String expectedString = "YWJjZWZnaGk=";
    byte[] expectedBytes = expectedString.getBytes(StandardCharsets.US_ASCII);

    ByteBuffer inputBuffer = ByteBuffer.allocate(input.length);
    inputBuffer.put(input);
    inputBuffer.position(0);
    testEncoder_encodeByteBuffer(encoder, inputBuffer, expectedBytes);

    inputBuffer = ByteBuffer.allocateDirect(input.length);
    inputBuffer.put(input);
    inputBuffer.position(0);
    testEncoder_encodeByteBuffer(encoder, inputBuffer, expectedBytes);
  }

  private static void testEncoder_encodeByteBuffer(
      Base64.Encoder encoder, ByteBuffer inputBuffer, byte[] expectedBytes) {
    assertEquals(0, inputBuffer.position());
    assertEquals(inputBuffer.remaining(), inputBuffer.limit());
    int inputLength = inputBuffer.remaining();

    ByteBuffer encodedBuffer = encoder.encode(inputBuffer);

    assertEquals(inputLength, inputBuffer.position());
    assertEquals(0, inputBuffer.remaining());
    assertEquals(inputLength, inputBuffer.limit());
    assertEquals(0, encodedBuffer.position());
    assertEquals(expectedBytes.length, encodedBuffer.remaining());
    assertEquals(expectedBytes.length, encodedBuffer.limit());
  }

  // Test the various methods on Decoder that decode arrays. Assumes all of them basically do the
  // same thing as tested extensively above but checks for quirks around parameters.
  public void testDecoder_decodeArrayMethods() {
    Base64.Decoder decoder = Base64.getDecoder();

    // Edge cases for empty input.
    byte[] emptyByteArray = new byte[0];
    assertEquals(emptyByteArray, decoder.decode(emptyByteArray));
    assertEquals(0, decoder.decode(emptyByteArray, emptyByteArray));
    assertEquals(emptyByteArray, decoder.decode(""));

    // Non-empty input.
    String inputString = "YWJjZWZnaGk=";
    byte[] input = inputString.getBytes(StandardCharsets.US_ASCII);
    String expectedString = "abcefghi";
    byte[] expectedBytes = expectedString.getBytes(StandardCharsets.US_ASCII);

    byte[] decodedBytes = decoder.decode(input);
    assertEquals(expectedBytes, decodedBytes);

    byte[] tooShort = new byte[decodedBytes.length - 1];
    try {
      decoder.decode(input, tooShort);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    byte[] tooLong = new byte[decodedBytes.length + 1];
    int tooLongBytesDecoded = decoder.decode(input, tooLong);
    assertEquals(decodedBytes.length, tooLongBytesDecoded);
    assertEquals(0, tooLong[tooLong.length - 1]);
    assertEquals(tooLong, decodedBytes.length, decodedBytes);

    byte[] justRight = new byte[decodedBytes.length];
    int justRightBytesDecoded = decoder.decode(input, justRight);
    assertEquals(decodedBytes.length, justRightBytesDecoded);
    assertEquals(decodedBytes, justRight);

    byte[] decodedString = decoder.decode(inputString);
    assertEquals(expectedBytes, decodedString);
  }

  public void testDecoder_decodeByteBuffer() {
    Base64.Decoder decoder = Base64.getDecoder();

    byte[] emptyByteArray = new byte[0];
    ByteBuffer emptyByteBuffer = ByteBuffer.wrap(emptyByteArray);
    ByteBuffer emptyDecodedBuffer = decoder.decode(emptyByteBuffer);
    assertEquals(emptyByteBuffer, emptyDecodedBuffer);
    assertNotSame(emptyByteArray, emptyDecodedBuffer);

    // Test the two types of byte buffer.
    String inputString = "YWJjZWZnaGk=";
    byte[] input = inputString.getBytes(StandardCharsets.US_ASCII);
    String expectedString = "abcefghi";
    byte[] expectedBytes = expectedString.getBytes(StandardCharsets.US_ASCII);

    ByteBuffer inputBuffer = ByteBuffer.allocate(input.length);
    inputBuffer.put(input);
    inputBuffer.position(0);
    testDecoder_decodeByteBuffer(decoder, inputBuffer, expectedBytes);

    inputBuffer = ByteBuffer.allocateDirect(input.length);
    inputBuffer.put(input);
    inputBuffer.position(0);
    testDecoder_decodeByteBuffer(decoder, inputBuffer, expectedBytes);
  }

  private static void testDecoder_decodeByteBuffer(
      Base64.Decoder decoder, ByteBuffer inputBuffer, byte[] expectedBytes) {
    assertEquals(0, inputBuffer.position());
    assertEquals(inputBuffer.remaining(), inputBuffer.limit());
    int inputLength = inputBuffer.remaining();

    ByteBuffer decodedBuffer = decoder.decode(inputBuffer);

    assertEquals(inputLength, inputBuffer.position());
    assertEquals(0, inputBuffer.remaining());
    assertEquals(inputLength, inputBuffer.limit());
    assertEquals(0, decodedBuffer.position());
    assertEquals(expectedBytes.length, decodedBuffer.remaining());
    assertEquals(expectedBytes.length, decodedBuffer.limit());
  }

  public void testDecoder_decodeByteBuffer_invalidData() {
    Base64.Decoder decoder = Base64.getDecoder();

    // Test the two types of byte buffer.
    String inputString = "AAAA AAAA";
    byte[] input = inputString.getBytes(StandardCharsets.US_ASCII);

    ByteBuffer inputBuffer = ByteBuffer.allocate(input.length);
    inputBuffer.put(input);
    inputBuffer.position(0);
    testDecoder_decodeByteBuffer_invalidData(decoder, inputBuffer);

    inputBuffer = ByteBuffer.allocateDirect(input.length);
    inputBuffer.put(input);
    inputBuffer.position(0);
    testDecoder_decodeByteBuffer_invalidData(decoder, inputBuffer);
  }

  private static void testDecoder_decodeByteBuffer_invalidData(
      Base64.Decoder decoder, ByteBuffer inputBuffer) {
    assertEquals(0, inputBuffer.position());
    assertEquals(inputBuffer.remaining(), inputBuffer.limit());
    int limit = inputBuffer.limit();

    try {
      decoder.decode(inputBuffer);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    assertEquals(0, inputBuffer.position());
    assertEquals(limit, inputBuffer.remaining());
    assertEquals(limit, inputBuffer.limit());
  }

  private static final String lipsum =
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

  public void testDecoder_wrap_basic() throws Exception {
    Base64.Encoder encoder = Base64.getEncoder();
    Base64.Decoder decoder = Base64.getDecoder();
    testInputStream(encoder, decoder);
  }

  public void testDecoder_wrap_mime() throws Exception {
    Base64.Encoder encoder = Base64.getMimeEncoder();
    Base64.Decoder decoder = Base64.getMimeDecoder();
    testInputStream(encoder, decoder);
  }

  public void testDecoder_wrap_url() throws Exception {
    Base64.Encoder encoder = Base64.getUrlEncoder();
    Base64.Decoder decoder = Base64.getUrlDecoder();
    testInputStream(encoder, decoder);
  }

  private void testInputStream(Base64.Encoder encoder, Base64.Decoder decoder)
      throws IOException {
    Random rng = new Random(32176L);
    int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };

    // Test input needs to be at least 2048 bytes to fill up the
    // read buffer of Base64InputStream.
    byte[] plain = (lipsum + lipsum + lipsum + lipsum + lipsum).getBytes();
    byte[] encoded = encoder.encode(plain);
    byte[] actual = new byte[plain.length * 2];
    int b;

    // ----- test decoding ("encoded" -> "plain") -----

    // read as much as it will give us in one chunk
    ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
    InputStream b64is = decoder.wrap(bais);
    int ap = 0;
    while ((b = b64is.read(actual, ap, actual.length - ap)) != -1) {
      ap += b;
    }
    assertEquals(actual, ap, plain);

    // read individual bytes
    bais = new ByteArrayInputStream(encoded);
    b64is = decoder.wrap(bais);
    ap = 0;
    while ((b = b64is.read()) != -1) {
      actual[ap++] = (byte) b;
    }
    assertEquals(actual, ap, plain);

    // mix reads of variously-sized arrays with one-byte reads
    bais = new ByteArrayInputStream(encoded);
    b64is = decoder.wrap(bais);
    ap = 0;
    while (true) {
      int l = writeLengths[rng.nextInt(writeLengths.length)];
      if (l >= 0) {
        b = b64is.read(actual, ap, l);
        if (b == -1) {
          break;
        }
        ap += b;
      } else {
        for (int i = 0; i < -l; ++i) {
          if ((b = b64is.read()) == -1) {
            break;
          }
          actual[ap++] = (byte) b;
        }
      }
    }
    assertEquals(actual, ap, plain);
  }

  public void testDecoder_wrap_singleByteReads() throws IOException {
    InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream("/v8=".getBytes()));
    assertEquals(254, in.read());
    assertEquals(255, in.read());
  }

  public void testEncoder_wrap_basic() throws Exception {
    testOutputStream(Base64.getEncoder());
  }

  public void testEncoder_wrap_url() throws Exception {
    testOutputStream(Base64.getUrlEncoder());
  }

  public void testEncoder_wrap_mime() throws Exception {
    testOutputStream(Base64.getMimeEncoder());
  }

  private void testOutputStream(Base64.Encoder encoder) throws Exception {

    int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };
    Random rng = new Random(32176L);

    // Test input needs to be at least 1024 bytes to test filling
    // up the write(int) buffer of Base64OutputStream.
    byte[] plain = (lipsum + lipsum).getBytes();

    byte[] encoded = encoder.encode(plain);

    ByteArrayOutputStream baos;
    OutputStream b64os;
    byte[] actual;
    int p;

    // ----- test encoding ("plain" -> "encoded") -----

    // one large write(byte[]) of the whole input
    baos = new ByteArrayOutputStream();
    b64os = encoder.wrap(baos);
    b64os.write(plain);
    b64os.close();
    actual = baos.toByteArray();
    assertEquals(encoded, actual);

    // many calls to write(int)
    baos = new ByteArrayOutputStream();
    b64os = encoder.wrap(baos);
    for (byte aPlain : plain) {
      b64os.write(aPlain);
    }
    b64os.close();
    actual = baos.toByteArray();
    assertEquals(encoded, actual);

    // intermixed sequences of write(int) with
    // write(byte[],int,int) of various lengths.
    baos = new ByteArrayOutputStream();
    b64os = encoder.wrap(baos);
    p = 0;
    while (p < plain.length) {
      int l = writeLengths[rng.nextInt(writeLengths.length)];
      l = Math.min(l, plain.length - p);
      if (l >= 0) {
        b64os.write(plain, p, l);
        p += l;
      } else {
        l = Math.min(-l, plain.length - p);
        for (int i = 0; i < l; ++i) {
          b64os.write(plain[p + i]);
        }
        p += l;
      }
    }
    b64os.close();
    actual = baos.toByteArray();
    assertEquals(encoded, actual);
  }

  /** Decodes a string, returning a string. */
  private static String decodeString(Base64.Decoder decoder, String in) throws Exception {
    byte[] out = decoder.decode(in);
    return new String(out);
  }

  /**
   * Encodes the string 'in' using 'flags'.  Asserts that decoding
   * gives the same string.  Returns the encoded string.
   */
  private static String encodeToString(Base64.Encoder encoder, Base64.Decoder decoder, String in)
      throws Exception {
    String b64 = encoder.encodeToString(in.getBytes());
    String dec = decodeString(decoder, b64);
    assertEquals(in, dec);
    return b64;
  }

  /** Assert that decoding 'in' throws IllegalArgumentException. */
  private static void assertBad(Base64.Decoder decoder, String in) throws Exception {
    try {
      decoder.decode(in);
      fail("should have failed to decode");
    } catch (IllegalArgumentException e) {
    }
  }

  /** Assert that actual equals the first len bytes of expected. */
  private static void assertEquals(byte[] expected, int len, byte[] actual) {
    assertEquals(len, actual.length);
    for (int i = 0; i < len; ++i) {
      assertEquals(expected[i], actual[i]);
    }
  }

  /** Assert that actual equals the first len bytes of expected. */
  private static void assertEquals(byte[] expected, byte[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], actual[i]);
    }
  }

}
