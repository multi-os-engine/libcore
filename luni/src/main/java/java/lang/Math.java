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

import java.math.BigDecimal;
import java.util.Random;

/**
 * Class Math provides basic math constants and operations such as trigonometric
 * functions, hyperbolic functions, exponential, logarithms, etc.
 */
public final class Math {
    /**
     * The double value closest to e, the base of the natural logarithm.
     */
    public static final double E = 2.718281828459045;

    /**
     * The double value closest to pi, the ratio of a circle's circumference to
     * its diameter.
     */
    public static final double PI = 3.141592653589793;

    private static class NoImagePreloadHolder {
        private static final Random INSTANCE = new Random();
    }

    /**
     * Prevents this class from being instantiated.
     */
    private Math() {
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code abs(-0.0) = +0.0}</li>
     * <li>{@code abs(+infinity) = +infinity}</li>
     * <li>{@code abs(-infinity) = +infinity}</li>
     * <li>{@code abs(NaN) = NaN}</li>
     * </ul>
     */
    public static double abs(double d) {
        return Double.longBitsToDouble(Double.doubleToRawLongBits(d) & 0x7fffffffffffffffL);
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code abs(-0.0) = +0.0}</li>
     * <li>{@code abs(+infinity) = +infinity}</li>
     * <li>{@code abs(-infinity) = +infinity}</li>
     * <li>{@code abs(NaN) = NaN}</li>
     * </ul>
     */
    public static float abs(float f) {
        return Float.intBitsToFloat(Float.floatToRawIntBits(f) & 0x7fffffff);
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * If the argument is {@code Integer.MIN_VALUE}, {@code Integer.MIN_VALUE}
     * is returned.
     */
    public static int abs(int i) {
        return (i >= 0) ? i : -i;
    }

    /**
     * Returns the absolute value of the argument. If the argument is {@code
     * Long.MIN_VALUE}, {@code Long.MIN_VALUE} is returned.
     */
    public static long abs(long l) {
        return (l >= 0) ? l : -l;
    }

    /**
     * Returns the closest double approximation of the arc cosine of the
     * argument within the range {@code [0..pi]}. The returned result is within
     * 1 ulp (unit in the last place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code acos((anything > 1) = NaN}</li>
     * <li>{@code acos((anything < -1) = NaN}</li>
     * <li>{@code acos(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to compute arc cosine of.
     * @return the arc cosine of the argument.
     */
    public static native double acos(double d);

    /**
     * Returns the closest double approximation of the arc sine of the argument
     * within the range {@code [-pi/2..pi/2]}. The returned result is within 1
     * ulp (unit in the last place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code asin((anything > 1)) = NaN}</li>
     * <li>{@code asin((anything < -1)) = NaN}</li>
     * <li>{@code asin(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose arc sine has to be computed.
     * @return the arc sine of the argument.
     */
    public static native double asin(double d);

    /**
     * Returns the closest double approximation of the arc tangent of the
     * argument within the range {@code [-pi/2..pi/2]}. The returned result is
     * within 1 ulp (unit in the last place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code atan(+0.0) = +0.0}</li>
     * <li>{@code atan(-0.0) = -0.0}</li>
     * <li>{@code atan(+infinity) = +pi/2}</li>
     * <li>{@code atan(-infinity) = -pi/2}</li>
     * <li>{@code atan(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose arc tangent has to be computed.
     * @return the arc tangent of the argument.
     */
    public static native double atan(double d);

    /**
     * Returns the closest double approximation of the arc tangent of {@code
     * y/x} within the range {@code [-pi..pi]}. This is the angle of the polar
     * representation of the rectangular coordinates (x,y). The returned result
     * is within 2 ulps (units in the last place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code atan2((anything), NaN ) = NaN;}</li>
     * <li>{@code atan2(NaN , (anything) ) = NaN;}</li>
     * <li>{@code atan2(+0.0, +(anything but NaN)) = +0.0}</li>
     * <li>{@code atan2(-0.0, +(anything but NaN)) = -0.0}</li>
     * <li>{@code atan2(+0.0, -(anything but NaN)) = +pi}</li>
     * <li>{@code atan2(-0.0, -(anything but NaN)) = -pi}</li>
     * <li>{@code atan2(+(anything but 0 and NaN), 0) = +pi/2}</li>
     * <li>{@code atan2(-(anything but 0 and NaN), 0) = -pi/2}</li>
     * <li>{@code atan2(+(anything but infinity and NaN), +infinity)} {@code =}
     * {@code +0.0}</li>
     * <li>{@code atan2(-(anything but infinity and NaN), +infinity)} {@code =}
     * {@code -0.0}</li>
     * <li>{@code atan2(+(anything but infinity and NaN), -infinity) = +pi}</li>
     * <li>{@code atan2(-(anything but infinity and NaN), -infinity) = -pi}</li>
     * <li>{@code atan2(+infinity, +infinity ) = +pi/4}</li>
     * <li>{@code atan2(-infinity, +infinity ) = -pi/4}</li>
     * <li>{@code atan2(+infinity, -infinity ) = +3pi/4}</li>
     * <li>{@code atan2(-infinity, -infinity ) = -3pi/4}</li>
     * <li>{@code atan2(+infinity, (anything but,0, NaN, and infinity))} {@code
     * =} {@code +pi/2}</li>
     * <li>{@code atan2(-infinity, (anything but,0, NaN, and infinity))} {@code
     * =} {@code -pi/2}</li>
     * </ul>
     *
     * @param y
     *            the numerator of the value whose atan has to be computed.
     * @param x
     *            the denominator of the value whose atan has to be computed.
     * @return the arc tangent of {@code y/x}.
     */
    public static native double atan2(double y, double x);

    /**
     * Returns the closest double approximation of the cube root of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code cbrt(+0.0) = +0.0}</li>
     * <li>{@code cbrt(-0.0) = -0.0}</li>
     * <li>{@code cbrt(+infinity) = +infinity}</li>
     * <li>{@code cbrt(-infinity) = -infinity}</li>
     * <li>{@code cbrt(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose cube root has to be computed.
     * @return the cube root of the argument.
     */
    public static native double cbrt(double d);

    /**
     * Returns the double conversion of the most negative (closest to negative
     * infinity) integer value greater than or equal to the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code ceil(+0.0) = +0.0}</li>
     * <li>{@code ceil(-0.0) = -0.0}</li>
     * <li>{@code ceil((anything in range (-1,0)) = -0.0}</li>
     * <li>{@code ceil(+infinity) = +infinity}</li>
     * <li>{@code ceil(-infinity) = -infinity}</li>
     * <li>{@code ceil(NaN) = NaN}</li>
     * </ul>
     */
    public static native double ceil(double d);

    /**
     * Returns the closest double approximation of the cosine of the argument.
     * The returned result is within 1 ulp (unit in the last place) of the real
     * result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code cos(+infinity) = NaN}</li>
     * <li>{@code cos(-infinity) = NaN}</li>
     * <li>{@code cos(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the angle whose cosine has to be computed, in radians.
     * @return the cosine of the argument.
     */
    public static native double cos(double d);

    /**
     * Returns the closest double approximation of the hyperbolic cosine of the
     * argument. The returned result is within 2.5 ulps (units in the last
     * place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code cosh(+infinity) = +infinity}</li>
     * <li>{@code cosh(-infinity) = +infinity}</li>
     * <li>{@code cosh(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose hyperbolic cosine has to be computed.
     * @return the hyperbolic cosine of the argument.
     */
    public static native double cosh(double d);

    /**
     * Returns the closest double approximation of the raising "e" to the power
     * of the argument. The returned result is within 1 ulp (unit in the last
     * place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code exp(+infinity) = +infinity}</li>
     * <li>{@code exp(-infinity) = +0.0}</li>
     * <li>{@code exp(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose exponential has to be computed.
     * @return the exponential of the argument.
     */
    public static native double exp(double d);

    /**
     * Returns the closest double approximation of <i>{@code e}</i><sup> {@code
     * d}</sup>{@code - 1}. If the argument is very close to 0, it is much more
     * accurate to use {@code expm1(d)+1} than {@code exp(d)} (due to
     * cancellation of significant digits). The returned result is within 1 ulp
     * (unit in the last place) of the real result.
     * <p>
     * For any finite input, the result is not less than -1.0. If the real
     * result is within 0.5 ulp of -1, -1.0 is returned.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code expm1(+0.0) = +0.0}</li>
     * <li>{@code expm1(-0.0) = -0.0}</li>
     * <li>{@code expm1(+infinity) = +infinity}</li>
     * <li>{@code expm1(-infinity) = -1.0}</li>
     * <li>{@code expm1(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to compute the <i>{@code e}</i><sup>{@code d} </sup>
     *            {@code - 1} of.
     * @return the <i>{@code e}</i><sup>{@code d}</sup>{@code - 1} value of the
     *         argument.
     */
    public static native double expm1(double d);

    /**
     * Returns the double conversion of the most positive (closest to positive
     * infinity) integer value less than or equal to the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code floor(+0.0) = +0.0}</li>
     * <li>{@code floor(-0.0) = -0.0}</li>
     * <li>{@code floor(+infinity) = +infinity}</li>
     * <li>{@code floor(-infinity) = -infinity}</li>
     * <li>{@code floor(NaN) = NaN}</li>
     * </ul>
     */
    public static native double floor(double d);

    /**
     * Returns {@code sqrt(}<i>{@code x}</i><sup>{@code 2}</sup>{@code +} <i>
     * {@code y}</i><sup>{@code 2}</sup>{@code )}. The final result is without
     * medium underflow or overflow. The returned result is within 1 ulp (unit
     * in the last place) of the real result. If one parameter remains constant,
     * the result should be semi-monotonic.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code hypot(+infinity, (anything including NaN)) = +infinity}</li>
     * <li>{@code hypot(-infinity, (anything including NaN)) = +infinity}</li>
     * <li>{@code hypot((anything including NaN), +infinity) = +infinity}</li>
     * <li>{@code hypot((anything including NaN), -infinity) = +infinity}</li>
     * <li>{@code hypot(NaN, NaN) = NaN}</li>
     * </ul>
     *
     * @param x
     *            a double number.
     * @param y
     *            a double number.
     * @return the {@code sqrt(}<i>{@code x}</i><sup>{@code 2}</sup>{@code +}
     *         <i> {@code y}</i><sup>{@code 2}</sup>{@code )} value of the
     *         arguments.
     */
    public static native double hypot(double x, double y);

    /**
     * Returns the remainder of dividing {@code x} by {@code y} using the IEEE
     * 754 rules. The result is {@code x-round(x/p)*p} where {@code round(x/p)}
     * is the nearest integer (rounded to even), but without numerical
     * cancellation problems.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code IEEEremainder((anything), 0) = NaN}</li>
     * <li>{@code IEEEremainder(+infinity, (anything)) = NaN}</li>
     * <li>{@code IEEEremainder(-infinity, (anything)) = NaN}</li>
     * <li>{@code IEEEremainder(NaN, (anything)) = NaN}</li>
     * <li>{@code IEEEremainder((anything), NaN) = NaN}</li>
     * <li>{@code IEEEremainder(x, +infinity) = x } where x is anything but
     * +/-infinity</li>
     * <li>{@code IEEEremainder(x, -infinity) = x } where x is anything but
     * +/-infinity</li>
     * </ul>
     *
     * @param x
     *            the numerator of the operation.
     * @param y
     *            the denominator of the operation.
     * @return the IEEE754 floating point reminder of of {@code x/y}.
     */
    public static native double IEEEremainder(double x, double y);

    /**
     * Returns the closest double approximation of the natural logarithm of the
     * argument. The returned result is within 1 ulp (unit in the last place) of
     * the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code log(+0.0) = -infinity}</li>
     * <li>{@code log(-0.0) = -infinity}</li>
     * <li>{@code log((anything < 0) = NaN}</li>
     * <li>{@code log(+infinity) = +infinity}</li>
     * <li>{@code log(-infinity) = NaN}</li>
     * <li>{@code log(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose log has to be computed.
     * @return the natural logarithm of the argument.
     */
    public static native double log(double d);

    /**
     * Returns the closest double approximation of the base 10 logarithm of the
     * argument. The returned result is within 1 ulp (unit in the last place) of
     * the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code log10(+0.0) = -infinity}</li>
     * <li>{@code log10(-0.0) = -infinity}</li>
     * <li>{@code log10((anything < 0) = NaN}</li>
     * <li>{@code log10(+infinity) = +infinity}</li>
     * <li>{@code log10(-infinity) = NaN}</li>
     * <li>{@code log10(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose base 10 log has to be computed.
     * @return the natural logarithm of the argument.
     */
    public static native double log10(double d);

    /**
     * Returns the closest double approximation of the natural logarithm of the
     * sum of the argument and 1. If the argument is very close to 0, it is much
     * more accurate to use {@code log1p(d)} than {@code log(1.0+d)} (due to
     * numerical cancellation). The returned result is within 1 ulp (unit in the
     * last place) of the real result and is semi-monotonic.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code log1p(+0.0) = +0.0}</li>
     * <li>{@code log1p(-0.0) = -0.0}</li>
     * <li>{@code log1p((anything < 1)) = NaN}</li>
     * <li>{@code log1p(-1.0) = -infinity}</li>
     * <li>{@code log1p(+infinity) = +infinity}</li>
     * <li>{@code log1p(-infinity) = NaN}</li>
     * <li>{@code log1p(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to compute the {@code ln(1+d)} of.
     * @return the natural logarithm of the sum of the argument and 1.
     */
    public static native double log1p(double d);

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code max(NaN, (anything)) = NaN}</li>
     * <li>{@code max((anything), NaN) = NaN}</li>
     * <li>{@code max(+0.0, -0.0) = +0.0}</li>
     * <li>{@code max(-0.0, +0.0) = +0.0}</li>
     * </ul>
     */
    public static double max(double d1, double d2) {
        if (d1 > d2) {
            return d1;
        }
        if (d1 < d2) {
            return d2;
        }
        /* if either arg is NaN, return NaN */
        if (d1 != d2) {
            return Double.NaN;
        }
        /* max(+0.0,-0.0) == +0.0 */
        /* Double.doubleToRawLongBits(0.0d) == 0 */
        if (Double.doubleToRawLongBits(d1) != 0) {
            return d2;
        }
        return 0.0d;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code max(NaN, (anything)) = NaN}</li>
     * <li>{@code max((anything), NaN) = NaN}</li>
     * <li>{@code max(+0.0, -0.0) = +0.0}</li>
     * <li>{@code max(-0.0, +0.0) = +0.0}</li>
     * </ul>
     */
    public static float max(float f1, float f2) {
        if (f1 > f2) {
            return f1;
        }
        if (f1 < f2) {
            return f2;
        }
        /* if either arg is NaN, return NaN */
        if (f1 != f2) {
            return Float.NaN;
        }
        /* max(+0.0,-0.0) == +0.0 */
        /* Float.floatToRawIntBits(0.0f) == 0*/
        if (Float.floatToRawIntBits(f1) != 0) {
            return f2;
        }
        return 0.0f;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     */
    public static int max(int i1, int i2) {
        return i1 > i2 ? i1 : i2;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     */
    public static long max(long l1, long l2) {
        return l1 > l2 ? l1 : l2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code min(NaN, (anything)) = NaN}</li>
     * <li>{@code min((anything), NaN) = NaN}</li>
     * <li>{@code min(+0.0, -0.0) = -0.0}</li>
     * <li>{@code min(-0.0, +0.0) = -0.0}</li>
     * </ul>
     */
    public static double min(double d1, double d2) {
        if (d1 > d2) {
            return d2;
        }
        if (d1 < d2) {
            return d1;
        }
        /* if either arg is NaN, return NaN */
        if (d1 != d2) {
            return Double.NaN;
        }
        /* min(+0.0,-0.0) == -0.0 */
        /* 0x8000000000000000L == Double.doubleToRawLongBits(-0.0d) */
        if (Double.doubleToRawLongBits(d1) == 0x8000000000000000L) {
            return -0.0d;
        }
        return d2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code min(NaN, (anything)) = NaN}</li>
     * <li>{@code min((anything), NaN) = NaN}</li>
     * <li>{@code min(+0.0, -0.0) = -0.0}</li>
     * <li>{@code min(-0.0, +0.0) = -0.0}</li>
     * </ul>
     */
    public static float min(float f1, float f2) {
        if (f1 > f2) {
            return f2;
        }
        if (f1 < f2) {
            return f1;
        }
        /* if either arg is NaN, return NaN */
        if (f1 != f2) {
            return Float.NaN;
        }
        /* min(+0.0,-0.0) == -0.0 */
        /* 0x80000000 == Float.floatToRawIntBits(-0.0f) */
        if (Float.floatToRawIntBits(f1) == 0x80000000) {
            return -0.0f;
        }
        return f2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     */
    public static int min(int i1, int i2) {
        return i1 < i2 ? i1 : i2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     */
    public static long min(long l1, long l2) {
        return l1 < l2 ? l1 : l2;
    }

    /**
     * Returns the closest double approximation of the result of raising {@code
     * x} to the power of {@code y}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code pow((anything), +0.0) = 1.0}</li>
     * <li>{@code pow((anything), -0.0) = 1.0}</li>
     * <li>{@code pow(x, 1.0) = x}</li>
     * <li>{@code pow((anything), NaN) = NaN}</li>
     * <li>{@code pow(NaN, (anything except 0)) = NaN}</li>
     * <li>{@code pow(+/-(|x| > 1), +infinity) = +infinity}</li>
     * <li>{@code pow(+/-(|x| > 1), -infinity) = +0.0}</li>
     * <li>{@code pow(+/-(|x| < 1), +infinity) = +0.0}</li>
     * <li>{@code pow(+/-(|x| < 1), -infinity) = +infinity}</li>
     * <li>{@code pow(+/-1.0 , +infinity) = NaN}</li>
     * <li>{@code pow(+/-1.0 , -infinity) = NaN}</li>
     * <li>{@code pow(+0.0, (+anything except 0, NaN)) = +0.0}</li>
     * <li>{@code pow(-0.0, (+anything except 0, NaN, odd integer)) = +0.0}</li>
     * <li>{@code pow(+0.0, (-anything except 0, NaN)) = +infinity}</li>
     * <li>{@code pow(-0.0, (-anything except 0, NAN, odd integer))} {@code =}
     * {@code +infinity}</li>
     * <li>{@code pow(-0.0, (odd integer)) = -pow( +0 , (odd integer) )}</li>
     * <li>{@code pow(+infinity, (+anything except 0, NaN)) = +infinity}</li>
     * <li>{@code pow(+infinity, (-anything except 0, NaN)) = +0.0}</li>
     * <li>{@code pow(-infinity, (anything)) = -pow(0, (-anything))}</li>
     * <li>{@code pow((-anything), (integer))} {@code =} {@code
     * pow(-1,(integer))*pow(+anything,integer) }</li>
     * <li>{@code pow((-anything except 0 and inf), (non-integer)) = NAN}</li>
     * </ul>
     *
     * @param x
     *            the base of the operation.
     * @param y
     *            the exponent of the operation.
     * @return {@code x} to the power of {@code y}.
     */
    public static native double pow(double x, double y);

    /**
     * Returns the double conversion of the result of rounding the argument to
     * an integer. Tie breaks are rounded towards even.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code rint(+0.0) = +0.0}</li>
     * <li>{@code rint(-0.0) = -0.0}</li>
     * <li>{@code rint(+infinity) = +infinity}</li>
     * <li>{@code rint(-infinity) = -infinity}</li>
     * <li>{@code rint(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to be rounded.
     * @return the closest integer to the argument (as a double).
     */
    public static native double rint(double d);

    /**
     * Returns the result of rounding the argument to an integer. The result is
     * equivalent to {@code (long) Math.floor(d+0.5)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code round(+0.0) = +0.0}</li>
     * <li>{@code round(-0.0) = +0.0}</li>
     * <li>{@code round((anything > Long.MAX_VALUE) = Long.MAX_VALUE}</li>
     * <li>{@code round((anything < Long.MIN_VALUE) = Long.MIN_VALUE}</li>
     * <li>{@code round(+infinity) = Long.MAX_VALUE}</li>
     * <li>{@code round(-infinity) = Long.MIN_VALUE}</li>
     * <li>{@code round(NaN) = +0.0}</li>
     * </ul>
     *
     * @param d
     *            the value to be rounded.
     * @return the closest integer to the argument.
     */
    public static long round(double d) {
        // check for NaN
        if (d != d) {
            return 0L;
        }
        return (long) floor(d + 0.5d);
    }

    /**
     * Returns the result of rounding the argument to an integer. The result is
     * equivalent to {@code (int) Math.floor(f+0.5)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code round(+0.0) = +0.0}</li>
     * <li>{@code round(-0.0) = +0.0}</li>
     * <li>{@code round((anything > Integer.MAX_VALUE) = Integer.MAX_VALUE}</li>
     * <li>{@code round((anything < Integer.MIN_VALUE) = Integer.MIN_VALUE}</li>
     * <li>{@code round(+infinity) = Integer.MAX_VALUE}</li>
     * <li>{@code round(-infinity) = Integer.MIN_VALUE}</li>
     * <li>{@code round(NaN) = +0.0}</li>
     * </ul>
     *
     * @param f
     *            the value to be rounded.
     * @return the closest integer to the argument.
     */
    public static int round(float f) {
        // check for NaN
        if (f != f) {
            return 0;
        }
        return (int) floor(f + 0.5f);
    }

    /**
     * Returns the signum function of the argument. If the argument is less than
     * zero, it returns -1.0. If the argument is greater than zero, 1.0 is
     * returned. If the argument is either positive or negative zero, the
     * argument is returned as result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code signum(+0.0) = +0.0}</li>
     * <li>{@code signum(-0.0) = -0.0}</li>
     * <li>{@code signum(+infinity) = +1.0}</li>
     * <li>{@code signum(-infinity) = -1.0}</li>
     * <li>{@code signum(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose signum has to be computed.
     * @return the value of the signum function.
     */
    public static double signum(double d) {
        if (Double.isNaN(d)) {
            return Double.NaN;
        }
        double sig = d;
        if (d > 0) {
            sig = 1.0;
        } else if (d < 0) {
            sig = -1.0;
        }
        return sig;
    }

    /**
     * Returns the signum function of the argument. If the argument is less than
     * zero, it returns -1.0. If the argument is greater than zero, 1.0 is
     * returned. If the argument is either positive or negative zero, the
     * argument is returned as result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code signum(+0.0) = +0.0}</li>
     * <li>{@code signum(-0.0) = -0.0}</li>
     * <li>{@code signum(+infinity) = +1.0}</li>
     * <li>{@code signum(-infinity) = -1.0}</li>
     * <li>{@code signum(NaN) = NaN}</li>
     * </ul>
     *
     * @param f
     *            the value whose signum has to be computed.
     * @return the value of the signum function.
     */
    public static float signum(float f) {
        if (Float.isNaN(f)) {
            return Float.NaN;
        }
        float sig = f;
        if (f > 0) {
            sig = 1.0f;
        } else if (f < 0) {
            sig = -1.0f;
        }
        return sig;
    }

    /**
     * Returns the closest double approximation of the sine of the argument. The
     * returned result is within 1 ulp (unit in the last place) of the real
     * result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code sin(+0.0) = +0.0}</li>
     * <li>{@code sin(-0.0) = -0.0}</li>
     * <li>{@code sin(+infinity) = NaN}</li>
     * <li>{@code sin(-infinity) = NaN}</li>
     * <li>{@code sin(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the angle whose sin has to be computed, in radians.
     * @return the sine of the argument.
     */
    public static native double sin(double d);

    /**
     * Returns the closest double approximation of the hyperbolic sine of the
     * argument. The returned result is within 2.5 ulps (units in the last
     * place) of the real result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code sinh(+0.0) = +0.0}</li>
     * <li>{@code sinh(-0.0) = -0.0}</li>
     * <li>{@code sinh(+infinity) = +infinity}</li>
     * <li>{@code sinh(-infinity) = -infinity}</li>
     * <li>{@code sinh(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose hyperbolic sine has to be computed.
     * @return the hyperbolic sine of the argument.
     */
    public static native double sinh(double d);

    /**
     * Returns the closest double approximation of the square root of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code sqrt(+0.0) = +0.0}</li>
     * <li>{@code sqrt(-0.0) = -0.0}</li>
     * <li>{@code sqrt( (anything < 0) ) = NaN}</li>
     * <li>{@code sqrt(+infinity) = +infinity}</li>
     * <li>{@code sqrt(NaN) = NaN}</li>
     * </ul>
     */
    public static native double sqrt(double d);

    /**
     * Returns the closest double approximation of the tangent of the argument.
     * The returned result is within 1 ulp (unit in the last place) of the real
     * result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code tan(+0.0) = +0.0}</li>
     * <li>{@code tan(-0.0) = -0.0}</li>
     * <li>{@code tan(+infinity) = NaN}</li>
     * <li>{@code tan(-infinity) = NaN}</li>
     * <li>{@code tan(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the angle whose tangent has to be computed, in radians.
     * @return the tangent of the argument.
     */
    public static native double tan(double d);

    /**
     * Returns the closest double approximation of the hyperbolic tangent of the
     * argument. The absolute value is always less than 1. The returned result
     * is within 2.5 ulps (units in the last place) of the real result. If the
     * real result is within 0.5ulp of 1 or -1, it should return exactly +1 or
     * -1.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code tanh(+0.0) = +0.0}</li>
     * <li>{@code tanh(-0.0) = -0.0}</li>
     * <li>{@code tanh(+infinity) = +1.0}</li>
     * <li>{@code tanh(-infinity) = -1.0}</li>
     * <li>{@code tanh(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose hyperbolic tangent has to be computed.
     * @return the hyperbolic tangent of the argument.
     */
    public static native double tanh(double d);

    /**
     * Returns a pseudo-random double {@code n}, where {@code n >= 0.0 && n < 1.0}.
     * This method reuses a single instance of {@link java.util.Random}.
     * This method is thread-safe because access to the {@code Random} is synchronized,
     * but this harms scalability. Applications may find a performance benefit from
     * allocating a {@code Random} for each of their threads.
     *
     * @return a pseudo-random number.
     */
    public static double random() {
        return NoImagePreloadHolder.INSTANCE.nextDouble();
    }

    /**
     * Set the seed for the pseudo random generator used by {@link #random()}
     * and {@link #randomIntInternal()}.
     *
     * @hide for internal use only.
     */
    public static void setRandomSeedInternal(long seed) {
        NoImagePreloadHolder.INSTANCE.setSeed(seed);
    }

    /**
     * @hide for internal use only.
     */
    public static int randomIntInternal() {
        return NoImagePreloadHolder.INSTANCE.nextInt();
    }

    /**
     * Returns the measure in radians of the supplied degree angle. The result
     * is {@code angdeg / 180 * pi}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code toRadians(+0.0) = +0.0}</li>
     * <li>{@code toRadians(-0.0) = -0.0}</li>
     * <li>{@code toRadians(+infinity) = +infinity}</li>
     * <li>{@code toRadians(-infinity) = -infinity}</li>
     * <li>{@code toRadians(NaN) = NaN}</li>
     * </ul>
     *
     * @param angdeg
     *            an angle in degrees.
     * @return the radian measure of the angle.
     */
    public static double toRadians(double angdeg) {
        return angdeg / 180d * PI;
    }

    /**
     * Returns the measure in degrees of the supplied radian angle. The result
     * is {@code angrad * 180 / pi}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code toDegrees(+0.0) = +0.0}</li>
     * <li>{@code toDegrees(-0.0) = -0.0}</li>
     * <li>{@code toDegrees(+infinity) = +infinity}</li>
     * <li>{@code toDegrees(-infinity) = -infinity}</li>
     * <li>{@code toDegrees(NaN) = NaN}</li>
     * </ul>
     *
     * @param angrad
     *            an angle in radians.
     * @return the degree measure of the angle.
     */
    public static double toDegrees(double angrad) {
        return angrad * 180d / PI;
    }

    /**
     * Returns the fused multiply add of the three arguments; that is,
     * returns the exact product of the first two arguments summed
     * with the third argument and then rounded once to the nearest
     * {@code double}.
     *
     * The rounding is done using the {@linkplain
     * java.math.RoundingMode#HALF_EVEN round to nearest even
     * rounding mode}.
     *
     * In contrast, if {@code a * b + c} is evaluated as a regular
     * floating-point expression, two rounding errors are involved,
     * the first for the multiply operation, the second for the
     * addition operation.
     *
     * <p>Special cases:
     * <ul>
     * <li> If any argument is NaN, the result is NaN.
     *
     * <li> If one of the first two arguments is infinite and the
     * other is zero, the result is NaN.
     *
     * <li> If the exact product of the first two arguments is infinite
     * (in other words, at least one of the arguments is infinite and
     * the other is neither zero nor NaN) and the third argument is an
     * infinity of the opposite sign, the result is NaN.
     *
     * </ul>
     *
     * <p>Note that {@code fma(a, 1.0, c)} returns the same
     * result as ({@code a + c}).  However,
     * {@code fma(a, b, +0.0)} does <em>not</em> always return the
     * same result as ({@code a * b}) since
     * {@code fma(-0.0, +0.0, +0.0)} is {@code +0.0} while
     * ({@code -0.0 * +0.0}) is {@code -0.0}; {@code fma(a, b, -0.0)} is
     * equivalent to ({@code a * b}) however.
     *
     * @apiNote This method corresponds to the fusedMultiplyAdd
     * operation defined in IEEE 754-2008.
     *
     * @param a a value
     * @param b a value
     * @param c a value
     *
     * @return (<i>a</i>&nbsp;&times;&nbsp;<i>b</i>&nbsp;+&nbsp;<i>c</i>)
     * computed, as if with unlimited range and precision, and rounded
     * once to the nearest {@code double} value
     *
     * @since 9
     */
    public static double fma(double a, double b, double c) {
        /*
         * Infinity and NaN arithmetic is not quite the same with two
         * roundings as opposed to just one so the simple expression
         * "a * b + c" cannot always be used to compute the correct
         * result.  With two roundings, the product can overflow and
         * if the addend is infinite, a spurious NaN can be produced
         * if the infinity from the overflow and the infinite addend
         * have opposite signs.
         */

        // First, screen for and handle non-finite input values whose
        // arithmetic is not supported by BigDecimal.
        if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)) {
            return Double.NaN;
        } else { // All inputs non-NaN
            boolean infiniteA = Double.isInfinite(a);
            boolean infiniteB = Double.isInfinite(b);
            boolean infiniteC = Double.isInfinite(c);
            double result;

            if (infiniteA || infiniteB || infiniteC) {
                if (infiniteA && b == 0.0 ||
                    infiniteB && a == 0.0 ) {
                    return Double.NaN;
                }
                // Store product in a double field to cause an
                // overflow even if non-strictfp evaluation is being
                // used.
                double product = a * b;
                if (Double.isInfinite(product) && !infiniteA && !infiniteB) {
                    // Intermediate overflow; might cause a
                    // spurious NaN if added to infinite c.
                    assert Double.isInfinite(c);
                    return c;
                } else {
                    result = product + c;
                    assert !Double.isFinite(result);
                    return result;
                }
            } else { // All inputs finite
                BigDecimal product = (new BigDecimal(a)).multiply(new BigDecimal(b));
                if (c == 0.0) { // Positive or negative zero
                    // If the product is an exact zero, use a
                    // floating-point expression to compute the sign
                    // of the zero final result. The product is an
                    // exact zero if and only if at least one of a and
                    // b is zero.
                    if (a == 0.0 || b == 0.0) {
                        return a * b + c;
                    } else {
                        // The sign of a zero addend doesn't matter if
                        // the product is nonzero. The sign of a zero
                        // addend is not factored in the result if the
                        // exact product is nonzero but underflows to
                        // zero; see IEEE-754 2008 section 6.3 "The
                        // sign bit".
                        return product.doubleValue();
                    }
                } else {
                    return product.add(new BigDecimal(c)).doubleValue();
                }
            }
        }
    }

    /**
     * Returns the fused multiply add of the three arguments; that is,
     * returns the exact product of the first two arguments summed
     * with the third argument and then rounded once to the nearest
     * {@code float}.
     *
     * The rounding is done using the {@linkplain
     * java.math.RoundingMode#HALF_EVEN round to nearest even
     * rounding mode}.
     *
     * In contrast, if {@code a * b + c} is evaluated as a regular
     * floating-point expression, two rounding errors are involved,
     * the first for the multiply operation, the second for the
     * addition operation.
     *
     * <p>Special cases:
     * <ul>
     * <li> If any argument is NaN, the result is NaN.
     *
     * <li> If one of the first two arguments is infinite and the
     * other is zero, the result is NaN.
     *
     * <li> If the exact product of the first two arguments is infinite
     * (in other words, at least one of the arguments is infinite and
     * the other is neither zero nor NaN) and the third argument is an
     * infinity of the opposite sign, the result is NaN.
     *
     * </ul>
     *
     * <p>Note that {@code fma(a, 1.0f, c)} returns the same
     * result as ({@code a + c}).  However,
     * {@code fma(a, b, +0.0f)} does <em>not</em> always return the
     * same result as ({@code a * b}) since
     * {@code fma(-0.0f, +0.0f, +0.0f)} is {@code +0.0f} while
     * ({@code -0.0f * +0.0f}) is {@code -0.0f}; {@code fma(a, b, -0.0f)} is
     * equivalent to ({@code a * b}) however.
     *
     * @apiNote This method corresponds to the fusedMultiplyAdd
     * operation defined in IEEE 754-2008.
     *
     * @param a a value
     * @param b a value
     * @param c a value
     *
     * @return (<i>a</i>&nbsp;&times;&nbsp;<i>b</i>&nbsp;+&nbsp;<i>c</i>)
     * computed, as if with unlimited range and precision, and rounded
     * once to the nearest {@code float} value
     *
     * @since 9
     */
    public static float fma(float a, float b, float c) {
        /*
         *  Since the double format has more than twice the precision
         *  of the float format, the multiply of a * b is exact in
         *  double. The add of c to the product then incurs one
         *  rounding error. Since the double format moreover has more
         *  than (2p + 2) precision bits compared to the p bits of the
         *  float format, the two roundings of (a * b + c), first to
         *  the double format and then secondarily to the float format,
         *  are equivalent to rounding the intermediate result directly
         *  to the float format.
         *
         * In terms of strictfp vs default-fp concerns related to
         * overflow and underflow, since
         *
         * (Float.MAX_VALUE * Float.MAX_VALUE) << Double.MAX_VALUE
         * (Float.MIN_VALUE * Float.MIN_VALUE) >> Double.MIN_VALUE
         *
         * neither the multiply nor add will overflow or underflow in
         * double. Therefore, it is not necessary for this method to
         * be declared strictfp to have reproducible
         * behavior. However, it is necessary to explicitly store down
         * to a float variable to avoid returning a value in the float
         * extended value set.
         */
        float result = (float)(((double) a * (double) b ) + (double) c);
        return result;
    }

    /**
     * Returns the argument's ulp (unit in the last place). The size of a ulp of
     * a double value is the positive distance between this value and the double
     * value next larger in magnitude. For non-NaN {@code x}, {@code ulp(-x) ==
     * ulp(x)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code ulp(+0.0) = Double.MIN_VALUE}</li>
     * <li>{@code ulp(-0.0) = Double.MIN_VALUE}</li>
     * <li>{@code ulp(+infinity) = infinity}</li>
     * <li>{@code ulp(-infinity) = infinity}</li>
     * <li>{@code ulp(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the floating-point value to compute ulp of.
     * @return the size of a ulp of the argument.
     */
    public static double ulp(double d) {
        // special cases
        if (Double.isInfinite(d)) {
            return Double.POSITIVE_INFINITY;
        } else if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE) {
            return pow(2, 971);
        }
        d = abs(d);
        return nextafter(d, Double.MAX_VALUE) - d;
    }

    private static native double nextafter(double x, double y);

    /**
     * Returns the argument's ulp (unit in the last place). The size of a ulp of
     * a float value is the positive distance between this value and the float
     * value next larger in magnitude. For non-NaN {@code x}, {@code ulp(-x) ==
     * ulp(x)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code ulp(+0.0) = Float.MIN_VALUE}</li>
     * <li>{@code ulp(-0.0) = Float.MIN_VALUE}</li>
     * <li>{@code ulp(+infinity) = infinity}</li>
     * <li>{@code ulp(-infinity) = infinity}</li>
     * <li>{@code ulp(NaN) = NaN}</li>
     * </ul>
     *
     * @param f
     *            the floating-point value to compute ulp of.
     * @return the size of a ulp of the argument.
     */
    public static float ulp(float f) {
        // special cases
        if (Float.isNaN(f)) {
            return Float.NaN;
        } else if (Float.isInfinite(f)) {
            return Float.POSITIVE_INFINITY;
        } else if (f == Float.MAX_VALUE || f == -Float.MAX_VALUE) {
            return (float) pow(2, 104);
        }

        f = Math.abs(f);
        int hx = Float.floatToRawIntBits(f);
        int hy = Float.floatToRawIntBits(Float.MAX_VALUE);
        if ((hx & 0x7fffffff) == 0) { /* f == 0 */
            return Float.intBitsToFloat((hy & 0x80000000) | 0x1);
        }
        if ((hx > 0) ^ (hx > hy)) { /* |f| < |Float.MAX_VALUE| */
            hx += 1;
        } else {
            hx -= 1;
        }
        return Float.intBitsToFloat(hx) - f;
    }

    /**
     * Returns a double with the given magnitude and the sign of {@code sign}.
     * If {@code sign} is NaN, the sign of the result is arbitrary.
     * If you need a determinate sign in such cases, use {@code StrictMath.copySign}.
     * @since 1.6
     */
    public static double copySign(double magnitude, double sign) {
        long magnitudeBits = Double.doubleToRawLongBits(magnitude);
        long signBits = Double.doubleToRawLongBits(sign);
        magnitudeBits = (magnitudeBits & ~Double.SIGN_MASK) | (signBits & Double.SIGN_MASK);
        return Double.longBitsToDouble(magnitudeBits);
    }

    /**
     * Returns a float with the given magnitude and the sign of {@code sign}.
     * If {@code sign} is NaN, the sign of the result is arbitrary.
     * If you need a determinate sign in such cases, use {@code StrictMath.copySign}.
     * @since 1.6
     */
    public static float copySign(float magnitude, float sign) {
        int magnitudeBits = Float.floatToRawIntBits(magnitude);
        int signBits = Float.floatToRawIntBits(sign);
        magnitudeBits = (magnitudeBits & ~Float.SIGN_MASK) | (signBits & Float.SIGN_MASK);
        return Float.intBitsToFloat(magnitudeBits);
    }

    /**
     * Returns the unbiased base-2 exponent of float {@code f}.
     * @since 1.6
     */
    public static int getExponent(float f) {
        int bits = Float.floatToRawIntBits(f);
        bits = (bits & Float.EXPONENT_MASK) >> Float.MANTISSA_BITS;
        return bits - Float.EXPONENT_BIAS;
    }

    /**
     * Returns the unbiased base-2 exponent of double {@code d}.
     * @since 1.6
     */
    public static int getExponent(double d) {
        long bits = Double.doubleToRawLongBits(d);
        bits = (bits & Double.EXPONENT_MASK) >> Double.MANTISSA_BITS;
        return (int) bits - Double.EXPONENT_BIAS;
    }

    /**
     * Returns the next double after {@code start} in the given {@code direction}.
     * @since 1.6
     */
    public static double nextAfter(double start, double direction) {
        if (start == 0 && direction == 0) {
            return direction;
        }
        return nextafter(start, direction);
    }

    /**
     * Returns the next float after {@code start} in the given {@code direction}.
     * @since 1.6
     */
    public static float nextAfter(float start, double direction) {
        if (Float.isNaN(start) || Double.isNaN(direction)) {
            return Float.NaN;
        }
        if (start == 0 && direction == 0) {
            return (float) direction;
        }
        if ((start == Float.MIN_VALUE && direction < start)
                || (start == -Float.MIN_VALUE && direction > start)) {
            return (start > 0 ? 0f : -0f);
        }
        if (Float.isInfinite(start) && (direction != start)) {
            return (start > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE);
        }
        if ((start == Float.MAX_VALUE && direction > start)
                || (start == -Float.MAX_VALUE && direction < start)) {
            return (start > 0 ? Float.POSITIVE_INFINITY
                    : Float.NEGATIVE_INFINITY);
        }
        if (direction > start) {
            if (start > 0) {
                return Float.intBitsToFloat(Float.floatToIntBits(start) + 1);
            }
            if (start < 0) {
                return Float.intBitsToFloat(Float.floatToIntBits(start) - 1);
            }
            return +Float.MIN_VALUE;
        }
        if (direction < start) {
            if (start > 0) {
                return Float.intBitsToFloat(Float.floatToIntBits(start) - 1);
            }
            if (start < 0) {
                return Float.intBitsToFloat(Float.floatToIntBits(start) + 1);
            }
            return -Float.MIN_VALUE;
        }
        return (float) direction;
    }

    /**
     * Returns the next double larger than {@code d}.
     * @since 1.6
     */
    public static double nextUp(double d) {
        if (Double.isNaN(d)) {
            return Double.NaN;
        }
        if (d == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        if (d == 0) {
            return Double.MIN_VALUE;
        } else if (d > 0) {
            return Double.longBitsToDouble(Double.doubleToLongBits(d) + 1);
        } else {
            return Double.longBitsToDouble(Double.doubleToLongBits(d) - 1);
        }
    }

    /**
     * Returns the next float larger than {@code f}.
     * @since 1.6
     */
    public static float nextUp(float f) {
        if (Float.isNaN(f)) {
            return Float.NaN;
        }
        if (f == Float.POSITIVE_INFINITY) {
            return Float.POSITIVE_INFINITY;
        }
        if (f == 0) {
            return Float.MIN_VALUE;
        } else if (f > 0) {
            return Float.intBitsToFloat(Float.floatToIntBits(f) + 1);
        } else {
            return Float.intBitsToFloat(Float.floatToIntBits(f) - 1);
        }
    }

    /**
     * Returns {@code d} * 2^{@code scaleFactor}. The result may be rounded.
     * @since 1.6
     */
    public static double scalb(double d, int scaleFactor) {
        if (Double.isNaN(d) || Double.isInfinite(d) || d == 0) {
            return d;
        }
        // change double to long for calculation
        long bits = Double.doubleToLongBits(d);
        // the sign of the results must be the same of given d
        long sign = bits & Double.SIGN_MASK;
        // calculates the factor of the result
        long factor = ((bits & Double.EXPONENT_MASK) >> Double.MANTISSA_BITS)
                - Double.EXPONENT_BIAS + scaleFactor;

        // calculates the factor of sub-normal values
        int subNormalFactor = Long.numberOfLeadingZeros(bits & ~Double.SIGN_MASK)
                - Double.NON_MANTISSA_BITS;
        if (subNormalFactor < 0) {
            // not sub-normal values
            subNormalFactor = 0;
        } else {
            factor = factor - subNormalFactor;
        }
        if (factor > Double.MAX_EXPONENT) {
            return (d > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
        }

        long result;
        // if result is a sub-normal
        if (factor <= -Double.EXPONENT_BIAS) {
            // the number of digits that shifts
            long digits = factor + Double.EXPONENT_BIAS + subNormalFactor;
            if (Math.abs(d) < Double.MIN_NORMAL) {
                // origin d is already sub-normal
                result = shiftLongBits(bits & Double.MANTISSA_MASK, digits);
            } else {
                // origin d is not sub-normal, change mantissa to sub-normal
                result = shiftLongBits(bits & Double.MANTISSA_MASK | 0x0010000000000000L, digits - 1);
            }
        } else {
            if (Math.abs(d) >= Double.MIN_NORMAL) {
                // common situation
                result = ((factor + Double.EXPONENT_BIAS) << Double.MANTISSA_BITS)
                        | (bits & Double.MANTISSA_MASK);
            } else {
                // origin d is sub-normal, change mantissa to normal style
                result = ((factor + Double.EXPONENT_BIAS) << Double.MANTISSA_BITS)
                        | ((bits << (subNormalFactor + 1)) & Double.MANTISSA_MASK);
            }
        }
        return Double.longBitsToDouble(result | sign);
    }

    /**
     * Returns {@code d} * 2^{@code scaleFactor}. The result may be rounded.
     * @since 1.6
     */
    public static float scalb(float d, int scaleFactor) {
        if (Float.isNaN(d) || Float.isInfinite(d) || d == 0) {
            return d;
        }
        int bits = Float.floatToIntBits(d);
        int sign = bits & Float.SIGN_MASK;
        int factor = ((bits & Float.EXPONENT_MASK) >> Float.MANTISSA_BITS)
                - Float.EXPONENT_BIAS + scaleFactor;
        // calculates the factor of sub-normal values
        int subNormalFactor = Integer.numberOfLeadingZeros(bits & ~Float.SIGN_MASK)
                - Float.NON_MANTISSA_BITS;
        if (subNormalFactor < 0) {
            // not sub-normal values
            subNormalFactor = 0;
        } else {
            factor = factor - subNormalFactor;
        }
        if (factor > Float.MAX_EXPONENT) {
            return (d > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);
        }

        int result;
        // if result is a sub-normal
        if (factor <= -Float.EXPONENT_BIAS) {
            // the number of digits that shifts
            int digits = factor + Float.EXPONENT_BIAS + subNormalFactor;
            if (Math.abs(d) < Float.MIN_NORMAL) {
                // origin d is already sub-normal
                result = shiftIntBits(bits & Float.MANTISSA_MASK, digits);
            } else {
                // origin d is not sub-normal, change mantissa to sub-normal
                result = shiftIntBits(bits & Float.MANTISSA_MASK | 0x00800000, digits - 1);
            }
        } else {
            if (Math.abs(d) >= Float.MIN_NORMAL) {
                // common situation
                result = ((factor + Float.EXPONENT_BIAS) << Float.MANTISSA_BITS)
                        | (bits & Float.MANTISSA_MASK);
            } else {
                // origin d is sub-normal, change mantissa to normal style
                result = ((factor + Float.EXPONENT_BIAS) << Float.MANTISSA_BITS)
                        | ((bits << (subNormalFactor + 1)) & Float.MANTISSA_MASK);
            }
        }
        return Float.intBitsToFloat(result | sign);
    }

    // Shifts integer bits as float, if the digits is positive, left-shift; if
    // not, shift to right and calculate its carry.
    private static int shiftIntBits(int bits, int digits) {
        if (digits > 0) {
            return bits << digits;
        }
        // change it to positive
        int absDigits = -digits;
        if (!(Integer.numberOfLeadingZeros(bits & ~Float.SIGN_MASK) <= (32 - absDigits))) {
            return 0;
        }
        int ret = bits >> absDigits;
        boolean halfBit = ((bits >> (absDigits - 1)) & 0x1) == 1;
        if (halfBit) {
            if (Integer.numberOfTrailingZeros(bits) < (absDigits - 1)) {
                ret = ret + 1;
            }
            if (Integer.numberOfTrailingZeros(bits) == (absDigits - 1)) {
                if ((ret & 0x1) == 1) {
                    ret = ret + 1;
                }
            }
        }
        return ret;
    }

    // Shifts long bits as double, if the digits is positive, left-shift; if
    // not, shift to right and calculate its carry.
    private static long shiftLongBits(long bits, long digits) {
        if (digits > 0) {
            return bits << digits;
        }
        // change it to positive
        long absDigits = -digits;
        if (!(Long.numberOfLeadingZeros(bits & ~Double.SIGN_MASK) <= (64 - absDigits))) {
            return 0;
        }
        long ret = bits >> absDigits;
        boolean halfBit = ((bits >> (absDigits - 1)) & 0x1) == 1;
        if (halfBit) {
            // some bits will remain after shifting, calculates its carry
            // subnormal
            if (Long.numberOfTrailingZeros(bits) < (absDigits - 1)) {
                ret = ret + 1;
            }
            if (Long.numberOfTrailingZeros(bits) == (absDigits - 1)) {
                if ((ret & 0x1) == 1) {
                    ret = ret + 1;
                }
            }
        }
        return ret;
    }

    // Sugar starts here

    /**
     * Returns the sum of its arguments,
     * throwing an exception if the result overflows an {@code int}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int addExact(int x, int y) {
        int r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("integer overflow");
        }
        return r;
    }

    /**
     * Returns the sum of its arguments,
     * throwing an exception if the result overflows a {@code long}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 1.8
     */
    public static long addExact(long x, long y) {
        long r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("long overflow");
        }
        return r;
    }

    /**
     * Returns the difference of the arguments,
     * throwing an exception if the result overflows an {@code int}.
     *
     * @param x the first value
     * @param y the second value to subtract from the first
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int subtractExact(int x, int y) {
        int r = x - y;
        // HD 2-12 Overflow iff the arguments have different signs and
        // the sign of the result is different than the sign of x
        if (((x ^ y) & (x ^ r)) < 0) {
            throw new ArithmeticException("integer overflow");
        }
        return r;
    }

    /**
     * Returns the difference of the arguments,
     * throwing an exception if the result overflows a {@code long}.
     *
     * @param x the first value
     * @param y the second value to subtract from the first
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 1.8
     */
    public static long subtractExact(long x, long y) {
        long r = x - y;
        // HD 2-12 Overflow iff the arguments have different signs and
        // the sign of the result is different than the sign of x
        if (((x ^ y) & (x ^ r)) < 0) {
            throw new ArithmeticException("long overflow");
        }
        return r;
    }

    /**
     * Returns the product of the arguments,
     * throwing an exception if the result overflows an {@code int}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int multiplyExact(int x, int y) {
        long r = (long)x * (long)y;
        if ((int)r != r) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)r;
    }

    /**
     * Returns the product of the arguments, throwing an exception if the result
     * overflows a {@code long}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 9
     */
    public static long multiplyExact(long x, int y) {
        return multiplyExact(x, (long) y);
    }

    /**
     * Returns the product of the arguments,
     * throwing an exception if the result overflows a {@code long}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 1.8
     */
    public static long multiplyExact(long x, long y) {
        long r = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 != 0)) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
           if (((y != 0) && (r / y != x)) ||
               (x == Long.MIN_VALUE && y == -1)) {
                throw new ArithmeticException("long overflow");
            }
        }
        return r;
    }

    /**
     * Returns the argument incremented by one, throwing an exception if the
     * result overflows an {@code int}.
     *
     * @param a the value to increment
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int incrementExact(int a) {
        if (a == Integer.MAX_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return a + 1;
    }

    /**
     * Returns the argument incremented by one, throwing an exception if the
     * result overflows a {@code long}.
     *
     * @param a the value to increment
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 1.8
     */
    public static long incrementExact(long a) {
        if (a == Long.MAX_VALUE) {
            throw new ArithmeticException("long overflow");
        }

        return a + 1L;
    }

    /**
     * Returns the argument decremented by one, throwing an exception if the
     * result overflows an {@code int}.
     *
     * @param a the value to decrement
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int decrementExact(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return a - 1;
    }

    /**
     * Returns the argument decremented by one, throwing an exception if the
     * result overflows a {@code long}.
     *
     * @param a the value to decrement
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 1.8
     */
    public static long decrementExact(long a) {
        if (a == Long.MIN_VALUE) {
            throw new ArithmeticException("long overflow");
        }

        return a - 1L;
    }

    /**
     * Returns the negation of the argument, throwing an exception if the
     * result overflows an {@code int}.
     *
     * @param a the value to negate
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int negateExact(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return -a;
    }

    /**
     * Returns the negation of the argument, throwing an exception if the
     * result overflows a {@code long}.
     *
     * @param a the value to negate
     * @return the result
     * @throws ArithmeticException if the result overflows a long
     * @since 1.8
     */
    public static long negateExact(long a) {
        if (a == Long.MIN_VALUE) {
            throw new ArithmeticException("long overflow");
        }

        return -a;
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an {@code int}.
     *
     * @param value the long value
     * @return the argument as an int
     * @throws ArithmeticException if the {@code argument} overflows an int
     * @since 1.8
     */
    public static int toIntExact(long value) {
        if ((int)value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)value;
    }

    /**
     * Returns the exact mathematical product of the arguments.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @since 9
     */
    public static long multiplyFull(int x, int y) {
        return (long) x * (long) y;
    }

    /**
     * Returns as a {@code long} the most significant 64 bits of the 128-bit
     * product of two 64-bit factors.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @since 9
     */
    public static long multiplyHigh(long x, long y) {
        if (x < 0 || y < 0) {
            // Use technique from section 8-2 of Henry S. Warren, Jr.,
            // Hacker's Delight (2nd ed.) (Addison Wesley, 2013), 173-174.
            long x1 = x >> 32;
            long x2 = x & 0xFFFFFFFFL;
            long y1 = y >> 32;
            long y2 = y & 0xFFFFFFFFL;
            long z2 = x2 * y2;
            long t = x1 * y2 + (z2 >>> 32);
            long z1 = t & 0xFFFFFFFFL;
            long z0 = t >> 32;
            z1 += x2 * y1;
            return x1 * y1 + z0 + (z1 >> 32);
        } else {
            // Use Karatsuba technique with two base 2^32 digits.
            long x1 = x >>> 32;
            long y1 = y >>> 32;
            long x2 = x & 0xFFFFFFFFL;
            long y2 = y & 0xFFFFFFFFL;
            long A = x1 * y1;
            long B = x2 * y2;
            long C = (x1 + x2) * (y1 + y2);
            long K = C - A - B;
            return (((B >>> 32) + K) >>> 32) + A;
        }
    }

    /**
     * Returns the largest (closest to positive infinity)
     * {@code int} value that is less than or equal to the algebraic quotient.
     * There is one special case, if the dividend is the
     * {@linkplain Integer#MIN_VALUE Integer.MIN_VALUE} and the divisor is {@code -1},
     * then integer overflow occurs and
     * the result is equal to the {@code Integer.MIN_VALUE}.
     * <p>
     * Normal integer division operates under the round to zero rounding mode
     * (truncation).  This operation instead acts under the round toward
     * negative infinity (floor) rounding mode.
     * The floor rounding mode gives different results than truncation
     * when the exact result is negative.
     * <ul>
     *   <li>If the signs of the arguments are the same, the results of
     *       {@code floorDiv} and the {@code /} operator are the same.  <br>
     *       For example, {@code floorDiv(4, 3) == 1} and {@code (4 / 3) == 1}.</li>
     *   <li>If the signs of the arguments are different,  the quotient is negative and
     *       {@code floorDiv} returns the integer less than or equal to the quotient
     *       and the {@code /} operator returns the integer closest to zero.<br>
     *       For example, {@code floorDiv(-4, 3) == -2},
     *       whereas {@code (-4 / 3) == -1}.
     *   </li>
     * </ul>
     * <p>
     *
     * @param x the dividend
     * @param y the divisor
     * @return the largest (closest to positive infinity)
     * {@code int} value that is less than or equal to the algebraic quotient.
     * @throws ArithmeticException if the divisor {@code y} is zero
     * @see #floorMod(int, int)
     * @see #floor(double)
     * @since 1.8
     */
    public static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

   /**
     * Returns the largest (closest to positive infinity)
     * {@code long} value that is less than or equal to the algebraic quotient.
     * There is one special case, if the dividend is the
     * {@linkplain Long#MIN_VALUE Long.MIN_VALUE} and the divisor is {@code -1},
     * then integer overflow occurs and
     * the result is equal to {@code Long.MIN_VALUE}.
     * <p>
     * Normal integer division operates under the round to zero rounding mode
     * (truncation).  This operation instead acts under the round toward
     * negative infinity (floor) rounding mode.
     * The floor rounding mode gives different results from truncation
     * when the exact result is negative.
     * <p>
     * For examples, see {@link #floorDiv(int, int)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the largest (closest to positive infinity)
     * {@code int} value that is less than or equal to the algebraic quotient.
     * @throws ArithmeticException if the divisor {@code y} is zero
     * @see #floorMod(long, int)
     * @see #floor(double)
     * @since 9
     */
    public static long floorDiv(long x, int y) {
        return floorDiv(x, (long) y);
    }

    /**
     * Returns the largest (closest to positive infinity)
     * {@code long} value that is less than or equal to the algebraic quotient.
     * There is one special case, if the dividend is the
     * {@linkplain Long#MIN_VALUE Long.MIN_VALUE} and the divisor is {@code -1},
     * then integer overflow occurs and
     * the result is equal to the {@code Long.MIN_VALUE}.
     * <p>
     * Normal integer division operates under the round to zero rounding mode
     * (truncation).  This operation instead acts under the round toward
     * negative infinity (floor) rounding mode.
     * The floor rounding mode gives different results than truncation
     * when the exact result is negative.
     * <p>
     * For examples, see {@link #floorDiv(int, int)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the largest (closest to positive infinity)
     * {@code long} value that is less than or equal to the algebraic quotient.
     * @throws ArithmeticException if the divisor {@code y} is zero
     * @see #floorMod(long, long)
     * @see #floor(double)
     * @since 1.8
     */
    public static long floorDiv(long x, long y) {
        long r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    /**
     * Returns the floor modulus of the {@code int} arguments.
     * <p>
     * The floor modulus is {@code x - (floorDiv(x, y) * y)},
     * has the same sign as the divisor {@code y}, and
     * is in the range of {@code -abs(y) < r < +abs(y)}.
     *
     * <p>
     * The relationship between {@code floorDiv} and {@code floorMod} is such that:
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * The difference in values between {@code floorMod} and
     * the {@code %} operator is due to the difference between
     * {@code floorDiv} that returns the integer less than or equal to the quotient
     * and the {@code /} operator that returns the integer closest to zero.
     * <p>
     * Examples:
     * <ul>
     *   <li>If the signs of the arguments are the same, the results
     *       of {@code floorMod} and the {@code %} operator are the same.  <br>
     *       <ul>
     *       <li>{@code floorMod(4, 3) == 1}; &nbsp; and {@code (4 % 3) == 1}</li>
     *       </ul>
     *   <li>If the signs of the arguments are different, the results differ from the {@code %} operator.<br>
     *      <ul>
     *      <li>{@code floorMod(+4, -3) == -2}; &nbsp; and {@code (+4 % -3) == +1} </li>
     *      <li>{@code floorMod(-4, +3) == +2}; &nbsp; and {@code (-4 % +3) == -1} </li>
     *      <li>{@code floorMod(-4, -3) == -1}; &nbsp; and {@code (-4 % -3) == -1 } </li>
     *      </ul>
     *   </li>
     * </ul>
     * <p>
     * If the signs of arguments are unknown and a positive modulus
     * is needed it can be computed as {@code (floorMod(x, y) + abs(y)) % abs(y)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the floor modulus {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException if the divisor {@code y} is zero
     * @see #floorDiv(int, int)
     * @since 1.8
     */
    public static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }

    /**
     * Returns the floor modulus of the {@code long} and {@code int} arguments.
     * <p>
     * The floor modulus is {@code x - (floorDiv(x, y) * y)},
     * has the same sign as the divisor {@code y}, and
     * is in the range of {@code -abs(y) < r < +abs(y)}.
     *
     * <p>
     * The relationship between {@code floorDiv} and {@code floorMod} is such that:
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * For examples, see {@link #floorMod(int, int)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the floor modulus {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException if the divisor {@code y} is zero
     * @see #floorDiv(long, int)
     * @since 9
     */
    public static int floorMod(long x, int y) {
        // Result cannot overflow the range of int.
        return (int) (x - floorDiv(x, y) * y);
    }

    /**
     * Returns the floor modulus of the {@code long} arguments.
     * <p>
     * The floor modulus is {@code x - (floorDiv(x, y) * y)},
     * has the same sign as the divisor {@code y}, and
     * is in the range of {@code -abs(y) < r < +abs(y)}.
     *
     * <p>
     * The relationship between {@code floorDiv} and {@code floorMod} is such that:
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * For examples, see {@link #floorMod(int, int)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the floor modulus {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException if the divisor {@code y} is zero
     * @see #floorDiv(long, long)
     * @since 1.8
     */
    public static long floorMod(long x, long y) {
        return x - floorDiv(x, y) * y;
    }

    /**
     * Returns the floating-point value adjacent to {@code d} in
     * the direction of negative infinity.  This method is
     * semantically equivalent to {@code nextAfter(d,
     * Double.NEGATIVE_INFINITY)}; however, a
     * {@code nextDown} implementation may run faster than its
     * equivalent {@code nextAfter} call.
     *
     * <p>Special Cases:
     * <ul>
     * <li> If the argument is NaN, the result is NaN.
     *
     * <li> If the argument is negative infinity, the result is
     * negative infinity.
     *
     * <li> If the argument is zero, the result is
     * {@code -Double.MIN_VALUE}
     *
     * </ul>
     *
     * @param d  starting floating-point value
     * @return The adjacent floating-point value closer to negative
     * infinity.
     * @since 1.8
     */
    public static double nextDown(double d) {
        if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY)
            return d;
        else {
            if (d == 0.0)
                return -Double.MIN_VALUE;
            else
                return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                                               ((d > 0.0d)?-1L:+1L));
        }
    }

    /**
     * Returns the floating-point value adjacent to {@code f} in
     * the direction of negative infinity.  This method is
     * semantically equivalent to {@code nextAfter(f,
     * Float.NEGATIVE_INFINITY)}; however, a
     * {@code nextDown} implementation may run faster than its
     * equivalent {@code nextAfter} call.
     *
     * <p>Special Cases:
     * <ul>
     * <li> If the argument is NaN, the result is NaN.
     *
     * <li> If the argument is negative infinity, the result is
     * negative infinity.
     *
     * <li> If the argument is zero, the result is
     * {@code -Float.MIN_VALUE}
     *
     * </ul>
     *
     * @param f  starting floating-point value
     * @return The adjacent floating-point value closer to negative
     * infinity.
     * @since 1.8
     */
    public static float nextDown(float f) {
        if (Float.isNaN(f) || f == Float.NEGATIVE_INFINITY)
            return f;
        else {
            if (f == 0.0f)
                return -Float.MIN_VALUE;
            else
                return Float.intBitsToFloat(Float.floatToRawIntBits(f) +
                                            ((f > 0.0f)?-1:+1));
        }
    }
}
