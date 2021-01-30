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

import java.math.*;

/**
 * The wrapper for the primitive type {@code long}.
 * <p>
 * Implementation note: The "bit twiddling" methods in this class use techniques
 * described in <a href="http://www.hackersdelight.org/">Henry S. Warren,
 * Jr.'s Hacker's Delight, (Addison Wesley, 2002)</a> and <a href=
 * "http://graphics.stanford.edu/~seander/bithacks.html">Sean Anderson's
 * Bit Twiddling Hacks.</a>
 *
 * @see java.lang.Integer
 * @since 1.0
 */
@FindBugsSuppressWarnings("DM_NUMBER_CTOR")
public final class Long extends Number implements Comparable<Long> {

    private static final long serialVersionUID = 4290774380558885855L;

    /**
     * The value which the receiver represents.
     */
    private final long value;

    /**
     * Constant for the maximum {@code long} value, 2<sup>63</sup>-1.
     */
    public static final long MAX_VALUE = 0x7FFFFFFFFFFFFFFFL;

    /**
     * Constant for the minimum {@code long} value, -2<sup>63</sup>.
     */
    public static final long MIN_VALUE = 0x8000000000000000L;

    /**
     * The {@link Class} object that represents the primitive type {@code long}.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Long> TYPE
            = (Class<Long>) long[].class.getComponentType();
    // Note: Long.TYPE can't be set to "long.class", since *that* is
    // defined to be "java.lang.Long.TYPE";

    /**
     * Constant for the number of bits needed to represent a {@code long} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * Constructs a new {@code Long} with the specified primitive long value.
     *
     * @param value
     *            the primitive long value to store in the new instance.
     */
    public Long(long value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Long} from the specified string.
     *
     * @param string
     *            the string representation of a long value.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a long value.
     * @see #parseLong(String)
     */
    public Long(String string) throws NumberFormatException {
        this(parseLong(string));
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Compares this object to the specified long object to determine their
     * relative order.
     *
     * @param object
     *            the long object to compare this object to.
     * @return a negative value if the value of this long is less than the value
     *         of {@code object}; 0 if the value of this long and the value of
     *         {@code object} are equal; a positive value if the value of this
     *         long is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Long object) {
        return compare(value, object.value);
    }

    private static NumberFormatException invalidLong(String s) {
        throw new NumberFormatException("Invalid long: \"" + s + "\"");
    }

    /**
     * Parses the specified string and returns a {@code Long} instance if the
     * string can be decoded into a long value. The string may be an optional
     * optional sign character ("-" or "+") followed by a hexadecimal ("0x..."
     * or "#..."), octal ("0..."), or decimal ("...") representation of a long.
     *
     * @param string
     *            a string representation of a long value.
     * @return a {@code Long} containing the value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a long value.
     */
    public static Long decode(String string) throws NumberFormatException {
        int length = string.length();
        if (length == 0) {
            throw invalidLong(string);
        }

        int i = 0;
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative || firstDigit == '+') {
            if (length == 1) {
                throw invalidLong(string);
            }
            firstDigit = string.charAt(++i);
        }

        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0L);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (i == length) {
                    throw invalidLong(string);
                }
                i++;
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (i == length) {
                throw invalidLong(string);
            }
            i++;
            base = 16;
        }

        long result = parse(string, i, base, negative);
        return valueOf(result);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must be an instance of
     * {@code Long} and have the same long value as this object.
     *
     * @param o
     *            the object to compare this long with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Long}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Long) && (((Long) o).value == value);
    }

    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the {@code Long} value of the system property identified by
     * {@code string}. Returns {@code null} if {@code string} is {@code null}
     * or empty, if the property can not be found or if its value can not be
     * parsed as a long.
     *
     * @param string
     *            the name of the requested system property.
     * @return the requested property's value as a {@code Long} or {@code null}.
     */
    public static Long getLong(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return null;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Returns the {@code Long} value of the system property identified by
     * {@code string}. Returns the specified default value if {@code string} is
     * {@code null} or empty, if the property can not be found or if its value
     * can not be parsed as a long.
     *
     * @param string
     *            the name of the requested system property.
     * @param defaultValue
     *            the default value that is returned if there is no long system
     *            property with the requested name.
     * @return the requested property's value as a {@code Long} or the default
     *         value.
     */
    public static Long getLong(String string, long defaultValue) {
        if (string == null || string.length() == 0) {
            return valueOf(defaultValue);
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return valueOf(defaultValue);
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return valueOf(defaultValue);
        }
    }

    /**
     * Returns the {@code Long} value of the system property identified by
     * {@code string}. Returns the specified default value if {@code string} is
     * {@code null} or empty, if the property can not be found or if its value
     * can not be parsed as a long.
     *
     * @param string
     *            the name of the requested system property.
     * @param defaultValue
     *            the default value that is returned if there is no long system
     *            property with the requested name.
     * @return the requested property's value as a {@code Long} or the default
     *         value.
     */
    public static Long getLong(String string, Long defaultValue) {
        if (string == null || string.length() == 0) {
            return defaultValue;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return defaultValue;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Gets the primitive value of this long.
     *
     * @return this object's primitive value.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Parses the specified string as a signed decimal long value. The ASCII
     * characters \u002d ('-') and \u002b ('+') are recognized as the minus and
     * plus signs.
     *
     * @param string
     *            the string representation of a long value.
     * @return the primitive long value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a long value.
     */
    public static long parseLong(String string) throws NumberFormatException {
        return parseLong(string, 10);
    }

    /**
     * Parses the specified string as a signed long value using the specified
     * radix. The ASCII characters \u002d ('-') and \u002b ('+') are recognized
     * as the minus and plus signs.
     *
     * @param string
     *            the string representation of a long value.
     * @param radix
     *            the radix to use when parsing.
     * @return the primitive long value represented by {@code string} using
     *         {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a long value, or
     *             {@code radix < Character.MIN_RADIX ||
     *             radix > Character.MAX_RADIX}.
     */
    public static long parseLong(String string, int radix) throws NumberFormatException {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new NumberFormatException("Invalid radix: " + radix);
        }
        if (string == null || string.isEmpty()) {
            throw invalidLong(string);
        }
        char firstChar = string.charAt(0);
        int firstDigitIndex = (firstChar == '-' || firstChar == '+') ? 1 : 0;
        if (firstDigitIndex == string.length()) {
            throw invalidLong(string);
        }

        return parse(string, firstDigitIndex, radix, firstChar == '-');
    }

    private static long parse(String string, int offset, int radix, boolean negative) {
        long max = Long.MIN_VALUE / radix;
        long result = 0;
        int length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                throw invalidLong(string);
            }
            if (max > result) {
                throw invalidLong(string);
            }
            long next = result * radix - digit;
            if (next > result) {
                throw invalidLong(string);
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw invalidLong(string);
            }
        }
        return result;
    }

    /**
     * Equivalent to {@code parsePositiveLong(string, 10)}.
     *
     * @see #parsePositiveLong(String, int)
     *
     * @hide
     */
    public static long parsePositiveLong(String string) throws NumberFormatException {
        return parsePositiveLong(string, 10);
    }

    /**
     * Parses the specified string as a positive long value using the
     * specified radix. 0 is considered a positive long.
     * <p>
     * This method behaves the same as {@link #parseLong(String, int)} except
     * that it disallows leading '+' and '-' characters. See that method for
     * error conditions.
     *
     * @see #parseLong(String, int)
     *
     * @hide
     */
    public static long parsePositiveLong(String string, int radix) throws NumberFormatException {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new NumberFormatException("Invalid radix: " + radix);
        }
        if (string == null || string.length() == 0) {
            throw invalidLong(string);
        }
        return parse(string, 0, radix, false);
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    /**
     * Converts the specified long value into its binary string representation.
     * The returned string is a concatenation of '0' and '1' characters.
     *
     * @param v
     *            the long value to convert.
     * @return the binary string representation of {@code v}.
     */
    public static String toBinaryString(long v) {
        return IntegralToString.longToBinaryString(v);
    }

    /**
     * Converts the specified long value into its hexadecimal string
     * representation. The returned string is a concatenation of characters from
     * '0' to '9' and 'a' to 'f'.
     *
     * @param v
     *            the long value to convert.
     * @return the hexadecimal string representation of {@code l}.
     */
    public static String toHexString(long v) {
        return IntegralToString.longToHexString(v);
    }

    /**
     * Converts the specified long value into its octal string representation.
     * The returned string is a concatenation of characters from '0' to '7'.
     *
     * @param v
     *            the long value to convert.
     * @return the octal string representation of {@code l}.
     */
    public static String toOctalString(long v) {
        return IntegralToString.longToOctalString(v);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    /**
     * Converts the specified long value into its decimal string representation.
     * The returned string is a concatenation of a minus sign if the number is
     * negative and characters from '0' to '9'.
     *
     * @param n
     *            the long to convert.
     * @return the decimal string representation of {@code l}.
     */
    public static String toString(long n) {
        return IntegralToString.longToString(n);
    }

    /**
     * Converts the specified signed long value into a string representation based on
     * the specified radix. The returned string is a concatenation of a minus
     * sign if the number is negative and characters from '0' to '9' and 'a' to
     * 'z', depending on the radix. If {@code radix} is not in the interval
     * defined by {@code Character.MIN_RADIX} and {@code Character.MAX_RADIX}
     * then 10 is used as the base for the conversion.
     *
     * <p>This method treats its argument as signed. If you want to convert an
     * unsigned value to one of the common non-decimal bases, you may find
     * {@link #toBinaryString}, {@code #toHexString}, or {@link #toOctalString}
     * more convenient.
     *
     * @param v
     *            the signed long to convert.
     * @param radix
     *            the base to use for the conversion.
     * @return the string representation of {@code v}.
     */
    public static String toString(long v, int radix) {
        return IntegralToString.longToString(v, radix);
    }

    /**
     * Parses the specified string as a signed decimal long value.
     *
     * @param string
     *            the string representation of a long value.
     * @return a {@code Long} instance containing the long value represented by
     *         {@code string}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a long value.
     * @see #parseLong(String)
     */
    public static Long valueOf(String string) throws NumberFormatException {
        return valueOf(parseLong(string));
    }

    /**
     * Parses the specified string as a signed long value using the specified
     * radix.
     *
     * @param string
     *            the string representation of a long value.
     * @param radix
     *            the radix to use when parsing.
     * @return a {@code Long} instance containing the long value represented by
     *         {@code string} using {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a long value, or
     *             {@code radix < Character.MIN_RADIX ||
     *             radix > Character.MAX_RADIX}.
     * @see #parseLong(String, int)
     */
    public static Long valueOf(String string, int radix) throws NumberFormatException {
        return valueOf(parseLong(string, radix));
    }

    /**
     * Determines the highest (leftmost) bit of the specified long value that is
     * 1 and returns the bit mask value for that bit. This is also referred to
     * as the Most Significant 1 Bit. Returns zero if the specified long is
     * zero.
     *
     * @param v
     *            the long to examine.
     * @return the bit mask indicating the highest 1 bit in {@code v}.
     * @since 1.5
     */
    public static long highestOneBit(long v) {
        // Hacker's Delight, Figure 3-1
        v |= (v >> 1);
        v |= (v >> 2);
        v |= (v >> 4);
        v |= (v >> 8);
        v |= (v >> 16);
        v |= (v >> 32);
        return v - (v >>> 1);
    }

    /**
     * Determines the lowest (rightmost) bit of the specified long value that is
     * 1 and returns the bit mask value for that bit. This is also referred to
     * as the Least Significant 1 Bit. Returns zero if the specified long is
     * zero.
     *
     * @param v
     *            the long to examine.
     * @return the bit mask indicating the lowest 1 bit in {@code v}.
     * @since 1.5
     */
    public static long lowestOneBit(long v) {
        return v & -v;
    }

    /**
     * Determines the number of leading zeros in the specified long value prior
     * to the {@link #highestOneBit(long) highest one bit}.
     *
     * @param v
     *            the long to examine.
     * @return the number of leading zeros in {@code v}.
     * @since 1.5
     */
    public static int numberOfLeadingZeros(long v) {
        // After Hacker's Delight, Figure 5-6
        if (v < 0) {
            return 0;
        }
        if (v == 0) {
            return 64;
        }
        // On a 64-bit VM, the two previous tests should probably be replaced by
        // if (v <= 0) return ((int) (~v >> 57)) & 64;

        int n = 1;
        int i = (int) (v >>> 32);
        if (i == 0) {
            n +=  32;
            i = (int) v;
        }
        if (i >>> 16 == 0) {
            n +=  16;
            i <<= 16;
        }
        if (i >>> 24 == 0) {
            n +=  8;
            i <<= 8;
        }
        if (i >>> 28 == 0) {
            n +=  4;
            i <<= 4;
        }
        if (i >>> 30 == 0) {
            n +=  2;
            i <<= 2;
        }
        return n - (i >>> 31);
    }

    /**
     * Determines the number of trailing zeros in the specified long value after
     * the {@link #lowestOneBit(long) lowest one bit}.
     *
     * @param v
     *            the long to examine.
     * @return the number of trailing zeros in {@code v}.
     * @since 1.5
     */
    public static int numberOfTrailingZeros(long v) {
        int low = (int) v;
        return low !=0 ? Integer.numberOfTrailingZeros(low)
                       : 32 + Integer.numberOfTrailingZeros((int) (v >>> 32));
    }

    /**
     * Counts the number of 1 bits in the specified long value; this is also
     * referred to as population count.
     *
     * @param v
     *            the long to examine.
     * @return the number of 1 bits in {@code v}.
     * @since 1.5
     */
    public static int bitCount(long v) {
        // Combines techniques from several sources
        v -=  (v >>> 1) & 0x5555555555555555L;
        v = (v & 0x3333333333333333L) + ((v >>> 2) & 0x3333333333333333L);
        int i =  ((int)(v >>> 32)) + (int) v;
        i = (i & 0x0F0F0F0F) + ((i >>> 4) & 0x0F0F0F0F);
        i += i >>> 8;
        i += i >>> 16;
        return i  & 0x0000007F;
    }

    /*
     * On a modern 64-bit processor with a fast hardware multiply, this is
     * much faster (assuming you're running a 64-bit VM):
     *
     * // http://chessprogramming.wikispaces.com/Population+Count
     * int bitCount (long x) {
     *     x -=  (x >>> 1) & 0x5555555555555555L;
     *     x = (x & 0x3333333333333333L) + ((x >>> 2) & 0x3333333333333333L);
     *     x = (x + (x >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
     *     x = (x * 0x0101010101010101L) >>> 56;
     *     return (int) x;
     * }
     *
     * Really modern processors (e.g., Nehalem, K-10) have hardware popcount
     * instructions.
     */

    /**
     * Rotates the bits of the specified long value to the left by the specified
     * number of bits.
     *
     * @param v
     *            the long value to rotate left.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static long rotateLeft(long v, int distance) {
        // Shift distances are mod 64 (JLS3 15.19), so we needn't mask -distance
        return (v << distance) | (v >>> -distance);
    }

    /**
     * Rotates the bits of the specified long value to the right by the
     * specified number of bits.
     *
     * @param v
     *            the long value to rotate right.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static long rotateRight(long v, int distance) {
        // Shift distances are mod 64 (JLS3 15.19), so we needn't mask -distance
        return (v >>> distance) | (v << -distance);
    }

    /**
     * Reverses the order of the bytes of the specified long value.
     *
     * @param v
     *            the long value for which to reverse the byte order.
     * @return the reversed value.
     * @since 1.5
     */
    public static long reverseBytes(long v) {
        // Hacker's Delight 7-1, with minor tweak from Veldmeijer
        // http://graphics.stanford.edu/~seander/bithacks.html
        v = ((v >>> 8) & 0x00FF00FF00FF00FFL) | ((v & 0x00FF00FF00FF00FFL) << 8);
        v = ((v >>>16) & 0x0000FFFF0000FFFFL) | ((v & 0x0000FFFF0000FFFFL) <<16);
        return ((v >>>32)                   ) | ((v                      ) <<32);
    }

    /**
     * Reverses the order of the bits of the specified long value.
     *
     * @param v
     *            the long value for which to reverse the bit order.
     * @return the reversed value.
     * @since 1.5
     */
    public static long reverse(long v) {
        // Hacker's Delight 7-1, with minor tweak from Veldmeijer
        // http://graphics.stanford.edu/~seander/bithacks.html
        v = ((v >>> 1) & 0x5555555555555555L) | ((v & 0x5555555555555555L) << 1);
        v = ((v >>> 2) & 0x3333333333333333L) | ((v & 0x3333333333333333L) << 2);
        v = ((v >>> 4) & 0x0F0F0F0F0F0F0F0FL) | ((v & 0x0F0F0F0F0F0F0F0FL) << 4);
        v = ((v >>> 8) & 0x00FF00FF00FF00FFL) | ((v & 0x00FF00FF00FF00FFL) << 8);
        v = ((v >>>16) & 0x0000FFFF0000FFFFL) | ((v & 0x0000FFFF0000FFFFL) <<16);
        return ((v >>>32)                   ) | ((v                      ) <<32);
    }

    /**
     * Returns the value of the {@code signum} function for the specified long
     * value.
     *
     * @param v
     *            the long value to check.
     * @return -1 if {@code v} is negative, 1 if {@code v} is positive, 0 if
     *         {@code v} is zero.
     * @since 1.5
     */
    public static int signum(long v) {
        return v < 0 ? -1 : (v == 0 ? 0 : 1);
    }

    /**
     * Returns a {@code Long} instance for the specified long value.
     * <p>
     * If it is not necessary to get a new {@code Long} instance, it is
     * recommended to use this method instead of the constructor, since it
     * maintains a cache of instances which may result in better performance.
     *
     * @param v
     *            the long value to store in the instance.
     * @return a {@code Long} instance containing {@code v}.
     * @since 1.5
     */
    public static Long valueOf(long v) {
        return  v >= 128 || v < -128 ? new Long(v) : SMALL_VALUES[((int) v) + 128];
    }

    /**
     * A cache of instances used by {@link Long#valueOf(long)} and auto-boxing.
     */
    private static final Long[] SMALL_VALUES = new Long[256];

    static {
        for (int i = -128; i < 128; i++) {
            SMALL_VALUES[i + 128] = new Long(i);
        }
    }

    // Sugar starts here

    /**
     * Return a BigInteger equal to the unsigned value of the
     * argument.
     */
    private static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0L)
            return BigInteger.valueOf(i);
        else {
            int upper = (int) (i >>> 32);
            int lower = (int) i;

            // return (upper << 32) + lower
            return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }

    /**
     * Returns a hash code for a {@code long} value; compatible with
     * {@code Long.hashCode()}.
     *
     * @param value the value to hash
     * @return a hash code value for a {@code long} value.
     * @since 1.8
     */
    public static int hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }

    /**
     * Compares two {@code long} values numerically.
     * The value returned is identical to what would be returned by:
     * <pre>
     *    Long.valueOf(x).compareTo(Long.valueOf(y))
     * </pre>
     *
     * @param  x the first {@code long} to compare
     * @param  y the second {@code long} to compare
     * @return the value {@code 0} if {@code x == y};
     *         a value less than {@code 0} if {@code x < y}; and
     *         a value greater than {@code 0} if {@code x > y}
     * @since 1.7
     */
    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * Compares two {@code long} values numerically treating the values
     * as unsigned.
     *
     * @param  x the first {@code long} to compare
     * @param  y the second {@code long} to compare
     * @return the value {@code 0} if {@code x == y}; a value less
     *         than {@code 0} if {@code x < y} as unsigned values; and
     *         a value greater than {@code 0} if {@code x > y} as
     *         unsigned values
     * @since 1.8
     */
    public static int compareUnsigned(long x, long y) {
        return compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
    }

    /**
     * Returns the unsigned quotient of dividing the first argument by
     * the second where each argument and the result is interpreted as
     * an unsigned value.
     *
     * <p>Note that in two's complement arithmetic, the three other
     * basic arithmetic operations of add, subtract, and multiply are
     * bit-wise identical if the two operands are regarded as both
     * being signed or both being unsigned.  Therefore separate {@code
     * addUnsigned}, etc. methods are not provided.
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned quotient of the first argument divided by
     * the second argument
     * @see #remainderUnsigned
     * @since 1.8
     */
    public static long divideUnsigned(long dividend, long divisor) {
        if (divisor < 0L) { // signed comparison
            // Answer must be 0 or 1 depending on relative magnitude
            // of dividend and divisor.
            return (compareUnsigned(dividend, divisor)) < 0 ? 0L :1L;
        }

        if (dividend > 0) //  Both inputs non-negative
            return dividend/divisor;
        else {
            /*
             * For simple code, leveraging BigInteger.  Longer and faster
             * code written directly in terms of operations on longs is
             * possible; see "Hacker's Delight" for divide and remainder
             * algorithms.
             */
            return toUnsignedBigInteger(dividend).
                divide(toUnsignedBigInteger(divisor)).longValue();
        }
    }

    /**
     * Returns the unsigned remainder from dividing the first argument
     * by the second where each argument and the result is interpreted
     * as an unsigned value.
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned remainder of the first argument divided by
     * the second argument
     * @see #divideUnsigned
     * @since 1.8
     */
    public static long remainderUnsigned(long dividend, long divisor) {
        if (dividend > 0 && divisor > 0) { // signed comparisons
            return dividend % divisor;
        } else {
            if (compareUnsigned(dividend, divisor) < 0) // Avoid explicit check for 0 divisor
                return dividend;
            else
                return toUnsignedBigInteger(dividend).
                    remainder(toUnsignedBigInteger(divisor)).longValue();
        }
    }

    /**
     * Adds two {@code long} values together as per the + operator.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the sum of {@code a} and {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long sum(long a, long b) {
        return a + b;
    }

    /**
     * Returns the greater of two {@code long} values
     * as if by calling {@link Math#max(long, long) Math.max}.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the greater of {@code a} and {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    /**
     * Returns the smaller of two {@code long} values
     * as if by calling {@link Math#min(long, long) Math.min}.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the smaller of {@code a} and {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long min(long a, long b) {
        return Math.min(a, b);
    }
}
