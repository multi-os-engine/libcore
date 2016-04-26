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
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
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

    public void testPrecisionFromString_simplePowersOfTen() {
        assertEquals(new BigDecimal(BigInteger.valueOf(-10), 1), new BigDecimal("-1.0"));
        assertEquals(new BigDecimal(BigInteger.valueOf(-1), 1), new BigDecimal("-0.1"));
        assertEquals(new BigDecimal(BigInteger.valueOf(-1), -1), new BigDecimal("-1E+1"));

        assertEquals(new BigDecimal(BigInteger.valueOf(10), 1), new BigDecimal("1.0"));
        assertEquals(new BigDecimal(BigInteger.valueOf(1), 0), new BigDecimal("1"));
        assertFalse(new BigDecimal("1.0").equals(new BigDecimal("1")));
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

    /**
     * Tests that Long.MIN_VALUE / -1 doesn't overflow back to Long.MIN_VALUE,
     * like it would in long arithmetic.
     */
    // https://code.google.com/p/android/issues/detail?id=196555
    public void testDivideAvoids64bitOverflow() throws Exception {
        BigDecimal minLong = new BigDecimal("-9223372036854775808");
        assertEquals("9223372036854775808/(-1)",
                new BigDecimal("9223372036854775808"),
                minLong.divide(new BigDecimal("-1"), /* scale = */ 0, RoundingMode.UNNECESSARY));

        assertEquals("922337203685477580.8/(-0.1)",
                new BigDecimal("9223372036854775808"),
                new BigDecimal("-922337203685477580.8")
                        .divide(new BigDecimal("-0.1"), /* scale = */ 0, RoundingMode.UNNECESSARY));

        assertEquals("92233720368547758080/(-1E+1)",
                new BigDecimal("9223372036854775808"),
                new BigDecimal("-92233720368547758080")
                        .divide(new BigDecimal("-1E+1"), /* scale = */ 0, RoundingMode.UNNECESSARY));

        assertEquals("9223372036854775808/(-10) with one decimal of precision",
                new BigDecimal("922337203685477580.8"),
                minLong.divide(new BigDecimal("-1E+1"), /* scale = */ 1, RoundingMode.UNNECESSARY));

        // cases that request adjustment of the result scale, i.e. (diffScale != 0)
        // i.e. result scale != (numerator.scale - divisor.scale)
        assertEquals("9223372036854775808/(-1) with one decimal of precision",//
                new BigDecimal("9223372036854775808.0"),
                minLong.divide(new BigDecimal("-1"), /* scale = */ 1, RoundingMode.UNNECESSARY));

        assertEquals("9223372036854775808/(-1.0)",//
                new BigDecimal("9223372036854775808"),
                minLong.divide(new BigDecimal("-1.0"), /* scale = */ 0, RoundingMode.UNNECESSARY));

        assertEquals("4611686018427387904/(-5E-1)",//
                new BigDecimal("9223372036854775808"),
                new BigDecimal("-4611686018427387904").divide(
                        new BigDecimal("-5E-1"), 0, RoundingMode.UNNECESSARY));

        assertEquals("9223372036854775808/(-1.0) with one decimal of precision",//
                new BigDecimal("9223372036854775808.0"),
                minLong.divide(new BigDecimal("-1.0"), /* scale = */ 1, RoundingMode.UNNECESSARY));
    }

}
