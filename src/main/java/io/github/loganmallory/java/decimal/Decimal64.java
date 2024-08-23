package io.github.loganmallory.java.decimal;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.util.Objects;

import static io.github.loganmallory.java.decimal.Decimal64.Internal.Data.*;
import static io.github.loganmallory.java.decimal.Decimal64.Internal.N_MANTISSA_BITS;
import static io.github.loganmallory.java.decimal.Decimal64.Internal.SPECIAL_EXPONENT;
import static java.nio.charset.StandardCharsets.US_ASCII;


public class Decimal64 {

    /** The bit pattern for a NaN Decimal, where mantissa=0, and exponent=-256. */
    public static final long NAN = makeUnsafe(0, SPECIAL_EXPONENT);

    /** The bit pattern for a -Inf Decimal, where mantissa=-2^54-1, and exponent=-256. */
    public static final long NEGATIVE_INFINITY = makeUnsafe(-((1L<<(N_MANTISSA_BITS-1))-1), SPECIAL_EXPONENT);

    /** The bit pattern for a +Inf Decimal, where mantissa=2^54-1, and exponent=-256. */
    public static final long POSITIVE_INFINITY = makeUnsafe((1L<<(N_MANTISSA_BITS-1))-1, SPECIAL_EXPONENT);

    /** The bit pattern for a zero-value Decimal, where mantissa=0, and exponent=0. */
    public static final long ZERO = 0;

    /** The bit pattern for a one-value Decimal, where mantissa=0, and exponent=0. */
    public static final long ONE = makeUnsafe(1, 0);

    /** The bit pattern for a two-value Decimal, where mantissa=0, and exponent=0. */
    public static final long TWO = makeUnsafe(2, 0);

    /**
     * A container class for internal methods and values.
     * Exposed as public for extensibility.
     * */
    public static class Internal {

        /**
         * The maximum number of base 10 digits a Decimal can store
         */
        public static final int PRECISION = 16;

        /**
         * The number of bits for the mantissa in the Decimal.
         * A value of 55 means we can have a mantissa in the range [-2^54, 2^54 - 1].
         */
        public static final int N_MANTISSA_BITS = 55;

        /**
         * The number of bits for the exponent in the Decimal.
         * A value of 9 means we can have an exponent in the range [-256, 255].
         */
        public static final int N_EXPONENT_BITS = 9;

        /**
         * The maximum positive value the mantissa can have.
         * Derived from the {@value N_MANTISSA_BITS} mantissa bits,
         * where (2^54 - 1) can represent some 17 digit numbers, and all 16 digit numbers.
         * We truncate to 16 digits for simplicity and easier/faster math.
         */
        public static final long MAX_MANTISSA = 9_999_999_999_999_999L;

        /**
         * The most negative value the mantissa can have.
         * See {@link Decimal64.Internal#MAX_MANTISSA}
         */
        public static final long MIN_MANTISSA = -9_999_999_999_999_999L;

        /**
         * The maximum positive value the exponent can have.
         * Derived from the {@value N_EXPONENT_BITS} exponent bits.
         * The value -256 is reserved for special values like NaN, -Inf, +Inf.
         */
        public static final int MAX_EXPONENT = 255;

        /**
         * The most negative value the mantissa can have.
         * See {@link Decimal64.Internal#MAX_EXPONENT}
         */
        public static final int MIN_EXPONENT = -255;

        /**
         * The reserved exponent value, used to indicate that the Decimal is a special value
         * E.g. NaN, -Inf, +Inf, and possibly others in the future.
         */
        public static final int SPECIAL_EXPONENT = MIN_EXPONENT - 1;

        /**
         * A bitmask for the exponent bits
         */
        public static final int EXPONENT_MASK_i32 = 0x1ff;

        /**
         * A constant used to extend the sign when retrieving the exponent
         */
        public static final int EXPONENT_SIGN_SHIFT_i32 = 32 - N_EXPONENT_BITS;

        /**
         * Container class for interacting with the Decimal bit pattern,
         * e.g. making a new 64-bit Decimal, getting/setting mantissa and exponent.
         */
        public static class Data {

            /**
             * Performs the bit masking and shifting required to compose a Decimal in 64 bits.
             * <h1>Unsafe: Does not validate inputs </h1>
             * <ul>
             *     <li>Mantissa should be in range [{@value MIN_MANTISSA}, {@value MAX_MANTISSA}] (<= {@value PRECISION} digits) </li>
             *     <li>Exponent should be in range [{@value SPECIAL_EXPONENT}, {@value MAX_EXPONENT}] </li>
             * </ul>
             *
             * @param mantissa The mantissa for the Decimal
             * @param exponent The exponent for the Decimal
             * @return A 64 bit Decimal representing: mantissa * 10^-exponent
             */
            public static long makeUnsafe(long mantissa, int exponent) {
                return (mantissa << 9) | (exponent & EXPONENT_MASK_i32);
            }

            /**
             * Returns a new Decimal with the given mantissa.
             * <h1>Unsafe: Does not validate inputs</h1>
             */
            public static long setMantissa(long decimal, long mantissa) {
                decimal &= EXPONENT_MASK_i32; // zero out mantissa
                decimal |= (mantissa << N_EXPONENT_BITS);
                return decimal;
            }

            /**
             * Returns a new Decimal with the given exponent.
             * <h1>Unsafe: Does not validate inputs</h1>
             */
            public static long setExponent(long decimal, int exponent) {
                decimal &= ~EXPONENT_MASK_i32; // zero out exponent
                decimal |= (exponent & EXPONENT_MASK_i32);
                return decimal;
            }

            /**
             * Get the mantissa from a Decimal's bits.
             */
            public static long getMantissa(long decimal) {
                return decimal >> N_EXPONENT_BITS;
            }

            /**
             * Get the exponent from a Decimal's bits.
             */
            public static int getExponent(long decimal) {
                int exp = (int) (decimal & EXPONENT_MASK_i32);
                // extend the sign bit
                exp = ((exp << EXPONENT_SIGN_SHIFT_i32) >> EXPONENT_SIGN_SHIFT_i32);
                return exp;
            }

            public static boolean isFinite(long decimal) {
                // shortcuts getExponent(..) to check unsigned exponent
                return (decimal & EXPONENT_MASK_i32) != 256;
            }
        }

        public static class Debug {

            public static String triplet(long decimal) {
                return "(" + decimal + ", " + getMantissa(decimal) + ", " + getExponent(decimal) + ")";
            }

            public static void validate(long decimal) {
                if (getExponent(decimal) == -256) {
                    if (decimal != NAN && decimal != NEGATIVE_INFINITY && decimal != POSITIVE_INFINITY) {
                        throw new RuntimeException("Decimal " + triplet(decimal) + " has special exponent -256 but isn't NaN or -/+ Infinity");
                    }
                } else {
                    validateFinite(decimal);
                }
            }

            public static void validateFinite(long decimal) {
                // check mantissa
                long mantissa = getMantissa(decimal);
                if (mantissa < MIN_MANTISSA || mantissa > MAX_MANTISSA) {
                    throw new RuntimeException("Decimal " + triplet(decimal) + " mantissa is out of range [" + MIN_MANTISSA + ", " + MAX_MANTISSA + "]");
                }
                if (mantissa != 0 && mantissa % 10 == 0) {
                    throw new RuntimeException("Decimal " + triplet(decimal) + " mantissa has trailing zeros");
                }

                // check exponent
                int exponent = getExponent(decimal);
                if (exponent < MIN_EXPONENT || exponent > MAX_EXPONENT) {
                    throw new RuntimeException("Decimal " + triplet(decimal) + " exponent is out of range [" + MIN_EXPONENT + ", " + MAX_EXPONENT + "]");
                }
                if (mantissa == 0 && exponent != 0) {
                    throw new RuntimeException("Decimal " + triplet(decimal) + " is ambiguous, exponent should be zero");
                }
            }
        }

        public static class Convert {

            public static class Parts {

                /**
                 * Creates a Decimal from the given mantissa and exponent.
                 * The value will overflow to -/+ Inf, and underflow to 0.
                 */
                public static long fromParts(long mantissa, int exponent) {
                    // fast path zero
                    if (mantissa == 0) {
                        return ZERO;
                    }

                    // catches the majority of real world calls
                    if (mantissa <= MAX_MANTISSA && mantissa >= MIN_MANTISSA && exponent >= -240 && exponent <= MAX_EXPONENT) {
                        // safety: mantissa <= 16 digits and exponent won't overflow 255
                        return fromPartsFiniteLessThan16DigitsNoFlowNoZero(mantissa, exponent);
                    }

                    // slower, but handles all cases
                    return fromPartsPossibleFlowNoZero(mantissa, exponent);
                }


                public static long fromPartsFiniteLessThan16DigitsNoFlowNoZero(long mantissa, int exponent) {
                    assert FastMath.nDigits(mantissa) <= 16 : "mantissa must be <= 16 digits";
                    assert mantissa != 0 : "mantissa must not be 0";
                    assert exponent <= 255 : "mantissa must be <= 255";
                    assert (exponent - FastMath.log10I64(mantissa)) >= -255 : "exponent - log10(mantissa) must be >= -255";

                    // strip trailing zeros (at most 15 iterations because mantissa is <= 16 digits)
                    for (int i = 0; mantissa % 10 == 0 && i < 15; i++) {
                        mantissa /= 10;
                        exponent -= 1;
                    }

                    // safety: mantissa <= 16 digits, exponent is in range [-255, 255]
                    assert FastMath.nDigits(mantissa) <= 16 : "mantissa must be <= 16 digits";
                    assert exponent >= -255 && exponent <= 255 : "exponent must be in range [-255, 255]";
                    return makeUnsafe(mantissa, exponent);
                }

                public static long fromPartsPossibleFlowNoZero(long mantissa, int exponent) {
                    if (exponent < MIN_EXPONENT) {
                        // overflow guaranteed
                        return mantissa > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                    }

                    int mantissa_n_digits = FastMath.nDigits(mantissa);

                    // trim mantissa to 16 digits
                    int drop = mantissa_n_digits - PRECISION; // [-15, 3]
                    if (drop > 0) {
                        long div = FastMath.i64TenToThe(drop);
                        int remainder = Math.abs((int) (mantissa % div)); // at most 3 digits
                        mantissa /= div;
                        exponent -= drop;

                        // apply half-even rounding
                        int half = (int) (5 * div / 10);
                        if (remainder > half || (remainder == half && mantissa % 2 != 0)) {
                            mantissa += mantissa > 0 ? 1 : -1;
                        }
                        mantissa_n_digits -= drop;
                    }

                    // strip trailing zeros - at most 15 iterations because mantissa is <= 16 digits
                    if (mantissa != 0) {
                        for (int i = 0; mantissa % 10 == 0 && i < 15; i++) {
                            mantissa /= 10;
                            exponent -= 1;
                            mantissa_n_digits--;
                        }
                    }

                    if (exponent < MIN_EXPONENT) {
                        // overflow
                        return mantissa > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                    }

                    if (exponent > MAX_EXPONENT) {
                        // possible underflow
                        drop = (exponent - MAX_EXPONENT); // <= 16

                        if (drop >= mantissa_n_digits) {
                            return ZERO; // underflow
                        }
                        long div = FastMath.i64TenToThe(drop);
                        long remainder = Math.abs(mantissa % div);
                        mantissa /= div;
                        exponent -= drop;

                        // apply half-even rounding
                        long half = 5 * div / 10;
                        if (remainder > half || (remainder == half && mantissa % 2 != 0)) {
                            mantissa += mantissa > 0 ? 1 : -1;
                        }

                        // strip trailing zeros
                        for (int i = 0; mantissa % 10 == 0 && i < 16; i++) { // or i < (mantissa_n_digits - drop)
                            mantissa /= 10;
                            exponent -= 1;
                        }
                    }

                    // safety: mantissa != 0, is <= 16 digits, and exponent in range [-255, 255]
                    assert mantissa != 0 : "mantissa must not be 0";
                    assert FastMath.nDigits(mantissa) <= 16 : "mantissa must be <= 16 digits";
                    assert exponent >= -255 && exponent <= 255 : "exponent must be in range [-255, 255]";
                    return makeUnsafe(mantissa, exponent);
                }
            }

            public static class I64 {

                public static long fromI64(long integer) {
                    return fromParts(integer, 0);
                }
            }

            public static class I32 {

                public static long fromI32(int integer) {
                    return fromParts(integer, 0);
                }
            }

            public static class F64 {

                public static final double MIN_REPRESENTABLE_F64 = 1e-255;
                public static final double MAX_REPRESENTABLE_F64 = 9_999_999_999_999_999e255;

                public static long fromF64(double flt) {
                    if (!Double.isFinite(flt)) {
                        return fromF64NonFinite(flt);
                    }
                    return fromF64Finite(flt);

                }

                public static long fromF64NonFinite(double flt) {
                    if (flt != flt) {
                        return NAN;
                    }
                    return flt > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                }

                public static long fromF64Finite(double flt) {
                    int sign = 1;
                    if (flt < 0) {
                        sign = -1;
                        flt = -flt;
                    }

                    if (flt > MAX_REPRESENTABLE_F64) {
                        return sign > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                    }

                    if (flt < MIN_REPRESENTABLE_F64) {
                        return ZERO;
                    }

                    // TODO check subnormal, use f64 mantissa and exponent
                    int rem = PRECISION;
                    if (flt >= 1) {
                        rem -= (int) Math.ceil(Math.log10(flt));
                        rem = Math.max(0, rem);
                    }
                    long mantissa = ((long) flt) * FastMath.i64TenToThe(rem);
                    mantissa += ((long) (flt * FastMath.f64TenToThe(rem))) - mantissa;
                    return fromParts(mantissa * sign, rem);
                }

                public static double toF64(long decimal) {
                    if (!isFinite(decimal)) {
                        return toF64NonFinite(decimal);
                    }
                    return toF64Finite(decimal);
                }

                public static double toF64NonFinite(long decimal) {
                    if (decimal == NAN) {
                        return Double.NaN;
                    }
                    return decimal > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }

                public static double toF64Finite(long decimal) {
                    long mantissa = getMantissa(decimal);
                    int exponent = getExponent(decimal);

                    if (exponent == 0) {
                        // decimal is an integer
                        return mantissa;
                    }

                    var v = (double) mantissa;
                    if ((long) v == mantissa) {
                        if (exponent > 0 && exponent < FastMath.F64_TEN_TO_THE.length) {
                            return mantissa / FastMath.f64TenToThe(exponent);
                        }
                        if (exponent < 0 && exponent > -FastMath.F64_TEN_TO_THE.length) {
                            return mantissa * FastMath.f64TenToThe(-exponent);
                        }
                    }

                    return BigDecimal.valueOf(mantissa, exponent).doubleValue();
                }
            }

            public static class BigDec {
                public static long fromBigDecimal(@NotNull BigDecimal bigDecimal) {
                    if (bigDecimal.precision() > PRECISION) {
                        bigDecimal = bigDecimal.round(MathContext.DECIMAL64); // TODO: this is amazingly slow
                    }
                    return fromParts(bigDecimal.unscaledValue().longValue(), bigDecimal.scale());
                }

                public static @NotNull BigDecimal toBigDecimal(long decimal) {
                    if (!isFinite(decimal)) {
                        throw new RuntimeException("Can't convert non-finite decimal " + triplet(decimal) + " to BigDecimal");
                    }
                    return BigDecimal.valueOf(getMantissa(decimal), getExponent(decimal));
                }
            }

            public static class Str {

                public static long fromString(@NotNull CharSequence str) {
                    return Decimal64.Internal.Convert.Str.fromString(str, 0, str.length());
                }

                public static long fromString(@NotNull CharSequence str, int offset) {
                    return Decimal64.Internal.Convert.Str.fromString(str, offset, str.length() - offset);
                }

                /** Wrapper for ByteBuffer that implements CharSequence */
                public static class ByteBufferCharSequence implements CharSequence {
                    public ByteBuffer buf;

                    public ByteBufferCharSequence(ByteBuffer buf) {
                        this.buf = buf;
                    }

                    @Override
                    public int length() {
                        return buf.remaining();
                    }

                    @Override
                    public char charAt(int index) {
                        return (char) buf.get(buf.position() + index);
                    }

                    @Override
                    public @NotNull String subSequence(int start, int end) {
                        if (buf.hasArray()) {
                            return new String(buf.array(), buf.position() + start, end - start, US_ASCII);
                        }
                        byte[] out = new byte[end - start];
                        buf.get(buf.position() + start, out);
                        return new String(out, US_ASCII);
                    }

                    @Override
                    public @NotNull String toString() {
                        return subSequence(0, buf.remaining());
                    }
                }

                public static long fromString(@NotNull ByteBuffer str) {
                    var charSeq = new ByteBufferCharSequence(str);
                    return Decimal64.Internal.Convert.Str.fromString(charSeq, 0, str.limit() - str.position());
                }

                public static long fromString(@NotNull CharSequence str, int offset, int len) {
                    // throw on invalid indexes
                    Objects.checkFromToIndex(offset, offset + len, str.length());

                    int i = offset;
                    int n = offset + len;
                    char c = '\0';

                    // ignore leading whitespace
                    while (i < n && Character.isWhitespace(c = str.charAt(i))) {
                        i++;
                    }

                    // was it all blank?
                    if (i == n) {
                        throw new NumberFormatException("Invalid decimal string: '" + str.subSequence(offset, offset + len) + "'");
                    }
                    int head = i;

                    // ignore trailing whitespace (we know there is at least 1 non-whitespace char)
                    while (n > i+1 && Character.isWhitespace(str.charAt(n - 1))) {
                        n--;
                    }
                    int tail = n;

                    // check for nan
                    if (c == 'N'){
                        if (bytesFinishNan(str, i+1, n)) {
                            return NAN;
                        }
                        throw new NumberFormatException("Invalid decimal string: '" + str.subSequence(head, tail) + "'");
                    }

                    // check for sign
                    int sign = 1;
                    switch (c) {
                        case '-':
                            sign = -1;
                            // fallthrough
                        case '+':
                            i++;
                    }

                    // anything left?
                    if (i == n) {
                        throw new NumberFormatException("Invalid decimal string: '" + str.subSequence(offset, offset + len) + "'");
                    }

                    // check for infinity
                    if ((c = str.charAt(i)) == 'I') {
                        if (bytesFinishInfinity(str, i+1, n)) {
                            return sign > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                        }
                        throw new NumberFormatException("Invalid decimal string: '" + str.subSequence(head, tail) + "'");
                    }

                    // ignore leading zeros
                    while (i < n && (c = str.charAt(i)) == '0') {
                        i++;
                    }

                    // string was all zeros
                    if (i == n) {
                        return ZERO;
                    }

                    // TODO use custom parsing
                    return Decimal64.Internal.Convert.Str.fromStringFinite(str, head, tail - head);
                }

                public static long fromStringFinite(@NotNull CharSequence str, int offset, int len) {
                    var string = String.valueOf(str.subSequence(offset, offset + len));
                    BigDecimal bigDecimal;
                    try {
                        bigDecimal = new BigDecimal(string, MathContext.DECIMAL64);
                    } catch (Exception e) {
                        throw new NumberFormatException("Invalid decimal string: '" + string + "'");
                    }
                    return Decimal64.Internal.Convert.BigDec.fromBigDecimal(bigDecimal);
                }

                public static boolean bytesFinishNan(@NotNull CharSequence str, int i, int n) {
                    // assumes buf[i-1] == 'N'
                    return n - i == 2 && str.charAt(i) == 'a' && str.charAt(i+1) == 'N';
                }

                public static boolean bytesFinishInfinity(@NotNull CharSequence str, int i, int n) {
                    // assumes buf[i-1] == 'I'
                    return n - i == 7 && str.charAt(i) == 'n' && str.charAt(i+1) == 'f' && str.charAt(i+2) == 'i'
                                && str.charAt(i+3) == 'n' && str.charAt(i+4) == 'i' && str.charAt(i+5) == 't'
                                && str.charAt(i+6) == 'y';
                }

                public static @NotNull String toString(long decimal) {
                    ByteBuffer buf;

                    if (isFinite(decimal)) {
                        long mantissa = getMantissa(decimal);
                        int exponent = getExponent(decimal);
                        int size = FastMath.nDigits(mantissa) + 2 + Math.abs(exponent); // digits + sign + point + place on number line
                        buf = ByteBuffer.allocate(size);
                    } else {
                        buf = ByteBuffer.allocate(9);
                    }

                    toString(decimal, buf);

                    return new String(buf.array(), 0, buf.position(), US_ASCII);
                }

                public static final byte[] NAN_ASCII = new byte[]{'N', 'a', 'N'};
                public static final byte[] POSITIVE_INFINITY_ASCII = new byte[]{'+', 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y'};
                public static final byte[] NEGATIVE_INFINITY_ASCII = new byte[]{'-', 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y'};


                public static void toString(long decimal, @NotNull ByteBuffer out) {
                    if (!isFinite(decimal)) {
                        // TODO: could optimize with putLong etc.
                        if (decimal == NAN) {
                            out.put(NAN_ASCII);
                        } else if (decimal == NEGATIVE_INFINITY) {
                            out.put(NEGATIVE_INFINITY_ASCII);
                        } else {
                            out.put(POSITIVE_INFINITY_ASCII);
                        }
                        return;
                    }

                    long mantissa = getMantissa(decimal);
                    int exponent = getExponent(decimal);

                    if (mantissa == 0) {
                        // fast path zero
                        out.put((byte) '0');
                        return;
                    }

                    if (exponent == 0) {
                        // just an integer, e.g. 123
                        FastEncoding.write_i64(mantissa, out);
                        return;
                    }

                    if (exponent < 0) {
                        // also an integer, but with trailing zeros, e.g. 31400
                        FastEncoding.write_i64(mantissa, out);
                        for (int i = 0; i < -exponent; i++) {
                            out.put((byte) '0');
                        }
                        return;
                    }

                    // some digits are on right side of decimal point, e.g. 12.345 or 0.0123

                    int n_digits = FastMath.nDigits(mantissa);
                    if (n_digits > exponent) {
                        // digits are split across left and right side, e.g. 12.345
                        // exponent is in range [1, 15]
                        long pow = FastMath.i64TenToThe(exponent);
                        long left = mantissa / pow;
                        long right = Math.abs(mantissa - (left * pow));
                        FastEncoding.write_i64(left, out);
                        out.put((byte) '.');
                        int n_leading_zeros = exponent - FastMath.nDigits(right);
                        for (int i = 0; i < n_leading_zeros && i < 14; i++) {
                            // mantissa <= 16 digits, so at most 14 zeros
                            out.put((byte) '0');
                        }
                        FastEncoding.write_i64(right, out);
                        return;
                    }

                    // all digits are all on right side, e.g. 0.0123
                    if (mantissa < 0) {
                        out.put((byte) '-');
                    }
                    out.put((byte) '0');
                    out.put((byte) '.');
                    int n_leading_zeros = exponent - n_digits;
                    for (int i = 0; i < n_leading_zeros; i++) {
                        out.put((byte) '0');
                    }
                    FastEncoding.write_i64(Math.abs(mantissa), out);
                }
            }
        }

        public static class Compare {

            public static class DecimalVsDecimal {

                public static int compare(long decimalA, long decimalB) {
                    if (!isFinite(decimalA) || !isFinite(decimalB)) {
                        return compareNonFinite(decimalA, decimalB);
                    }
                    return compareFinite(decimalA, decimalB);
                }

                public static int compareNonFinite(long decimalA, long decimalB) {
                    if (decimalA == decimalB) {
                        return 0;
                    }

                    // NaN is always greater
                    if (decimalA == NAN) {
                        return 1;
                    }
                    if (decimalB == NAN) {
                        return -1;
                    }

                    // at least one of the decimals is infinity
                    // safety: the mantissa for -/+ infinity are signed, so raw values can be compared
                    return Long.compare(decimalA, decimalB);
                }

                public static int compareFinite(long decimalA, long decimalB) {
                    if (decimalA == decimalB) {
                        return 0;
                    }

                    long a_mantissa = getMantissa(decimalA);
                    int a_exponent  = getExponent(decimalA);
                    long b_mantissa = getMantissa(decimalB);
                    int b_exponent  = getExponent(decimalB);

                    // if zeros, positive vs. negative, or same exponent, we can just compare mantissas
                    if (a_mantissa == 0 || b_mantissa == 0 || (a_mantissa < 0) != (b_mantissa < 0) || a_exponent == b_exponent) {
                        return Long.compare(a_mantissa, b_mantissa);
                    }

                    // safety: a_exponent != b_exponent, a and b are same sign, neither are zero,
                    // mantissas are <= 16 digits, and exponents are in range [-255, 255]
                    return compareFiniteSignedUnsafe(a_mantissa, a_exponent, b_mantissa, b_exponent);
                }

                public static int compareFiniteSignedUnsafe(long a_mantissa, int a_exponent, long b_mantissa, int b_exponent) {
                    assert a_exponent != b_exponent: "a_exponent and b_exponent must be different";
                    assert FastMath.sameSign(a_mantissa, b_mantissa): "a_mantissa and b_mantissa must have same sign";
                    assert a_mantissa != 0 && b_mantissa != 0: "a_mantissa and b_mantissa must not be zero";
                    assert FastMath.nDigits(a_mantissa) <= 16: "n_digits(a_mantissa) must be <= 16";
                    assert FastMath.nDigits(b_mantissa) <= 16: "n_digits(b_mantissa) must be <= 16";
                    assert a_exponent >= -255 && a_exponent <= 255: "a_exponent must be in range [-255, 255]";
                    assert b_exponent >= -255 && b_exponent <= 255: "b_exponent must be in range [-255, 255]";

                    if (a_mantissa >= 0) {
                        return compareFiniteUnsignedUnsafe(a_mantissa, a_exponent, b_mantissa, b_exponent);
                    } else {
                        return compareFiniteUnsignedUnsafe(-a_mantissa, a_exponent, -b_mantissa, b_exponent) * -1;
                    }
                }

                public static int compareFiniteUnsignedUnsafe(long a_mantissa, int a_exponent, long b_mantissa, int b_exponent) {
                    assert a_exponent != b_exponent: "a_exponent and b_exponent must be different";
                    assert FastMath.sameSign(a_mantissa, b_mantissa): "a_mantissa and b_mantissa must have same sign";
                    assert a_mantissa != 0 && b_mantissa != 0: "a_mantissa and b_mantissa must not be zero";
                    assert FastMath.nDigits(a_mantissa) <= 16: "n_digits(a_mantissa) must be <= 16";
                    assert FastMath.nDigits(b_mantissa) <= 16: "n_digits(b_mantissa) must be <= 16";
                    assert a_exponent >= -255 && a_exponent <= 255: "a_exponent must be in range [-255, 255]";
                    assert b_exponent >= -255 && b_exponent <= 255: "b_exponent must be in range [-255, 255]";
                    assert a_mantissa >= 0 && b_mantissa >= 0: "a_mantissa and b_mantissa must be >= 0";

                    int a_n_digits = FastMath.nDigits(a_mantissa);
                    int b_n_digits = FastMath.nDigits(b_mantissa);

                    // if same number of digits, just compare exponents
                    if (a_n_digits == b_n_digits) {
                        return Integer.compare(b_exponent, a_exponent);
                    }

                    // get position (where the number starts on the number line)
                    int a_pos = a_n_digits - 1 - a_exponent;
                    int b_pos = b_n_digits - 1 - b_exponent;

                    if (a_pos != b_pos) {
                        return Integer.compare(a_pos, b_pos);
                    }
                    // else same starting position on number line

                    // upscale one of the mantissas to compare.
                    // diff between a_exponent and y_exponent is in range [1, 15]
                    // and `a` and `b` start at the same place on the number line,
                    // so up-scaling here is safe, won't overflow
                    if (a_exponent > b_exponent) {
                        b_mantissa *= FastMath.i64TenToThe(a_exponent - b_exponent);
                    } else {
                        a_mantissa *= FastMath.i64TenToThe(b_exponent - a_exponent);
                    }

                    // same exponent now, safe to compare
                    return Long.compare(a_mantissa, b_mantissa);
                }
            }

            public static class DecimalVsZero {

                public static boolean ltZero(long decimal) {
                    // safety: sign of data value is same as sign of mantissa, NaN value is 256, +/- Inf are signed
                    return decimal < 0;
                }

                public static boolean leZero(long decimal) {
                    // safety: sign of data value is same as sign of mantissa, NaN value is 256, +/- Inf are signed
                    return decimal <= 0;
                }

                public static boolean gtZero(long decimal) {
                    // safety: sign of data value is same as sign of mantissa, NaN mantissa is 256, +/- Inf are signed
                    return decimal > 0;
                }

                public static boolean geZero(long decimal) {
                    // safety: sign of data value is same as sign of mantissa, NaN mantissa is 256, +/- Inf are signed
                    return decimal >= 0;
                }
            }
        }

        public static class Maths {

            public static long abs(long decimal) {
                if (Internal.Compare.DecimalVsZero.ltZero(decimal)) {
                    return negate(decimal);
                }
                return decimal;
            }

            public static long negate(long decimal) {
                // safety: mantissa is <= 16 digits so won't overflow,
                //         -/+ Inf are symmetric so negating works,
                //         and NaN mantissa is 0 so fine to negate that too.
                return setMantissa(decimal, -getMantissa(decimal));
            }

            public static class Add {

                public static long add(long decimalA, long decimalB) {
                    if (!isFinite(decimalA) || !isFinite(decimalB)) {
                        return addNonFinite(decimalA, decimalB);
                    }
                    return addFinite(decimalA, decimalB);
                }

                public static long addNonFinite(long decimalA, long decimalB) {
                    if (decimalA == NAN || decimalB == NAN) {
                        // NaN + x
                        return NAN;
                    }

                    if (getExponent(decimalA) == getExponent(decimalB)) {
                        // both a and b are infinite
                        // inf + inf = inf
                        // -inf + inf = NaN
                        return decimalA == decimalB ? decimalA : NAN;
                    }

                    // only one of a or b is infinite
                    return isFinite(decimalA) ? decimalB : decimalA;
                }

                public static long addFinite(long decimalA, long decimalB) {
                    // fast-path check for zeros
                    if ((decimalA | decimalB) == ZERO) {
                        return ZERO;
                    }

                    long a_mantissa = getMantissa(decimalA);
                    long b_mantissa = getMantissa(decimalB);
                    int a_exponent = getExponent(decimalA);
                    int b_exponent = getExponent(decimalB);

                    int exponent_diff = a_exponent - b_exponent;

                    if (exponent_diff == 0) {
                        // same exponents, just sum the mantissas
                        // safety: summing two 16 digit numbers won't overflow an i64
                        return fromParts(a_mantissa + b_mantissa, a_exponent);
                    }

                    // fall back to BigDecimal
                    var bigDecimalA   = BigDecimal.valueOf(a_mantissa, a_exponent);
                    var bigDecimalB   = BigDecimal.valueOf(b_mantissa, b_exponent);
                    var bigDecimalSum = bigDecimalA.add(bigDecimalB, MathContext.DECIMAL64);
                    return Internal.Convert.BigDec.fromBigDecimal(bigDecimalSum);
                }
            }

            public static class Sub {

                public static long sub(long decimalA, long decimalB) {
                    if (!isFinite(decimalA) || !isFinite(decimalB)) {
                        return subNonFinite(decimalA, decimalB);
                    }
                    return subFinite(decimalA, decimalB);
                }

                public static long subNonFinite(long decimalA, long decimalB) {
                    // logic is the same as add
                    return Internal.Maths.Add.addNonFinite(decimalA, decimalB);
                }

                public static long subFinite(long decimalA, long decimalB) {
                    // a - b = a + (-b)
                    return Internal.Maths.Add.addFinite(decimalA, negate(decimalB));
                }
            }

            public static class Mul {

                public static long mul(long decimalA, long decimalB) {
                    if (!isFinite(decimalA) || !isFinite(decimalB)) {
                        return mulNonFinite(decimalA, decimalB);
                    }
                    return mulFinite(decimalA, decimalB);
                }

                public static long mulNonFinite(long decimalA, long decimalB) {
                    if (getMantissa(decimalA) == 0 || getMantissa(decimalB) == 0) {
                        // NaN * x, inf * 0
                        return NAN;
                    }

                    if (getExponent(decimalA) == getExponent(decimalB)) {
                        // both a and b are infinite
                        // -inf * -inf = inf
                        // inf * inf = inf
                        return decimalA == decimalB ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                    }

                    // only one of a or b is infinite
                    return FastMath.sameSign(decimalA, decimalB) ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                }

                public static long mulFinite(long decimalA, long decimalB) {
                    // fast path check for zero
                    if (decimalA == ZERO || decimalB == ZERO) {
                        return ZERO;
                    }

                    long a_mantissa = getMantissa(decimalA);
                    long b_mantissa = getMantissa(decimalB);
                    int a_exponent = getExponent(decimalA);
                    int b_exponent = getExponent(decimalB);

                    // i64 can hold all 18 digit numbers
                    if (FastMath.nDigits(a_mantissa) + FastMath.nDigits(b_mantissa) <= 18) {
                        long product = a_mantissa * b_mantissa;
                        int exponent = a_exponent + b_exponent;
                        return fromParts(product, exponent);
                    } // TODO saturating mul instead of log check

                    var bigDecimalA       = BigDecimal.valueOf(a_mantissa, a_exponent);
                    var bigDecimalB       = BigDecimal.valueOf(b_mantissa, b_exponent);
                    var bigDecimalProduct = bigDecimalA.multiply(bigDecimalB, MathContext.DECIMAL64);
                    return Internal.Convert.BigDec.fromBigDecimal(bigDecimalProduct);
                }
            }

            public static class Div {

                public static long div(long decimalA, long decimalB) {
                    if (!isFinite(decimalA) || !isFinite(decimalB)) {
                        return divNonFinite(decimalA, decimalB);
                    }
                    return divFinite(decimalA, decimalB);
                }

                public static long divNonFinite(long decimalA, long decimalB) {
                    if (decimalA == NAN || decimalB == NAN || decimalB == ZERO) {
                        // nan / x, x / nan, x / 0
                        return NAN;
                    }

                    if (decimalA == ZERO) {
                        // x / 0
                        return ZERO;
                    }

                    if (!isFinite(decimalA) && !isFinite(decimalB)) {
                        // inf / inf
                        return NAN;
                    }

                    if (!isFinite(decimalB)) {
                        // x / inf
                        return ZERO;
                    }

                    // catches inf / x
                    return decimalA;
                }

                public static long divFinite(long decimalA, long decimalB) {
                    if (decimalB == ZERO) {
                        // x / 0
                        return NAN;
                    }

                    if (decimalA == ZERO) {
                        // 0 / x
                        return ZERO;
                    }

                    if (decimalB == ONE) {
                        return decimalA;
                    }

                    var bigDecimalA        = Internal.Convert.BigDec.toBigDecimal(decimalA);
                    var bigDecimalB        = Internal.Convert.BigDec.toBigDecimal(decimalB);
                    var bigDecimalQuotient = bigDecimalA.divide(bigDecimalB, MathContext.DECIMAL64);
                    return Internal.Convert.BigDec.fromBigDecimal(bigDecimalQuotient);
                }
            }
        }
    }

    public static @NotNull String triplet(long decimal) {
        return Internal.Debug.triplet(decimal);
    }

    public static void validate(long decimal) {
        Internal.Debug.validate(decimal);
    }

    public static void validateFinite(long decimal) {
        Internal.Debug.validateFinite(decimal);
    }

    public static long fromParts(long mantissa, int exponent) {
        return Internal.Convert.Parts.fromParts(mantissa, exponent);
    }

    public static long fromI64(long integer) {
        return Internal.Convert.I64.fromI64(integer);
    }
    public static long fromI32(int integer) {
        return Internal.Convert.I32.fromI32(integer);
    }

    public static long fromF64(double flt) {
        return Internal.Convert.F64.fromF64(flt);
    }

    public static double toF64(long decimal) {
        return Internal.Convert.F64.toF64(decimal);
    }

    public static long fromBigDecimal(@NotNull BigDecimal bigDecimal) {
        return Internal.Convert.BigDec.fromBigDecimal(bigDecimal);
    }

    public static @NotNull BigDecimal toBigDecimal(long decimal) {
        return Internal.Convert.BigDec.toBigDecimal(decimal);
    }

    public static long fromString(@NotNull String str) {
        return Internal.Convert.Str.fromString(str);
    }

    public static @NotNull String toString(long decimal) {
        return Internal.Convert.Str.toString(decimal);
    }

    public static long fromString(@NotNull String str, int offset) {
        return Internal.Convert.Str.fromString(str, offset);
    }

    public static long fromString(@NotNull String str, int offset, int len) {
        return Internal.Convert.Str.fromString(str, offset, len);
    }

    public static long fromString(@NotNull ByteBuffer in) {
        return Internal.Convert.Str.fromString(in);
    }

    public static int compare(long decimalA, long decimalB) {
        return Internal.Compare.DecimalVsDecimal.compare(decimalA, decimalB);
    }

    public static boolean ltZero(long decimal) {
        return Internal.Compare.DecimalVsZero.ltZero(decimal);
    }

    public static boolean leZero(long decimal) {
        return Internal.Compare.DecimalVsZero.leZero(decimal);
    }

    public static boolean gtZero(long decimal) {
        return Internal.Compare.DecimalVsZero.gtZero(decimal);
    }

    public static boolean geZero(long decimal) {
        return Internal.Compare.DecimalVsZero.geZero(decimal);
    }

    public static long abs(long decimal) {
        return Internal.Maths.abs(decimal);
    }

    public static long negate(long decimal) {
        return Internal.Maths.negate(decimal);
    }

    public static long add(long decimalA, long decimalB) {
        return Internal.Maths.Add.add(decimalA, decimalB);
    }

    public static long sub(long decimalA, long decimalB) {
        return Internal.Maths.Sub.sub(decimalA, decimalB);
    }

    public static long mul(long decimalA, long decimalB) {
        return Internal.Maths.Mul.mul(decimalA, decimalB);
    }

    public static long div(long decimalA, long decimalB) {
        return Internal.Maths.Div.div(decimalA, decimalB);
    }
}
