/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import junit.framework.TestCase;

public final class BigDecimalTest extends TestCase {

    public void testGetPrecision() {
        assertPrecision(1, "0");
        assertPrecision(1, "0.9");
        assertPrecision(16, "0.9999999999999999");
        assertPrecision(16, "9999999999999999");
        assertPrecision(19, "1000000000000000000");
        assertPrecision(19, "1000000000000000001");
        assertPrecision(19, "-1000000000000000001");
        assertPrecision(19, "-1000000000000000000");

        String tenNines = "9999999999";
        String fiftyNines = tenNines + tenNines + tenNines + tenNines + tenNines;
        assertPrecision(10, "0." + tenNines);
        assertPrecision(50, "0." + fiftyNines);
        assertPrecision(250, "0." + fiftyNines + fiftyNines + fiftyNines + fiftyNines + fiftyNines);
        assertPrecision(10, tenNines);
        assertPrecision(50, fiftyNines);
        assertPrecision(250, fiftyNines + fiftyNines + fiftyNines + fiftyNines + fiftyNines);

        // test these special cases because we know precision() uses longs internally
        String maxLong = Long.toString(Long.MAX_VALUE);
        assertPrecision(maxLong.length(), maxLong);
        String minLong = Long.toString(Long.MIN_VALUE);
        assertPrecision(minLong.length() - 1, minLong);
    }

    private void assertPrecision(int expectedPrecision, String value) {
        BigDecimal parsed = new BigDecimal(value);
        assertEquals("Unexpected precision for parsed value " + value,
                expectedPrecision, parsed.precision());

        BigDecimal computed = parsed.divide(BigDecimal.ONE);
        assertEquals("Unexpected precision for computed value " + value,
                expectedPrecision, computed.precision());
    }

    public void testRound() {
        BigDecimal bigDecimal = new BigDecimal("0.999999999999999");
        BigDecimal rounded = bigDecimal.round(new MathContext(2, RoundingMode.FLOOR));
        assertEquals("0.99", rounded.toString());
    }

    // https://code.google.com/p/android/issues/detail?id=43480
    public void testPrecisionFromString() {
      BigDecimal a = new BigDecimal("-0.011111111111111111111");
      BigDecimal b = a.multiply(BigDecimal.ONE);

      assertEquals("-0.011111111111111111111", a.toString());
      assertEquals("-0.011111111111111111111", b.toString());

      assertEquals(20, a.precision());
      assertEquals(20, b.precision());

      assertEquals(21, a.scale());
      assertEquals(21, b.scale());

      assertEquals("-11111111111111111111", a.unscaledValue().toString());
      assertEquals("-11111111111111111111", b.unscaledValue().toString());

      assertEquals(a, b);
      assertEquals(b, a);

      assertEquals(0, a.subtract(b).signum());
      assertEquals(0, a.compareTo(b));
    }

    // https://code.google.com/p/android/issues/detail?id=54580
    public void test54580() {
        BigDecimal a = new BigDecimal("1.200002");
        assertEquals("1.200002", a.toPlainString());
        assertEquals("1.20", a.abs(new MathContext(3,RoundingMode.HALF_UP)).toPlainString());
        assertEquals("1.200002", a.toPlainString());
    }

    // https://code.google.com/p/android/issues/detail?id=191227
    public void test191227() {
        BigDecimal zero = BigDecimal.ZERO;
        zero = zero.setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal other = BigDecimal.valueOf(999999998000000001.00);
        other = other.setScale(2, RoundingMode.HALF_EVEN);

        assertFalse(zero.equals(other));
        assertFalse(other.equals(zero));
    }

    public void testDivideRounding() {
        BigDecimal n = BigDecimal.ONE;
        BigDecimal d = new BigDecimal(Long.MIN_VALUE);

        assertEquals("DOWN", new BigDecimal("0"), n.divide(d, 0, RoundingMode.DOWN));
        assertEquals("UP", new BigDecimal("-1"), n.divide(d, 0, RoundingMode.UP));
        assertEquals("FLOOR", new BigDecimal("-1"), n.divide(d, 0, RoundingMode.FLOOR));
        assertEquals("CEILING", new BigDecimal("0"), n.divide(d, 0, RoundingMode.CEILING));
        assertEquals("HALF_EVEN", new BigDecimal("0"), n.divide(d, 0, RoundingMode.HALF_EVEN));
        assertEquals("HALF_UP", new BigDecimal("0"), n.divide(d, 0, RoundingMode.HALF_UP));
        assertEquals("HALF_DOWN", new BigDecimal("0"), n.divide(d, 0, RoundingMode.HALF_DOWN));
    }

    private static void checkDivide(String expected, long n, long d, int scale, RoundingMode rm) {
        assertEquals(String.format(Locale.US, "%d/%d [%d, %s]", n, d, scale, rm.name()),
                new BigDecimal(expected),
                new BigDecimal(n).divide(new BigDecimal(d), scale, rm));
    }

    public void testDivide() {
        checkDivide("1", Long.MAX_VALUE, Long.MAX_VALUE / 2 + 1, 0, RoundingMode.DOWN);
        checkDivide("2", Long.MAX_VALUE, Long.MAX_VALUE / 2, 0, RoundingMode.DOWN);
        checkDivide("0.50", Long.MAX_VALUE / 2, Long.MAX_VALUE, 2, RoundingMode.HALF_UP);
        checkDivide("0.50", Long.MIN_VALUE / 2, Long.MIN_VALUE, 2, RoundingMode.HALF_UP);
        checkDivide("0.5000", Long.MIN_VALUE / 2, Long.MIN_VALUE, 4, RoundingMode.HALF_UP);
        // (-2^62 + 1) / (-2^63) = (2^62 - 1) / 2^63 = 0.5 - 2^-63
        checkDivide("0", Long.MIN_VALUE / 2 + 1, Long.MIN_VALUE, 0, RoundingMode.HALF_UP);
        checkDivide("1", Long.MIN_VALUE / 2, Long.MIN_VALUE, 0, RoundingMode.HALF_UP);
        checkDivide("0", Long.MIN_VALUE / 2, Long.MIN_VALUE, 0, RoundingMode.HALF_DOWN);
        // (-2^62 - 1) / (-2^63) = (2^62 + 1) / 2^63 = 0.5 + 2^-63
        checkDivide("1", Long.MIN_VALUE / 2 - 1, Long.MIN_VALUE, 0, RoundingMode.HALF_DOWN);
    }
}
