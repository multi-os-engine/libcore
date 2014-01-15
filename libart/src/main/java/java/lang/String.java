/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.Charsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Pattern;
import libcore.util.EmptyArray;

/**
 * An immutable sequence of characters/code units ({@code char}s). A
 * {@code String} is represented by array of UTF-16 values, such that
 * Unicode supplementary characters (code points) are stored/encoded as
 * surrogate pairs via Unicode code units ({@code char}).
 *
 * @see StringBuffer
 * @see StringBuilder
 * @see Charset
 * @since 1.0
 */
public final class String implements Serializable, Comparable<String>, CharSequence {

    private static final long serialVersionUID = -6849794470754667710L;

    private static final char REPLACEMENT_CHAR = (char) 0xfffd;

    /**
     * CaseInsensitiveComparator compares Strings ignoring the case of the
     * characters.
     */
    private static final class CaseInsensitiveComparator implements
            Comparator<String>, Serializable {
        private static final long serialVersionUID = 8575799808933029326L;

        /**
         * Compare the two objects to determine the relative ordering.
         *
         * @param o1
         *            an Object to compare
         * @param o2
         *            an Object to compare
         * @return an int < 0 if object1 is less than object2, 0 if they are
         *         equal, and > 0 if object1 is greater
         *
         * @exception ClassCastException
         *                if objects are not the correct type
         */
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    /**
     * A comparator ignoring the case of the characters.
     */
    public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

    private static final char[] ASCII;
    static {
        ASCII = new char[128];
        for (int i = 0; i < ASCII.length; ++i) {
            ASCII[i] = (char) i;
        }
    }

    private final int count;

    private int hashCode;

    /**
     * Creates an empty string.
     */
    public String() {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a string using the system's
     * {@link java.nio.charset.Charset#defaultCharset default charset}.
     */
    @FindBugsSuppressWarnings("DM_DEFAULT_ENCODING")
    public String(byte[] data) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a string, setting the high byte of every
     * character to the specified value.
     *
     * @param data
     *            the byte array to convert to a string.
     * @param high
     *            the high byte to use.
     * @throws NullPointerException
     *             if {@code data == null}.
     * @deprecated Use {@link #String(byte[])} or {@link #String(byte[], String)} instead.
     */
    @Deprecated
    public String(byte[] data, int high) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts a subsequence of the byte array to a string using the system's
     * {@link java.nio.charset.Charset#defaultCharset default charset}.
     *
     * @throws NullPointerException
     *             if {@code data == null}.
     * @throws IndexOutOfBoundsException
     *             if {@code byteCount < 0 || offset < 0 || offset + byteCount > data.length}.
     */
    public String(byte[] data, int offset, int byteCount) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a string, setting the high byte of every
     * character to {@code high}.
     *
     * @throws NullPointerException
     *             if {@code data == null}.
     * @throws IndexOutOfBoundsException
     *             if {@code byteCount < 0 || offset < 0 || offset + byteCount > data.length}
     *
     * @deprecated Use {@link #String(byte[], int, int)} instead.
     */
    @Deprecated
    public String(byte[] data, int high, int offset, int byteCount) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a string using the named charset.
     *
     * <p>The behavior when the bytes cannot be decoded by the named charset
     * is unspecified. Use {@link java.nio.charset.CharsetDecoder} for more control.
     *
     * @throws NullPointerException
     *             if {@code data == null}.
     * @throws IndexOutOfBoundsException
     *             if {@code byteCount < 0 || offset < 0 || offset + byteCount > data.length}.
     * @throws UnsupportedEncodingException
     *             if the named charset is not supported.
     */
    public String(byte[] data, int offset, int byteCount, String charsetName) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a string using the named charset.
     *
     * <p>The behavior when the bytes cannot be decoded by the named charset
     * is unspecified. Use {@link java.nio.charset.CharsetDecoder} for more control.
     *
     * @throws NullPointerException
     *             if {@code data == null}.
     * @throws UnsupportedEncodingException
     *             if {@code charsetName} is not supported.
     */
    public String(byte[] data, String charsetName) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a string using the given charset.
     *
     * <p>The behavior when the bytes cannot be decoded by the given charset
     * is to replace malformed input and unmappable characters with the charset's default
     * replacement string. Use {@link java.nio.charset.CharsetDecoder} for more control.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code byteCount < 0 || offset < 0 || offset + byteCount > data.length}
     * @throws NullPointerException
     *             if {@code data == null}
     *
     * @since 1.6
     */
    public String(byte[] data, int offset, int byteCount, Charset charset) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Converts the byte array to a String using the given charset.
     *
     * @throws NullPointerException if {@code data == null}
     * @since 1.6
     */
    public String(byte[] data, Charset charset) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Initializes this string to contain the characters in the specified
     * character array. Modifying the character array after creating the string
     * has no effect on the string.
     *
     * @throws NullPointerException if {@code data == null}
     */
    public String(char[] data) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Initializes this string to contain the specified characters in the
     * character array. Modifying the character array after creating the string
     * has no effect on the string.
     *
     * @throws NullPointerException
     *             if {@code data == null}.
     * @throws IndexOutOfBoundsException
     *             if {@code charCount < 0 || offset < 0 || offset + charCount > data.length}
     */
    public String(char[] data, int offset, int charCount) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /*
     * Internal version of the String(char[], int, int) constructor.
     * Does not range check or null check.
     */
    // TODO: Replace calls to this with calls to StringFactory, will require
    // splitting other files in java.lang.
    String(int offset, int charCount, char[] chars) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Constructs a new string with the same sequence of characters as {@code
     * toCopy}.
     */
    public String(String toCopy) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Creates a {@code String} from the contents of the specified
     * {@code StringBuffer}.
     */
    public String(StringBuffer stringBuffer) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Creates a {@code String} from the sub-array of Unicode code points.
     *
     * @throws NullPointerException
     *             if {@code codePoints == null}.
     * @throws IllegalArgumentException
     *             if any of the elements of {@code codePoints} are not valid
     *             Unicode code points.
     * @throws IndexOutOfBoundsException
     *             if {@code offset} or {@code count} are not within the bounds
     *             of {@code codePoints}.
     * @since 1.5
     */
    public String(int[] codePoints, int offset, int count) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Creates a {@code String} from the contents of the specified {@code
     * StringBuilder}.
     *
     * @throws NullPointerException
     *             if {@code stringBuilder == null}.
     * @since 1.5
     */
    public String(StringBuilder stringBuilder) {
        throw new UnsupportedOperationException("Use StringFactory instead.");
    }

    /**
     * Returns the character at {@code index}.
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= length()}.
     */
    public native char charAt(int index);

    native void setCharAt(int index, char c);

    private StringIndexOutOfBoundsException indexAndLength(int index) {
        throw new StringIndexOutOfBoundsException(this, index);
    }

    private StringIndexOutOfBoundsException startEndAndLength(int start, int end) {
        throw new StringIndexOutOfBoundsException(this, start, end - start);
    }

    private StringIndexOutOfBoundsException failedBoundsCheck(int arrayLength, int offset, int count) {
        throw new StringIndexOutOfBoundsException(arrayLength, offset, count);
    }

    /**
     * This isn't equivalent to either of ICU's u_foldCase case folds, and thus any of the Unicode
     * case folds, but it's what the RI uses.
     */
    private char foldCase(char ch) {
        if (ch < 128) {
            if ('A' <= ch && ch <= 'Z') {
                return (char) (ch + ('a' - 'A'));
            }
            return ch;
        }
        return Character.toLowerCase(Character.toUpperCase(ch));
    }

    /**
     * Compares the specified string to this string using the Unicode values of
     * the characters. Returns 0 if the strings contain the same characters in
     * the same order. Returns a negative integer if the first non-equal
     * character in this string has a Unicode value which is less than the
     * Unicode value of the character at the same position in the specified
     * string, or if this string is a prefix of the specified string. Returns a
     * positive integer if the first non-equal character in this string has a
     * Unicode value which is greater than the Unicode value of the character at
     * the same position in the specified string, or if the specified string is
     * a prefix of this string.
     *
     * @param string
     *            the string to compare.
     * @return 0 if the strings are equal, a negative integer if this string is
     *         before the specified string, or a positive integer if this string
     *         is after the specified string.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public native int compareTo(String string);

    /**
     * Compares the specified string to this string using the Unicode values of
     * the characters, ignoring case differences. Returns 0 if the strings
     * contain the same characters in the same order. Returns a negative integer
     * if the first non-equal character in this string has a Unicode value which
     * is less than the Unicode value of the character at the same position in
     * the specified string, or if this string is a prefix of the specified
     * string. Returns a positive integer if the first non-equal character in
     * this string has a Unicode value which is greater than the Unicode value
     * of the character at the same position in the specified string, or if the
     * specified string is a prefix of this string.
     *
     * @param string
     *            the string to compare.
     * @return 0 if the strings are equal, a negative integer if this string is
     *         before the specified string, or a positive integer if this string
     *         is after the specified string.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int compareToIgnoreCase(String string) {
        int result;
        int end = count < string.count ? count : string.count;
        char c1, c2;
        for (int i = 0; i < end; ++i) {
            if ((c1 = charAt(i)) == (c2 = string.charAt(i))) {
                continue;
            }
            c1 = foldCase(c1);
            c2 = foldCase(c2);
            if ((result = c1 - c2) != 0) {
                return result;
            }
        }
        return count - string.count;
    }

    /**
     * Concatenates this string and the specified string.
     *
     * @param string
     *            the string to concatenate
     * @return a new string which is the concatenation of this string and the
     *         specified string.
     */
    public native String concat(String string);

    /**
     * Creates a new string containing the characters in the specified character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     *
     * @param data
     *            the array of characters.
     * @return the new string.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     */
    public static String copyValueOf(char[] data) {
        return StringFactory.newStringFromChars(data, 0, data.length);
    }

    /**
     * Creates a new string containing the specified characters in the character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     *
     * @param data
     *            the array of characters.
     * @param start
     *            the starting offset in the character array.
     * @param length
     *            the number of characters to use.
     * @return the new string.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code length < 0, start < 0} or {@code start + length >
     *             data.length}.
     */
    public static String copyValueOf(char[] data, int start, int length) {
        return StringFactory.newStringFromChars(data, start, length);
    }

    /**
     * Compares the specified string to this string to determine if the
     * specified string is a suffix.
     *
     * @param suffix
     *            the suffix to look for.
     * @return {@code true} if the specified string is a suffix of this string,
     *         {@code false} otherwise.
     * @throws NullPointerException
     *             if {@code suffix} is {@code null}.
     */
    public boolean endsWith(String suffix) {
        return regionMatches(count - suffix.count, suffix, 0, suffix.count);
    }

    /**
     * Compares the specified object to this string and returns true if they are
     * equal. The object must be an instance of string with the same characters
     * in the same order.
     *
     * @param other
     *            the object to compare.
     * @return {@code true} if the specified object is equal to this string,
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override public boolean equals(Object other) {
        if (other == this) {
          return true;
        }
        if (other instanceof String) {
            String s = (String)other;
            int count = this.count;
            if (s.count != count) {
                return false;
            }
            // TODO: we want to avoid many boundchecks in the loop below
            // for long Strings until we have array equality intrinsic.
            // Bad benchmarks just push .equals without first getting a
            // hashCode hit (unlike real world use in a Hashtable). Filter
            // out these long strings here. When we get the array equality
            // intrinsic then remove this use of hashCode.
            if (hashCode() != s.hashCode()) {
                return false;
            }
            for (int i = 0; i < count; ++i) {
                if (charAt(i) != s.charAt(i)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares the specified string to this string ignoring the case of the
     * characters and returns true if they are equal.
     *
     * @param string
     *            the string to compare.
     * @return {@code true} if the specified string is equal to this string,
     *         {@code false} otherwise.
     */
    @FindBugsSuppressWarnings("ES_COMPARING_PARAMETER_STRING_WITH_EQ")
    public boolean equalsIgnoreCase(String string) {
        if (string == this) {
            return true;
        }
        if (string == null || count != string.count) {
            return false;
        }
        for (int i = 0; i < count; ++i) {
            char c1 = charAt(i);
            char c2 = string.charAt(i);
            if (c1 != c2 && foldCase(c1) != foldCase(c2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mangles this string into a byte array by stripping the high order bits from
     * each character. Use {@link #getBytes()} or {@link #getBytes(String)} instead.
     *
     * @param start
     *            the starting offset of characters to copy.
     * @param end
     *            the ending offset of characters to copy.
     * @param data
     *            the destination byte array.
     * @param index
     *            the starting offset in the destination byte array.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end > length()}, {@code index <
     *             0} or {@code end - start > data.length - index}.
     * @deprecated Use {@link #getBytes()} or {@link #getBytes(String)}
     */
    @Deprecated
    public void getBytes(int start, int end, byte[] data, int index) {
        // Note: last character not copied!
        if (start >= 0 && start <= end && end <= count) {
            try {
                for (int i = start; i < end; ++i) {
                    data[index++] = (byte) charAt(i);
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
                throw failedBoundsCheck(data.length, index, end - start);
            }
        } else {
            throw startEndAndLength(start, end);
        }
    }

    /**
     * Returns a new byte array containing the characters of this string encoded using the
     * system's {@link java.nio.charset.Charset#defaultCharset default charset}.
     *
     * <p>The behavior when this string cannot be represented in the system's default charset
     * is unspecified. In practice, when the default charset is UTF-8 (as it is on Android),
     * all strings can be encoded.
     */
    public byte[] getBytes() {
        return getBytes(Charset.defaultCharset());
    }

    /**
     * Returns a new byte array containing the characters of this string encoded using the
     * named charset.
     *
     * <p>The behavior when this string cannot be represented in the named charset
     * is unspecified. Use {@link java.nio.charset.CharsetEncoder} for more control.
     *
     * @throws UnsupportedEncodingException if the charset is not supported
     */
    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        return getBytes(Charset.forNameUEE(charsetName));
    }

    /**
     * Returns a new byte array containing the characters of this string encoded using the
     * given charset.
     *
     * <p>The behavior when this string cannot be represented in the given charset
     * is to replace malformed input and unmappable characters with the charset's default
     * replacement byte array. Use {@link java.nio.charset.CharsetEncoder} for more control.
     *
     * @since 1.6
     */
    public byte[] getBytes(Charset charset) {
        String canonicalCharsetName = charset.name();
        if (canonicalCharsetName.equals("UTF-8")) {
            return Charsets.toUtf8Bytes(this, 0, count);
        } else if (canonicalCharsetName.equals("ISO-8859-1")) {
            return Charsets.toIsoLatin1Bytes(this, 0, count);
        } else if (canonicalCharsetName.equals("US-ASCII")) {
            return Charsets.toAsciiBytes(this, 0, count);
        } else if (canonicalCharsetName.equals("UTF-16BE")) {
            return Charsets.toBigEndianUtf16Bytes(this, 0, count);
        } else {
            ByteBuffer buffer = charset.encode(this);
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            return bytes;
        }
    }

    /**
     * Copies the specified characters in this string to the character array
     * starting at the specified offset in the character array.
     *
     * @param start
     *            the starting offset of characters to copy.
     * @param end
     *            the ending offset of characters to copy.
     * @param buffer
     *            the destination character array.
     * @param index
     *            the starting offset in the character array.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end > length()}, {@code start >
     *             end}, {@code index < 0}, {@code end - start > buffer.length -
     *             index}
     */
    public void getChars(int start, int end, char[] buffer, int index) {
        // Note: last character not copied!
        if (start >= 0 && start <= end && end <= count) {
            if (buffer == null) {
                throw new NullPointerException("buffer == null");
            }
            if (index < 0) {
                throw new IndexOutOfBoundsException("index < 0");
            }
            if (end - start > buffer.length - index) {
                throw new ArrayIndexOutOfBoundsException("end - start > buffer.length - index");
            }
            getCharsNoCheck(start, end, buffer, index);
        } else {
            // We throw StringIndexOutOfBoundsException rather than System.arraycopy's AIOOBE.
            throw startEndAndLength(start, end);
        }
    }

    /**
     * Version of getChars without bounds checks, for use by other classes
     * within the java.lang package only.  The caller is responsible for
     * ensuring that start >= 0 && start <= end && end <= count.
     */
    native void getCharsNoCheck(int start, int end, char[] buffer, int index);

    @Override public int hashCode() {
        int hash = hashCode;
        if (hash == 0) {
            if (count == 0) {
                return 0;
            }
            for (int i = 0; i < count; ++i) {
                hash = 31 * hash + charAt(i);
            }
            hashCode = hash;
        }
        return hash;
    }

    /**
     * Searches in this string for the first index of the specified character.
     * The search for the character starts at the beginning and moves towards
     * the end of this string.
     *
     * @param c
     *            the character to find.
     * @return the index in this string of the specified character, -1 if the
     *         character isn't found.
     */
    public int indexOf(int c) {
        // TODO: just "return indexOf(c, 0);" when the JIT can inline that deep.
        if (c > 0xffff) {
            return indexOfSupplementary(c, 0);
        }
        return fastIndexOf(c, 0);
    }

    /**
     * Searches in this string for the index of the specified character. The
     * search for the character starts at the specified offset and moves towards
     * the end of this string.
     *
     * @param c
     *            the character to find.
     * @param start
     *            the starting offset.
     * @return the index in this string of the specified character, -1 if the
     *         character isn't found.
     */
    public int indexOf(int c, int start) {
        if (c > 0xffff) {
            return indexOfSupplementary(c, start);
        }
        return fastIndexOf(c, start);
    }

    private native int fastIndexOf(int c, int start);

    private int indexOfSupplementary(int c, int start) {
        if (!Character.isSupplementaryCodePoint(c)) {
            return -1;
        }
        char[] chars = Character.toChars(c);
        String needle = new String(0, chars.length, chars);
        return indexOf(needle, start);
    }

    /**
     * Searches in this string for the first index of the specified string. The
     * search for the string starts at the beginning and moves towards the end
     * of this string.
     *
     * @param string
     *            the string to find.
     * @return the index of the first character of the specified string in this
     *         string, -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int indexOf(String string) {
        int start = 0;
        int subCount = string.count;
        int _count = count;
        if (subCount > 0) {
            if (subCount > _count) {
                return -1;
            }
            char firstChar = string.charAt(0);
            while (true) {
                int i = indexOf(firstChar, start);
                if (i == -1 || subCount + i > _count) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < subCount && charAt(++o1) == string.charAt(o2)) {
                    // Intentionally empty
                }
                if (o2 == subCount) {
                    return i;
                }
                start = i + 1;
            }
        }
        return start < _count ? start : _count;
    }

    /**
     * Searches in this string for the index of the specified string. The search
     * for the string starts at the specified offset and moves towards the end
     * of this string.
     *
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the first character of the specified string in this
     *         string, -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code subString} is {@code null}.
     */
    public int indexOf(String subString, int start) {
        if (start < 0) {
            start = 0;
        }
        int subCount = subString.count;
        int _count = count;
        if (subCount > 0) {
            if (subCount + start > _count) {
                return -1;
            }
            char firstChar = subString.charAt(0);
            while (true) {
                int i = indexOf(firstChar, start);
                if (i == -1 || subCount + i > _count) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < subCount && charAt(++o1) == subString.charAt(o2)) {
                    // Intentionally empty
                }
                if (o2 == subCount) {
                    return i;
                }
                start = i + 1;
            }
        }
        return start < _count ? start : _count;
    }

    /**
     * Returns an interned string equal to this string. The VM maintains an internal set of
     * unique strings. All string literals found in loaded classes'
     * constant pools are automatically interned. Manually-interned strings are only weakly
     * referenced, so calling {@code intern} won't lead to unwanted retention.
     *
     * <p>Interning is typically used because it guarantees that for interned strings
     * {@code a} and {@code b}, {@code a.equals(b)} can be simplified to
     * {@code a == b}. (This is not true of non-interned strings.)
     *
     * <p>Many applications find it simpler and more convenient to use an explicit
     * {@link java.util.HashMap} to implement their own pools.
     */
    public native String intern();

    /**
     * Returns true if the length of this string is 0.
     *
     * @since 1.6
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns the last index of the code point {@code c}, or -1.
     * The search for the character starts at the end and moves towards the
     * beginning of this string.
     */
    public int lastIndexOf(int c) {
        if (c > 0xffff) {
            return lastIndexOfSupplementary(c, Integer.MAX_VALUE);
        }
        int _count = count;
        for (int i = _count - 1; i >= 0; --i) {
            if (charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the last index of the code point {@code c}, or -1.
     * The search for the character starts at offset {@code start} and moves towards
     * the beginning of this string.
     */
    public int lastIndexOf(int c, int start) {
        if (c > 0xffff) {
            return lastIndexOfSupplementary(c, start);
        }
        int _count = count;
        if (start >= 0) {
            if (start >= _count) {
                start = _count - 1;
            }
            for (int i = start; i >= 0; --i) {
                if (charAt(i) == c) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int lastIndexOfSupplementary(int c, int start) {
        if (!Character.isSupplementaryCodePoint(c)) {
            return -1;
        }
        char[] chars = Character.toChars(c);
        String needle = StringFactory.newStringFromChars(0, chars.length, chars);
        return lastIndexOf(needle, start);
    }

    /**
     * Searches in this string for the last index of the specified string. The
     * search for the string starts at the end and moves towards the beginning
     * of this string.
     *
     * @param string
     *            the string to find.
     * @return the index of the first character of the specified string in this
     *         string, -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int lastIndexOf(String string) {
        // Use count instead of count - 1 so lastIndexOf("") returns count
        return lastIndexOf(string, count);
    }

    /**
     * Searches in this string for the index of the specified string. The search
     * for the string starts at the specified offset and moves towards the
     * beginning of this string.
     *
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the first character of the specified string in this
     *         string , -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code subString} is {@code null}.
     */
    public int lastIndexOf(String subString, int start) {
        int subCount = subString.count;
        if (subCount <= count && start >= 0) {
            if (subCount > 0) {
                if (start > count - subCount) {
                    start = count - subCount;
                }
                // count and subCount are both >= 1
                char firstChar = subString.charAt(0);
                while (true) {
                    int i = lastIndexOf(firstChar, start);
                    if (i == -1) {
                        return -1;
                    }
                    int o1 = i, o2 = 0;
                    while (++o2 < subCount && charAt(++o1) == subString.charAt(o2)) {
                        // Intentionally empty
                    }
                    if (o2 == subCount) {
                        return i;
                    }
                    start = i - 1;
                }
            }
            return start < count ? start : count;
        }
        return -1;
    }

    /**
     * Returns the number of characters in this string.
     */
    public int length() {
        return count;
    }

    /**
     * Compares the specified string to this string and compares the specified
     * range of characters to determine if they are the same.
     *
     * @param thisStart
     *            the starting offset in this string.
     * @param string
     *            the string to compare.
     * @param start
     *            the starting offset in the specified string.
     * @param length
     *            the number of characters to compare.
     * @return {@code true} if the ranges of characters are equal, {@code false}
     *         otherwise
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public boolean regionMatches(int thisStart, String string, int start, int length) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        if (start < 0 || string.count - start < length) {
            return false;
        }
        if (thisStart < 0 || count - thisStart < length) {
            return false;
        }
        if (length <= 0) {
            return true;
        }
        for (int i = 0; i < length; ++i) {
            if (charAt(thisStart + i) != string.charAt(start + i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the specified string to this string and compares the specified
     * range of characters to determine if they are the same. When ignoreCase is
     * true, the case of the characters is ignored during the comparison.
     *
     * @param ignoreCase
     *            specifies if case should be ignored.
     * @param thisStart
     *            the starting offset in this string.
     * @param string
     *            the string to compare.
     * @param start
     *            the starting offset in the specified string.
     * @param length
     *            the number of characters to compare.
     * @return {@code true} if the ranges of characters are equal, {@code false}
     *         otherwise.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public boolean regionMatches(boolean ignoreCase, int thisStart, String string, int start, int length) {
        if (!ignoreCase) {
            return regionMatches(thisStart, string, start, length);
        }
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        if (thisStart < 0 || length > count - thisStart) {
            return false;
        }
        if (start < 0 || length > string.count - start) {
            return false;
        }
        int end = thisStart + length;
        while (thisStart < end) {
            char c1 = charAt(thisStart++);
            char c2 = string.charAt(start++);
            if (c1 != c2 && foldCase(c1) != foldCase(c2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies this string replacing occurrences of the specified character with
     * another character.
     *
     * @param oldChar
     *            the character to replace.
     * @param newChar
     *            the replacement character.
     * @return a new string with occurrences of oldChar replaced by newChar.
     */
    public String replace(char oldChar, char newChar) {
        String s = null;
        int _count = count;
        boolean copied = false;
        for (int i = 0; i < _count; ++i) {
            if (charAt(i) == oldChar) {
                if (!copied) {
                    s = StringFactory.newStringFromString(this);
                    copied = true;
                }
                s.setCharAt(i, newChar);
            }
        }

        return copied ? s : this;
    }

    /**
     * Copies this string replacing occurrences of the specified target sequence
     * with another sequence. The string is processed from the beginning to the
     * end.
     *
     * @param target
     *            the sequence to replace.
     * @param replacement
     *            the replacement sequence.
     * @return the resulting string.
     * @throws NullPointerException
     *             if {@code target} or {@code replacement} is {@code null}.
     */
    public String replace(CharSequence target, CharSequence replacement) {
        if (target == null) {
            throw new NullPointerException("target == null");
        }
        if (replacement == null) {
            throw new NullPointerException("replacement == null");
        }

        String targetString = target.toString();
        int matchStart = indexOf(targetString, 0);
        if (matchStart == -1) {
            // If there's nothing to replace, return the original string untouched.
            return this;
        }

        String replacementString = replacement.toString();

        // The empty target matches at the start and end and between each character.
        int targetLength = targetString.length();
        if (targetLength == 0) {
            // The result contains the original 'count' characters, a copy of the
            // replacement string before every one of those characters, and a final
            // copy of the replacement string at the end.
            int resultLength = count + (count + 1) * replacementString.length();
            StringBuilder result = new StringBuilder(resultLength);
            result.append(replacementString);
            for (int i = 0; i != count; ++i) {
                result.append(charAt(i));
                result.append(replacementString);
            }
            return result.toString();
        }

        StringBuilder result = new StringBuilder(count);
        int searchStart = 0;
        do {
            // Copy characters before the match...
            // TODO: Perform this faster than one char at a time?
            for (int i = searchStart; i < matchStart; ++i) {
                result.append(charAt(i));
            }
            // Insert the replacement...
            result.append(replacementString);
            // And skip over the match...
            searchStart = matchStart + targetLength;
        } while ((matchStart = indexOf(targetString, searchStart)) != -1);
        // Copy any trailing chars...
        // TODO: Perform this faster than one char at a time?
        for (int i = searchStart; i < count; ++i) {
            result.append(charAt(i));
        }
        return result.toString();
    }

    /**
     * Compares the specified string to this string to determine if the
     * specified string is a prefix.
     *
     * @param prefix
     *            the string to look for.
     * @return {@code true} if the specified string is a prefix of this string,
     *         {@code false} otherwise
     * @throws NullPointerException
     *             if {@code prefix} is {@code null}.
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Compares the specified string to this string, starting at the specified
     * offset, to determine if the specified string is a prefix.
     *
     * @param prefix
     *            the string to look for.
     * @param start
     *            the starting offset.
     * @return {@code true} if the specified string occurs in this string at the
     *         specified offset, {@code false} otherwise.
     * @throws NullPointerException
     *             if {@code prefix} is {@code null}.
     */
    public boolean startsWith(String prefix, int start) {
        return regionMatches(start, prefix, 0, prefix.count);
    }

    /**
     * Returns a string containing a suffix of this string. The returned string
     * shares this string's <a href="#backing_array">backing array</a>.
     *
     * @param start
     *            the offset of the first character.
     * @return a new string containing the characters from start to the end of
     *         the string.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0} or {@code start > length()}.
     */
    public String substring(int start) {
        if (start == 0) {
            return this;
        }
        if (start >= 0 && start <= count) {
            return fastSubstring(start, count - start);
        }
        throw indexAndLength(start);
    }

    /**
     * Returns a string containing a subsequence of characters from this string.
     * The returned string shares this string's <a href="#backing_array">backing
     * array</a>.
     *
     * @param start
     *            the offset of the first character.
     * @param end
     *            the offset one past the last character.
     * @return a new string containing the characters from start to end - 1
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code start > end} or {@code end >
     *             length()}.
     */
    public String substring(int start, int end) {
        if (start == 0 && end == count) {
            return this;
        }
        // NOTE last character not copied!
        // Fast range check.
        if (start >= 0 && start <= end && end <= count) {
            return fastSubstring(start, end - start);
        }
        throw startEndAndLength(start, end);
    }

    private native String fastSubstring(int start, int length);

    /**
     * Returns a new {@code char} array containing a copy of the characters in this string.
     * This is expensive and rarely useful. If you just want to iterate over the characters in
     * the string, use {@link #charAt} instead.
     */
    public native char[] toCharArray();

    /**
     * Converts this string to lower case, using the rules of the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @return a new lower case string, or {@code this} if it's already all lower case.
     */
    public String toLowerCase() {
        return CaseMapper.toLowerCase(Locale.getDefault(), this);
    }

    /**
     * Converts this string to lower case, using the rules of {@code locale}.
     *
     * <p>Most case mappings are unaffected by the language of a {@code Locale}. Exceptions include
     * dotted and dotless I in Azeri and Turkish locales, and dotted and dotless I and J in
     * Lithuanian locales. On the other hand, it isn't necessary to provide a Greek locale to get
     * correct case mapping of Greek characters: any locale will do.
     *
     * <p>See <a href="http://www.unicode.org/Public/UNIDATA/SpecialCasing.txt">http://www.unicode.org/Public/UNIDATA/SpecialCasing.txt</a>
     * for full details of context- and language-specific special cases.
     *
     * @return a new lower case string, or {@code this} if it's already all lower case.
     */
    public String toLowerCase(Locale locale) {
        return CaseMapper.toLowerCase(locale, this);
    }

    /**
     * Returns this string.
     */
    @Override
    public String toString() {
        return this;
    }

    /**
     * Converts this this string to upper case, using the rules of the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @return a new upper case string, or {@code this} if it's already all upper case.
     */
    public String toUpperCase() {
        return CaseMapper.toUpperCase(Locale.getDefault(), this, count);
    }

    /**
     * Converts this this string to upper case, using the rules of {@code locale}.
     *
     * <p>Most case mappings are unaffected by the language of a {@code Locale}. Exceptions include
     * dotted and dotless I in Azeri and Turkish locales, and dotted and dotless I and J in
     * Lithuanian locales. On the other hand, it isn't necessary to provide a Greek locale to get
     * correct case mapping of Greek characters: any locale will do.
     *
     * <p>See <a href="http://www.unicode.org/Public/UNIDATA/SpecialCasing.txt">http://www.unicode.org/Public/UNIDATA/SpecialCasing.txt</a>
     * for full details of context- and language-specific special cases.
     *
     * @return a new upper case string, or {@code this} if it's already all upper case.
     */
    public String toUpperCase(Locale locale) {
        return CaseMapper.toUpperCase(locale, this, count);
    }

    /**
     * Copies this string removing white space characters from the beginning and
     * end of the string.
     *
     * @return a new string with characters <code><= \\u0020</code> removed from
     *         the beginning and the end.
     */
    public String trim() {
        int start = 0, last = count - 1;
        int end = last;
        while ((start <= end) && (charAt(start) <= ' ')) {
            start++;
        }
        while ((end >= start) && (charAt(end) <= ' ')) {
            end--;
        }
        if (start == 0 && end == last) {
            return this;
        }
        return fastSubstring(start, end - start + 1);
    }

    /**
     * Creates a new string containing the characters in the specified character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     *
     * @param data
     *            the array of characters.
     * @return the new string.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     */
    public static String valueOf(char[] data) {
        return StringFactory.newStringFromChars(data, 0, data.length);
    }

    /**
     * Creates a new string containing the specified characters in the character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     *
     * @param data
     *            the array of characters.
     * @param start
     *            the starting offset in the character array.
     * @param length
     *            the number of characters to use.
     * @return the new string.
     * @throws IndexOutOfBoundsException
     *             if {@code length < 0}, {@code start < 0} or {@code start +
     *             length > data.length}
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     */
    public static String valueOf(char[] data, int start, int length) {
        return StringFactory.newStringFromChars(data, start, length);
    }

    /**
     * Converts the specified character to its string representation.
     *
     * @param value
     *            the character.
     * @return the character converted to a string.
     */
    public static String valueOf(char value) {
        String s;
        if (value < 128) {
            s = StringFactory.newStringFromChars(value, 1, ASCII);
        } else {
            s = StringFactory.newStringFromChars(0, 1, new char[] { value });
        }
        s.hashCode = value;
        return s;
    }

    /**
     * Converts the specified double to its string representation.
     *
     * @param value
     *            the double.
     * @return the double converted to a string.
     */
    public static String valueOf(double value) {
        return Double.toString(value);
    }

    /**
     * Converts the specified float to its string representation.
     *
     * @param value
     *            the float.
     * @return the float converted to a string.
     */
    public static String valueOf(float value) {
        return Float.toString(value);
    }

    /**
     * Converts the specified integer to its string representation.
     *
     * @param value
     *            the integer.
     * @return the integer converted to a string.
     */
    public static String valueOf(int value) {
        return Integer.toString(value);
    }

    /**
     * Converts the specified long to its string representation.
     *
     * @param value
     *            the long.
     * @return the long converted to a string.
     */
    public static String valueOf(long value) {
        return Long.toString(value);
    }

    /**
     * Converts the specified object to its string representation. If the object
     * is null return the string {@code "null"}, otherwise use {@code
     * toString()} to get the string representation.
     *
     * @param value
     *            the object.
     * @return the object converted to a string, or the string {@code "null"}.
     */
    public static String valueOf(Object value) {
        return value != null ? value.toString() : "null";
    }

    /**
     * Converts the specified boolean to its string representation. When the
     * boolean is {@code true} return {@code "true"}, otherwise return {@code
     * "false"}.
     *
     * @param value
     *            the boolean.
     * @return the boolean converted to a string.
     */
    public static String valueOf(boolean value) {
        return value ? "true" : "false";
    }

    /**
     * Returns whether the characters in the StringBuffer {@code strbuf} are the
     * same as those in this string.
     *
     * @param strbuf
     *            the StringBuffer to compare this string to.
     * @return {@code true} if the characters in {@code strbuf} are identical to
     *         those in this string. If they are not, {@code false} will be
     *         returned.
     * @throws NullPointerException
     *             if {@code strbuf} is {@code null}.
     * @since 1.4
     */
    public boolean contentEquals(StringBuffer strbuf) {
        synchronized (strbuf) {
            int size = strbuf.length();
            if (count != size) {
                return false;
            }
            String s = StringFactory.newStringFromChars(0, size, strbuf.getValue());
            return regionMatches(0, s, 0, size);
        }
    }

    /**
     * Compares a {@code CharSequence} to this {@code String} to determine if
     * their contents are equal.
     *
     * @param cs
     *            the character sequence to compare to.
     * @return {@code true} if equal, otherwise {@code false}
     * @since 1.5
     */
    public boolean contentEquals(CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException("cs == null");
        }

        int len = cs.length();

        if (len != count) {
            return false;
        }

        if (len == 0 && count == 0) {
            return true; // since both are empty strings
        }

        return regionMatches(0, cs.toString(), 0, len);
    }

    /**
     * Tests whether this string matches the given {@code regularExpression}. This method returns
     * true only if the regular expression matches the <i>entire</i> input string. A common mistake is
     * to assume that this method behaves like {@link #contains}; if you want to match anywhere
     * within the input string, you need to add {@code .*} to the beginning and end of your
     * regular expression. See {@link Pattern#matches}.
     *
     * <p>If the same regular expression is to be used for multiple operations, it may be more
     * efficient to reuse a compiled {@code Pattern}.
     *
     * @throws PatternSyntaxException
     *             if the syntax of the supplied regular expression is not
     *             valid.
     * @throws NullPointerException if {@code regularExpression == null}
     * @since 1.4
     */
    public boolean matches(String regularExpression) {
        return Pattern.matches(regularExpression, this);
    }

    /**
     * Replaces all matches for {@code regularExpression} within this string with the given
     * {@code replacement}.
     * See {@link Pattern} for regular expression syntax.
     *
     * <p>If the same regular expression is to be used for multiple operations, it may be more
     * efficient to reuse a compiled {@code Pattern}.
     *
     * @throws PatternSyntaxException
     *             if the syntax of the supplied regular expression is not
     *             valid.
     * @throws NullPointerException if {@code regularExpression == null}
     * @see Pattern
     * @since 1.4
     */
    public String replaceAll(String regularExpression, String replacement) {
        return Pattern.compile(regularExpression).matcher(this).replaceAll(replacement);
    }

    /**
     * Replaces the first match for {@code regularExpression} within this string with the given
     * {@code replacement}.
     * See {@link Pattern} for regular expression syntax.
     *
     * <p>If the same regular expression is to be used for multiple operations, it may be more
     * efficient to reuse a compiled {@code Pattern}.
     *
     * @throws PatternSyntaxException
     *             if the syntax of the supplied regular expression is not
     *             valid.
     * @throws NullPointerException if {@code regularExpression == null}
     * @see Pattern
     * @since 1.4
     */
    public String replaceFirst(String regularExpression, String replacement) {
        return Pattern.compile(regularExpression).matcher(this).replaceFirst(replacement);
    }

    /**
     * Splits this string using the supplied {@code regularExpression}.
     * Equivalent to {@code split(regularExpression, 0)}.
     * See {@link Pattern#split(CharSequence, int)} for an explanation of {@code limit}.
     * See {@link Pattern} for regular expression syntax.
     *
     * <p>If the same regular expression is to be used for multiple operations, it may be more
     * efficient to reuse a compiled {@code Pattern}.
     *
     * @throws NullPointerException if {@code regularExpression ==  null}
     * @throws PatternSyntaxException
     *             if the syntax of the supplied regular expression is not
     *             valid.
     * @see Pattern
     * @since 1.4
     */
    public String[] split(String regularExpression) {
        return split(regularExpression, 0);
    }

    /**
     * Splits this string using the supplied {@code regularExpression}.
     * See {@link Pattern#split(CharSequence, int)} for an explanation of {@code limit}.
     * See {@link Pattern} for regular expression syntax.
     *
     * <p>If the same regular expression is to be used for multiple operations, it may be more
     * efficient to reuse a compiled {@code Pattern}.
     *
     * @throws NullPointerException if {@code regularExpression ==  null}
     * @throws PatternSyntaxException
     *             if the syntax of the supplied regular expression is not
     *             valid.
     * @since 1.4
     */
    public String[] split(String regularExpression, int limit) {
        String[] result = java.util.regex.Splitter.fastSplit(regularExpression, this, limit);
        return result != null ? result : Pattern.compile(regularExpression).split(this, limit);
    }

    /**
     * Has the same result as the substring function, but is present so that
     * string may implement the CharSequence interface.
     *
     * @param start
     *            the offset the first character.
     * @param end
     *            the offset of one past the last character to include.
     * @return the subsequence requested.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end < 0}, {@code start > end} or
     *             {@code end > length()}.
     * @see java.lang.CharSequence#subSequence(int, int)
     * @since 1.4
     */
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    /**
     * Returns the Unicode code point at the given {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= length()}
     * @see Character#codePointAt(char[], int, int)
     * @since 1.5
     */
    public int codePointAt(int index) {
        if (index < 0 || index >= count) {
            throw indexAndLength(index);
        }
        return Character.codePointAt(this, index);
    }

    /**
     * Returns the Unicode code point that precedes the given {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 1 || index > length()}
     * @see Character#codePointBefore(char[], int, int)
     * @since 1.5
     */
    public int codePointBefore(int index) {
        if (index < 1 || index > count) {
            throw indexAndLength(index);
        }
        return Character.codePointBefore(this, index);
    }

    /**
     * Calculates the number of Unicode code points between {@code start}
     * and {@code end}.
     *
     * @param start
     *            the inclusive beginning index of the subsequence.
     * @param end
     *            the exclusive end index of the subsequence.
     * @return the number of Unicode code points in the subsequence.
     * @throws IndexOutOfBoundsException
     *         if {@code start < 0 || end > length() || start > end}
     * @see Character#codePointCount(CharSequence, int, int)
     * @since 1.5
     */
    public int codePointCount(int start, int end) {
        if (start < 0 || end > count || start > end) {
            throw startEndAndLength(start, end);
        }
        return Character.codePointCount(this, start, end);
    }

    /**
     * Determines if this {@code String} contains the sequence of characters in
     * the {@code CharSequence} passed.
     *
     * @param cs
     *            the character sequence to search for.
     * @return {@code true} if the sequence of characters are contained in this
     *         string, otherwise {@code false}.
     * @since 1.5
     */
    public boolean contains(CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException("cs == null");
        }
        return indexOf(cs.toString()) >= 0;
    }

    /**
     * Returns the index within this object that is offset from {@code index} by
     * {@code codePointOffset} code points.
     *
     * @param index
     *            the index within this object to calculate the offset from.
     * @param codePointOffset
     *            the number of code points to count.
     * @return the index within this object that is the offset.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than {@code length()}
     *             or if there aren't enough code points before or after {@code
     *             index} to match {@code codePointOffset}.
     * @since 1.5
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return Character.offsetByCodePoints(this, index, codePointOffset);
    }

    /**
     * Returns a localized formatted string, using the supplied format and arguments,
     * using the user's default locale.
     *
     * <p>If you're formatting a string other than for human
     * consumption, you should use the {@code format(Locale, String, Object...)}
     * overload and supply {@code Locale.US}. See
     * "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return the formatted string.
     * @throws NullPointerException if {@code format == null}
     * @throws java.util.IllegalFormatException
     *             if the format is invalid.
     * @since 1.5
     */
    public static String format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    /**
     * Returns a formatted string, using the supplied format and arguments,
     * localized to the given locale.
     *
     * @param locale
     *            the locale to apply; {@code null} value means no localization.
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return the formatted string.
     * @throws NullPointerException if {@code format == null}
     * @throws java.util.IllegalFormatException
     *             if the format is invalid.
     * @since 1.5
     */
    public static String format(Locale locale, String format, Object... args) {
        if (format == null) {
            throw new NullPointerException("format == null");
        }
        int bufferSize = format.length() + (args == null ? 0 : args.length * 10);
        Formatter f = new Formatter(new StringBuilder(bufferSize), locale);
        return f.format(format, args).toString();
    }

    /*
     * An implementation of a String.indexOf that is supposed to perform
     * substantially better than the default algorithm if the "needle" (the
     * subString being searched for) is a constant string.
     *
     * For example, a JIT, upon encountering a call to String.indexOf(String),
     * where the needle is a constant string, may compute the values cache, md2
     * and lastChar, and change the call to the following method.
     */
    @FindBugsSuppressWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private static int indexOf(String haystackString, String needleString,
            int cache, int md2, char lastChar) {
        int haystackLength = haystackString.count;
        int needleLength = needleString.count;
        int needleLengthMinus1 = needleLength - 1;
        outer_loop: for (int i = needleLengthMinus1; i < haystackLength;) {
            if (lastChar == haystackString.charAt(i)) {
                for (int j = 0; j < needleLengthMinus1; ++j) {
                    if (needleString.charAt(j) !=
                            haystackString.charAt(i + j - needleLengthMinus1)) {
                        int skip = 1;
                        if ((cache & (1 << haystackString.charAt(i))) == 0) {
                            skip += j;
                        }
                        i += Math.max(md2, skip);
                        continue outer_loop;
                    }
                }
                return i - needleLengthMinus1;
            }

            if ((cache & (1 << haystackString.charAt(i))) == 0) {
                i += needleLengthMinus1;
            }
            i++;
        }
        return -1;
    }
}
