/*
 * Copyright (C) 2008 The Android Open Source Project
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

package java.math;

import dalvik.system.NativeAllocation;

/*
 * In contrast to BigIntegers this class doesn't fake two's complement representation.
 * Any Bit-Operations, including Shifting, solely regard the unsigned magnitude.
 * Moreover BigInt objects are mutable and offer efficient in-place-operations.
 */
final class BigInt extends NativeAllocation {

    @Override
    public String toString() {
        return this.decString();
    }

    long getNativeBIGNUM() {
        return this.nativePtr;
    }

    private void makeNew() {
        // TODO: It would be much nicer if we could call resetNativeAllocation
        // from native. Then we wouldn't need BN_freeFunction or BN_size.
        resetNativeAllocation(NativeBN.BN_new(), NativeBN.BN_freeFunction(), NativeBN.BN_size());
    }

    private void makeValid() {
        if (this.nativePtr == 0) {
            makeNew();
        }
    }

    private static BigInt newBigInt() {
        BigInt bi = new BigInt();
        bi.makeNew();
        return bi;
    }


    static int cmp(BigInt a, BigInt b) {
        return NativeBN.BN_cmp(a.nativePtr, b.nativePtr);
    }


    void putCopy(BigInt from) {
        this.makeValid();
        NativeBN.BN_copy(this.nativePtr, from.nativePtr);
    }

    BigInt copy() {
        BigInt bi = new BigInt();
        bi.putCopy(this);
        return bi;
    }


    void putLongInt(long val) {
        this.makeValid();
        NativeBN.putLongInt(this.nativePtr, val);
    }

    void putULongInt(long val, boolean neg) {
        this.makeValid();
        NativeBN.putULongInt(this.nativePtr, val, neg);
    }

    private NumberFormatException invalidBigInteger(String s) {
        throw new NumberFormatException("Invalid BigInteger: " + s);
    }

    void putDecString(String original) {
        String s = checkString(original, 10);
        this.makeValid();
        int usedLen = NativeBN.BN_dec2bn(this.nativePtr, s);
        if (usedLen < s.length()) {
            throw invalidBigInteger(original);
        }
    }

    void putHexString(String original) {
        String s = checkString(original, 16);
        this.makeValid();
        int usedLen = NativeBN.BN_hex2bn(this.nativePtr, s);
        if (usedLen < s.length()) {
            throw invalidBigInteger(original);
        }
    }

    /**
     * Returns a string suitable for passing to OpenSSL.
     * Throws if 's' doesn't match Java's rules for valid BigInteger strings.
     * BN_dec2bn and BN_hex2bn do very little checking, so we need to manually
     * ensure we comply with Java's rules.
     * http://code.google.com/p/android/issues/detail?id=7036
     */
    String checkString(String s, int base) {
        if (s == null) {
            throw new NullPointerException("s == null");
        }
        // A valid big integer consists of an optional '-' or '+' followed by
        // one or more digit characters appropriate to the given base,
        // and no other characters.
        int charCount = s.length();
        int i = 0;
        if (charCount > 0) {
            char ch = s.charAt(0);
            if (ch == '+') {
                // Java supports leading +, but OpenSSL doesn't, so we need to strip it.
                s = s.substring(1);
                --charCount;
            } else if (ch == '-') {
                ++i;
            }
        }
        if (charCount - i == 0) {
            throw invalidBigInteger(s);
        }
        boolean nonAscii = false;
        for (; i < charCount; ++i) {
            char ch = s.charAt(i);
            if (Character.digit(ch, base) == -1) {
                throw invalidBigInteger(s);
            }
            if (ch > 128) {
                nonAscii = true;
            }
        }
        return nonAscii ? toAscii(s, base) : s;
    }

    // Java supports non-ASCII decimal digits, but OpenSSL doesn't.
    // We need to translate the decimal digits but leave any other characters alone.
    // This method assumes it's being called on a string that has already been validated.
    private static String toAscii(String s, int base) {
        int length = s.length();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char ch = s.charAt(i);
            int value = Character.digit(ch, base);
            if (value >= 0 && value <= 9) {
                ch = (char) ('0' + value);
            }
            result.append(ch);
        }
        return result.toString();
    }

    void putBigEndian(byte[] a, boolean neg) {
        this.makeValid();
        NativeBN.BN_bin2bn(a, a.length, neg, this.nativePtr);
    }

    void putLittleEndianInts(int[] a, boolean neg) {
        this.makeValid();
        NativeBN.litEndInts2bn(a, a.length, neg, this.nativePtr);
    }

    void putBigEndianTwosComplement(byte[] a) {
        this.makeValid();
        NativeBN.twosComp2bn(a, a.length, this.nativePtr);
    }


    long longInt() {
        return NativeBN.longInt(this.nativePtr);
    }

    String decString() {
        return NativeBN.BN_bn2dec(this.nativePtr);
    }

    String hexString() {
        return NativeBN.BN_bn2hex(this.nativePtr);
    }

    byte[] bigEndianMagnitude() {
        return NativeBN.BN_bn2bin(this.nativePtr);
    }

    int[] littleEndianIntsMagnitude() {
        return NativeBN.bn2litEndInts(this.nativePtr);
    }

    int sign() {
        return NativeBN.sign(this.nativePtr);
    }

    void setSign(int val) {
        if (val > 0) {
            NativeBN.BN_set_negative(this.nativePtr, 0);
        } else {
            if (val < 0) NativeBN.BN_set_negative(this.nativePtr, 1);
        }
    }

    boolean twosCompFitsIntoBytes(int desiredByteCount) {
        int actualByteCount = (NativeBN.bitLength(this.nativePtr) + 7) / 8;
        return actualByteCount <= desiredByteCount;
    }

    int bitLength() {
        return NativeBN.bitLength(this.nativePtr);
    }

    boolean isBitSet(int n) {
        return NativeBN.BN_is_bit_set(this.nativePtr, n);
    }

    // n > 0: shift left (multiply)
    static BigInt shift(BigInt a, int n) {
        BigInt r = newBigInt();
        NativeBN.BN_shift(r.nativePtr, a.nativePtr, n);
        return r;
    }

    void shift(int n) {
        NativeBN.BN_shift(this.nativePtr, this.nativePtr, n);
    }

    void addPositiveInt(int w) {
        NativeBN.BN_add_word(this.nativePtr, w);
    }

    void multiplyByPositiveInt(int w) {
        NativeBN.BN_mul_word(this.nativePtr, w);
    }

    static int remainderByPositiveInt(BigInt a, int w) {
        return NativeBN.BN_mod_word(a.nativePtr, w);
    }

    static BigInt addition(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_add(r.nativePtr, a.nativePtr, b.nativePtr);
        return r;
    }

    void add(BigInt a) {
        NativeBN.BN_add(this.nativePtr, this.nativePtr, a.nativePtr);
    }

    static BigInt subtraction(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_sub(r.nativePtr, a.nativePtr, b.nativePtr);
        return r;
    }


    static BigInt gcd(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_gcd(r.nativePtr, a.nativePtr, b.nativePtr);
        return r;
    }

    static BigInt product(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_mul(r.nativePtr, a.nativePtr, b.nativePtr);
        return r;
    }

    static BigInt bigExp(BigInt a, BigInt p) {
        // Sign of p is ignored!
        BigInt r = newBigInt();
        NativeBN.BN_exp(r.nativePtr, a.nativePtr, p.nativePtr);
        return r;
    }

    static BigInt exp(BigInt a, int p) {
        // Sign of p is ignored!
        BigInt power = new BigInt();
        power.putLongInt(p);
        return bigExp(a, power);
        // OPTIONAL:
        // int BN_sqr(BigInteger r, BigInteger a, BN_CTX ctx);
        // int BN_sqr(BIGNUM *r, const BIGNUM *a,BN_CTX *ctx);
    }

    static void division(BigInt dividend, BigInt divisor, BigInt quotient, BigInt remainder) {
        long quot, rem;
        if (quotient != null) {
            quotient.makeValid();
            quot = quotient.nativePtr;
        } else {
            quot = 0;
        }
        if (remainder != null) {
            remainder.makeValid();
            rem = remainder.nativePtr;
        } else {
            rem = 0;
        }
        NativeBN.BN_div(quot, rem, dividend.nativePtr, divisor.nativePtr);
    }

    static BigInt modulus(BigInt a, BigInt m) {
        // Sign of p is ignored! ?
        BigInt r = newBigInt();
        NativeBN.BN_nnmod(r.nativePtr, a.nativePtr, m.nativePtr);
        return r;
    }

    static BigInt modExp(BigInt a, BigInt p, BigInt m) {
        // Sign of p is ignored!
        BigInt r = newBigInt();
        NativeBN.BN_mod_exp(r.nativePtr, a.nativePtr, p.nativePtr, m.nativePtr);
        return r;
    }


    static BigInt modInverse(BigInt a, BigInt m) {
        BigInt r = newBigInt();
        NativeBN.BN_mod_inverse(r.nativePtr, a.nativePtr, m.nativePtr);
        return r;
    }


    static BigInt generatePrimeDefault(int bitLength) {
        BigInt r = newBigInt();
        NativeBN.BN_generate_prime_ex(r.nativePtr, bitLength, false, 0, 0, 0);
        return r;
    }

    boolean isPrime(int certainty) {
        return NativeBN.BN_is_prime_ex(nativePtr, certainty, 0);
    }
}
