package io.github.loganmallory.decimaljava;

import io.github.loganmallory.decimaljava.annotations.Decimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.loganmallory.decimaljava.Decimal64.*;
import static io.github.loganmallory.decimaljava.Decimal64.Internal.*;
import static io.github.loganmallory.decimaljava.Decimal64.Internal.Data.getExponent;
import static io.github.loganmallory.decimaljava.Decimal64.Internal.Data.getMantissa;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"fenum:argument"})
public class Decimal64Test {

    // e.g. `mvn clean package -DDECIMAL64_TEST_FUZZ_N=1000`
    // 100m takes ~7 minutes
    // 10m  takes ~40 seconds
    // 1m   takes ~5 seconds
    public static final int FUZZ_N = Integer.getInteger("DECIMAL64_TEST_FUZZ_N", 1_000_000);

    public static final long RNG_SEED = 111;

    private static void assertDecEquals(@Decimal long expectedDecimal, @Decimal long actualDecimal) {
        assertEquals(expectedDecimal, actualDecimal, () -> "expected: " + triplet(expectedDecimal) + ", got: " + triplet(actualDecimal));
    }

    private static void assertDecEquals(@Decimal long expectedDecimal, @Decimal long actualDecimal, Supplier<Object> input) {
        assertEquals(expectedDecimal, actualDecimal, () -> "input: " + input.get() + ", expected: " + triplet(expectedDecimal) + ", got: " + triplet(actualDecimal));
    }

    private static void enumerate_bounds(int n, int mantissaPow10Bound, int exponentBound, BiConsumer<Long, Integer> boundsConsumer) {
        // determine how many times the inner two loops will iterate per iteration of the outer loop k,
        // arithmetic series sum
        int series_n = mantissaPow10Bound + 1;
        int series_a = exponentBound;
        int series_l = exponentBound - mantissaPow10Bound;
        int series_sum = (int) Math.round((series_n / 2.0) * (series_a + series_l));

        // how many times does the outer loop need to run to reach at least n iterations total?
        int k = (n % series_sum == 0) ? (n / series_sum) : (n / series_sum + 1);

        for (int i = 0; i < k; i++) {
            for (int m = 0; m <= mantissaPow10Bound; m++) {
                long mBound = m <= 18 ? FastMath.i64TenToThe(m) : Long.MAX_VALUE;
                for (int eBound = 1; eBound < (exponentBound+1) - m; eBound++) {
                    boundsConsumer.accept(mBound, eBound);
                }
            }
        }
    }

    private static void fuzz(int n, Consumer<@Decimal Long> decimalConsumer) {
        var rng = new Random(RNG_SEED);
        enumerate_bounds(n, 17, 255, (mantissaBound, exponentBound) -> {
            mantissaBound = Math.min(MAX_MANTISSA, mantissaBound);
            var a = fromParts(rng.nextLong(-mantissaBound, mantissaBound), rng.nextInt(-exponentBound, exponentBound));
            decimalConsumer.accept(a);
        });
    }

    private static void fuzz(int n, BiConsumer<@Decimal Long, @Decimal Long> twoDecimalsConsumer) {
        var rng = new Random(RNG_SEED);
        enumerate_bounds(n, 17, 255, (mantissaBound, exponentBound) -> {
            mantissaBound = Math.min(MAX_MANTISSA, mantissaBound);
            var a = fromParts(rng.nextLong(-mantissaBound, mantissaBound), rng.nextInt(-exponentBound, exponentBound));
            var b = fromParts(rng.nextLong(-mantissaBound, mantissaBound), rng.nextInt(-exponentBound, exponentBound));
            twoDecimalsConsumer.accept(a, b);
        });
    }

    @Nested
    class Data {

        @Test
        public void nan() {
            assertEquals(256, NAN);
            assertEquals(0, getMantissa(NAN));
            assertEquals(-256, getExponent(NAN));
        }

        @Test
        public void negative_infinity() {
            assertEquals(-9223372036854775040L, NEGATIVE_INFINITY);
            assertEquals(-18014398509481983L, getMantissa(NEGATIVE_INFINITY));
            assertEquals(-256, getExponent(NEGATIVE_INFINITY));
        }

        @Test
        public void positive_infinity() {
            assertEquals(9223372036854775552L, POSITIVE_INFINITY);
            assertEquals(18014398509481983L, getMantissa(POSITIVE_INFINITY));
            assertEquals(-256, getExponent(POSITIVE_INFINITY));
        }

        @Test
        public void zero() {
            assertEquals(0, ZERO);
            assertEquals(0, getMantissa(ZERO));
            assertEquals(0, getExponent(ZERO));
        }

        @Test
        public void one() {
            assertEquals(1 << 9, ONE);
            assertEquals(1, getMantissa(ONE));
            assertEquals(0, getExponent(ONE));
        }

        @Test
        public void two() {
            assertEquals(2 << 9, TWO);
            assertEquals(2, getMantissa(TWO));
            assertEquals(0, getExponent(TWO));
        }

        @Test
        public void random() {
            fuzz(FUZZ_N, (a, b) -> {
                b = Internal.Data.setMantissa(b, getMantissa(a));
                b = Internal.Data.setExponent(b, getExponent(a));

                assertEquals(getMantissa(a), getMantissa(b), triplet(b));
                assertEquals(getExponent(a), getExponent(b), triplet(b));
                assertEquals(a, Internal.Data.makeUnsafe(getMantissa(a), getExponent(a)));
                assertEquals(b, Internal.Data.makeUnsafe(getMantissa(b), getExponent(b)));
            });

            fuzz(FUZZ_N, (a, b) -> {
                b = Internal.Data.setExponent(b, getExponent(a));
                b = Internal.Data.setMantissa(b, getMantissa(a));

                assertEquals(getMantissa(a), getMantissa(b), triplet(b));
                assertEquals(getExponent(a), getExponent(b), triplet(b));
                assertEquals(a, Internal.Data.makeUnsafe(getMantissa(a), getExponent(a)));
                assertEquals(b, Internal.Data.makeUnsafe(getMantissa(b), getExponent(b)));
            });
        }
    }

    @Nested
    class Debug {

        @Test
        public void nan() {
            assertEquals("(256, 0, -256)", triplet(NAN));
            assertEquals("(0, -256)", tuple(NAN));
            assertDoesNotThrow(() -> validate(NAN));
        }

        @Test
        public void negative_infinity() {
            assertEquals("(-9223372036854775040, -18014398509481983, -256)", triplet(NEGATIVE_INFINITY));
            assertEquals("(-18014398509481983, -256)", tuple(NEGATIVE_INFINITY));
            assertDoesNotThrow(() -> validate(NEGATIVE_INFINITY));
        }

        @Test
        public void positive_infinity() {
            assertEquals("(9223372036854775552, 18014398509481983, -256)", triplet(POSITIVE_INFINITY));
            assertEquals("(18014398509481983, -256)", tuple(POSITIVE_INFINITY));
            assertDoesNotThrow(() -> validate(POSITIVE_INFINITY));
        }

        @Test
        public void zero() {
            assertEquals("(0, 0, 0)", triplet(ZERO));
            assertEquals("(0, 0)", tuple(ZERO));
            assertDoesNotThrow(() -> validate(ZERO));
        }

        @Test
        public void one() {
            assertEquals("(512, 1, 0)", triplet(ONE));
            assertEquals("(1, 0)", tuple(ONE));
            assertDoesNotThrow(() -> validate(ONE));
        }

        @Test
        public void two() {
            assertEquals("(1024, 2, 0)", triplet(TWO));
            assertEquals("(2, 0)", tuple(TWO));
            assertDoesNotThrow(() -> validate(TWO));
        }

        @Test
        public void invalid_trailing_zeros() {
            var ex = assertThrows(RuntimeException.class, () -> validate(Internal.Data.makeUnsafe(31400, 0)));
            assertEquals("Decimal (16076800, 31400, 0) mantissa has trailing zeros", ex.getMessage());
        }

        @Test
        public void zero_with_nonzero_exponent() {
            var ex = assertThrows(RuntimeException.class, () -> validate(Internal.Data.makeUnsafe(0, -1)));
            assertEquals("Decimal (511, 0, -1) is ambiguous, exponent should be zero", ex.getMessage());
        }

        @Test
        public void random() {
            // exception stack traces are super expensive, so only do FUZZ_N / 100
            fuzz(FUZZ_N / 100, decimal -> {
                long mantissa = getMantissa(decimal);
                int exponent = getExponent(decimal);

                // check triplet debug string
                assertEquals(String.format("(%d, %d, %d)", Internal.Data.makeUnsafe(mantissa, exponent), mantissa, exponent), triplet(decimal));

                // strip trailing zeros in mantissa
                while (mantissa != 0 && mantissa % 10 == 0) {
                    mantissa /= 10;
                }

                {
                    // valid
                    long x = Internal.Data.makeUnsafe(mantissa, exponent);
                    assertDoesNotThrow(() -> validate(x), Internal.Debug.triplet(x));
                    assertDoesNotThrow(() -> validateFinite(x), Internal.Debug.triplet(x));
                }

                {
                    // invalid special exponent
                    long x = Internal.Data.makeUnsafe(mantissa == 0 ? 1 : mantissa, -256);
                    var ex = assertThrows(RuntimeException.class, () -> validate(x), Internal.Debug.triplet(x));
                    assertEquals("Decimal " + Internal.Debug.triplet(x) + " has special exponent -256 but isn't NaN or -/+ Infinity", ex.getMessage(), Internal.Debug.triplet(x));
                }

                {
                    // invalid large exponent
                    long x = Internal.Data.makeUnsafe(mantissa == 0 ? 1 : mantissa, -256);
                    var ex = assertThrows(RuntimeException.class, () -> validateFinite(x), Internal.Debug.triplet(x));
                    assertEquals("Decimal " + Internal.Debug.triplet(x) + " exponent is out of range [-255, 255]", ex.getMessage(), Internal.Debug.triplet(x));
                }

                {
                    mantissa = (mantissa / 10) * 10;
                    if (mantissa == 0) {
                        // zero mantissa with nonzero exponent
                        long x = Internal.Data.makeUnsafe(mantissa, exponent == 0 ? 1 : exponent);
                        var ex = assertThrows(RuntimeException.class, () -> validate(x), Internal.Debug.triplet(x));
                        assertEquals("Decimal " + Internal.Debug.triplet(x) + " is ambiguous, exponent should be zero", ex.getMessage(), Internal.Debug.triplet(x));
                    } else {
                        // invalid mantissa, trailing zeros
                        long x = Internal.Data.makeUnsafe(mantissa, exponent);
                        var ex = assertThrows(RuntimeException.class, () -> validate(x), Internal.Debug.triplet(x));
                        assertEquals("Decimal " + Internal.Debug.triplet(x) + " mantissa has trailing zeros", ex.getMessage(), Internal.Debug.triplet(x));
                    }
                }

                {
                    // invalid mantissa, exceeds max
                    long x = Internal.Data.makeUnsafe(MAX_MANTISSA + 1, exponent);
                    var ex = assertThrows(RuntimeException.class, () -> validate(x), Internal.Debug.triplet(x));
                    assertEquals("Decimal " + Internal.Debug.triplet(x) + " mantissa is out of range [-9999999999999999, 9999999999999999]", ex.getMessage(), Internal.Debug.triplet(x));
                }

                {
                    // invalid mantissa, exceeds min
                    long x = Internal.Data.makeUnsafe(MIN_MANTISSA - 1, exponent);
                    var ex = assertThrows(RuntimeException.class, () -> validate(x), Internal.Debug.triplet(x));
                    assertEquals("Decimal " + Internal.Debug.triplet(x) + " mantissa is out of range [-9999999999999999, 9999999999999999]", ex.getMessage(), Internal.Debug.triplet(x));
                }
            });
        }
    }

    @Nested
    class Convert {

        @Nested
        class Parts {

            @Nested
            class FromParts {

                @Test
                public void zero() {
                    long x = fromParts(0, 0);
                    assertEquals(ZERO, x);
                }

                @Test
                public void zero_ignore_exponent() {
                    long x = fromParts(0, -123);
                    assertEquals(ZERO, x);
                }

                @Test
                public void strip_trailing_zeros() {
                    long x = fromParts(31400, 1);
                    assertEquals(314, getMantissa(x));
                    assertEquals(-1, getExponent(x));
                }

                @Test
                public void overflow_positive() {
                    long x = fromParts(12300, -254);
                    assertEquals(POSITIVE_INFINITY, x);
                }

                @Test
                public void overflow_negative() {
                    long x = fromParts(-12300, -254);
                    assertEquals(NEGATIVE_INFINITY, x);
                }

                @Test
                public void underflow_to_zero() {
                    long x = fromParts(1, 256);
                    assertEquals(ZERO, x);
                }

                @Test
                public void underflow_truncating() {
                    long x = fromParts(123, 256);
                    assertEquals(12, getMantissa(x));
                    assertEquals(255, getExponent(x));
                }

                @Test
                public void strip_trailing_zeros_after_exponent_past_max() {
                    long x = fromParts(9748457115612414L, 270);
                    assertEquals(1, getMantissa(x));
                    assertEquals(254, getExponent(x));
                }

                @Test
                public void random() {
                    // TODO: randomly test fromParts(...).
                    //       Not really sure how to randomly test this.
                    //       Will leave for later, it's mostly covered by other ops like math anyways.
                }
            }
        }

        @Nested
        class I64 {

            @Nested
            class FromI64 {

                @Test
                public void case_0000() {
                    var x = fromI64(0);
                    assertDecEquals(ZERO, x);
                }

                @Test
                public void case_0001() {
                    var x = fromI64(1);
                    assertDecEquals(ONE, x);
                }

                @Test
                public void case_0002() {
                    var x = fromI64(-1);
                    var expected = fromParts(-1, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0003() {
                    var x = fromI64(123);
                    var expected = fromParts(123, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0004() {
                    var x = fromI64(-123);
                    var expected = fromParts(-123, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0005() {
                    var x = fromI64(100);
                    var expected = fromParts(1, -2);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0006() {
                    var x = fromI64(MIN_MANTISSA);
                    var expected = fromParts(MIN_MANTISSA, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0007() {
                    var x = fromI64(MAX_MANTISSA);
                    var expected = fromParts(MAX_MANTISSA, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0008() {
                    var x = fromI64(Long.MIN_VALUE);
                    var expected = fromParts(-9223372036854776L, -3);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0009() {
                    var x = fromI64(Long.MAX_VALUE);
                    var expected = fromParts(9223372036854776L, -3);
                    assertDecEquals(expected, x);
                }

                @Test
                public void random() {
                    var rng = new Random(RNG_SEED);
                    rng.longs(FUZZ_N).forEach(integer -> {
                        var expected = BigDecimal.valueOf(integer, 0).round(MathContext.DECIMAL64).stripTrailingZeros();
                        var actual = fromI64(integer);
                        assertEquals(expected, toBigDecimal(actual));
                    });
                }
            }

            @Nested
            class ToI64 {

                @Test
                public void nan() {
                    var ex = assertThrows(RuntimeException.class, () -> toI64(NAN));
                    assertEquals("Can't convert non-finite decimal to i64: NaN", ex.getMessage());
                }

                @Test
                public void negative_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toI64(NEGATIVE_INFINITY));
                    assertEquals("Can't convert non-finite decimal to i64: -Infinity", ex.getMessage());
                }

                @Test
                public void positive_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toI64(POSITIVE_INFINITY));
                    assertEquals("Can't convert non-finite decimal to i64: +Infinity", ex.getMessage());
                }

                @Test
                public void zero() {
                    assertEquals(0, toI64(ZERO));
                }

                @Test
                public void one() {
                    assertEquals(1, toI64(ONE));
                }

                @Test
                public void case_0000() {
                    var x = fromParts(5, 1);
                    assertEquals(0, toI64(x));
                }

                @Test
                public void case_0001() {
                    var x = fromParts(-15, 1);
                    assertEquals(-2, toI64(x));
                }

                @Test
                public void case_0002() {
                    var x = fromParts(-67, 1);
                    assertEquals(-7, toI64(x));
                }

                @Test
                public void case_0003() {
                    var x = fromParts(-95, 1);
                    assertEquals(-10, toI64(x));
                }

                @Test
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        Long expectedI64;

                        try {
                            expectedI64 = toBigDecimal(decimal).setScale(0, RoundingMode.HALF_EVEN).longValueExact();
                        } catch (ArithmeticException ignore) {
                            expectedI64 = null;
                        }

                        if (expectedI64 != null) {
                            assertEquals(expectedI64, toI64(decimal), triplet(decimal));
                        } else {
                            var ex = assertThrows(RuntimeException.class, () -> toI64(decimal));
                            assertEquals("Decimal is too large to convert to i64: " + Decimal64.toString(decimal), ex.getMessage());
                        }
                    });
                }
            }

            @Nested
            class ToI64WithExponent {

                @Test
                public void nan() {
                    var ex = assertThrows(RuntimeException.class, () -> toI64(NAN, 1));
                    assertEquals("Can't convert non-finite decimal to i64: NaN", ex.getMessage());
                }

                @Test
                public void negative_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toI64(NEGATIVE_INFINITY, 1));
                    assertEquals("Can't convert non-finite decimal to i64: -Infinity", ex.getMessage());
                }

                @Test
                public void positive_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toI64(POSITIVE_INFINITY, 1));
                    assertEquals("Can't convert non-finite decimal to i64: +Infinity", ex.getMessage());
                }

                @Test
                public void zero() {
                    var x = ZERO;

                    assertEquals(0, toI64(x, -1));
                    assertEquals(0, toI64(x, 0));
                    assertEquals(0, toI64(x, 1));
                }

                @Test
                public void one() {
                    var x = ONE;

                    assertEquals(0, toI64(x, -1));
                    assertEquals(1, toI64(x, 0));
                    assertEquals(10, toI64(x, 1));
                    assertEquals(100, toI64(x, 2));
                }

                @Test
                public void case_0000() {
                    // 5.159000000000001
                    var x = fromParts(5159000000000001L, 15);

                    assertEquals(0, toI64(x, -1));
                    assertEquals(5, toI64(x, 0));
                    assertEquals(52, toI64(x, 1));
                    assertEquals(516, toI64(x, 2));
                    assertEquals(5159, toI64(x, 3));
                    assertEquals(51590, toI64(x, 4));
                    assertEquals(51590000000000010L, toI64(x, 16));
                    var ex = assertThrows(ArithmeticException.class, () -> toI64(x, 18));
                    assertEquals("Expanding decimal to 18 right side digits would overflow i64: 5.159000000000001", ex.getMessage());
                }

                @Test
                public void case_0001() {
                    var x = fromParts(5, 1);

                    assertEquals(0, toI64(x, -1));
                    assertEquals(0, toI64(x, 0));
                    assertEquals(5, toI64(x, 1));
                    assertEquals(5_000, toI64(x, 4));
                }

                @Test
                public void case_0002() {
                    var x = fromParts(-15, 1);

                    assertEquals(0, toI64(x, -1));
                    assertEquals(-2, toI64(x, 0));
                    assertEquals(-15, toI64(x, 1));
                    assertEquals(-1500, toI64(x, 3));
                }

                @Test
                public void case_0003() {
                    var x = fromParts(-67, 1);

                    assertEquals(0, toI64(x, -1));
                    assertEquals(-7, toI64(x, 0));
                    assertEquals(-67, toI64(x, 1));
                }

                @Test
                public void case_0004() {
                    var x = fromParts(-95, 1);

                    assertEquals(0, toI64(x, -1));
                    assertEquals(-10, toI64(x, 0));
                    assertEquals(-95, toI64(x, 1));
                }

                @Test
                public void case_0005() {
                    // -10
                    var x = fromParts(-1, -1);
                    assertEquals(-1, toI64(x, -1));
                }

                @Test
                public void case_0006() {
                    // 6e18
                    var x = fromParts(6, -18);
                    var ex = assertThrows(ArithmeticException.class, () -> toI64(x, 14));
                    assertEquals("Expanding decimal to 14 right side digits would overflow i64: 6000000000000000000", ex.getMessage());
                }

                @Test
                public void case_0007() {
                    var x = fromParts(413, -2);
                    var ex = assertThrows(ArithmeticException.class, () -> toI64(x, 14));
                    assertEquals("Expanding decimal to 14 right side digits would overflow i64: 41300", ex.getMessage());
                }

                @Test
                public void case_0008() {
                    var x = fromParts(65885, 5);
                    assertEquals(6588500000L, toI64(x, 10));
                }

                @Test
                @SuppressWarnings("fenum:binary")
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        int nFractionalDigits = (int) (decimal % 16);
                        var bigDecimal = toBigDecimal(decimal);
                        Long expectedI64;

                        try {
                            BigDecimal pow = BigDecimal.TEN.pow(Math.abs(nFractionalDigits));
                            if (nFractionalDigits > 0) {
                                bigDecimal = bigDecimal.multiply(pow, MathContext.DECIMAL64);
                                if (bigDecimal.precision() > PRECISION) {
                                    bigDecimal = bigDecimal.round(MathContext.DECIMAL64);
                                }
                            } else {
                                bigDecimal = bigDecimal.divide(pow, MathContext.DECIMAL64);
                            }
                            if (bigDecimal.abs().compareTo(BigDecimal.ONE) < 0) {
                                expectedI64 = 0L;
                            } else if (bigDecimal.abs().compareTo(BigDecimal.valueOf(999_999_999_999_999_999L)) > 0) {
                                throw new ArithmeticException("overflow");
                            } else {
                                expectedI64 = bigDecimal.longValueExact();
                            }
                        } catch (ArithmeticException ignore) {
                            expectedI64 = null;
                        }

                        if (expectedI64 != null) {
                            assertEquals(expectedI64, toI64(decimal, nFractionalDigits), tuple(decimal) + ", " + nFractionalDigits);
                        } else {
                            var ex = assertThrows(RuntimeException.class, () -> toI64(decimal, nFractionalDigits), tuple(decimal) + ", " + nFractionalDigits);
                            assertEquals("Expanding decimal to " + nFractionalDigits + " right side digits would overflow i64: " + Decimal64.toString(decimal), ex.getMessage());
                        }
                    });
                }
            }
        }

        @Nested
        class I32 {

            @Nested
            class FromI32 {

                @Test
                public void case_0000() {
                    var x = fromI32(0);
                    assertDecEquals(ZERO, x);
                }

                @Test
                public void case_0001() {
                    var x = fromI32(1);
                    assertDecEquals(ONE, x);
                }

                @Test
                public void case_0002() {
                    var x = fromI32(-1);
                    var expected = fromParts(-1, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0003() {
                    var x = fromI32(123);
                    var expected = fromParts(123, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0004() {
                    var x = fromI32(-123);
                    var expected = fromParts(-123, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0005() {
                    var x = fromI32(100);
                    var expected = fromParts(1, -2);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0008() {
                    var x = fromI32(Integer.MIN_VALUE);
                    var expected = fromParts(-2147483648, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void case_0009() {
                    var x = fromI32(Integer.MAX_VALUE);
                    var expected = fromParts(2147483647, 0);
                    assertDecEquals(expected, x);
                }

                @Test
                public void random() {
                    var rng = new Random(RNG_SEED);
                    rng.ints(FUZZ_N).forEach(integer -> {
                        var expected = BigDecimal.valueOf(integer, 0).round(MathContext.DECIMAL64).stripTrailingZeros();
                        var actual = fromI32(integer);
                        assertEquals(expected, toBigDecimal(actual));
                    });
                }
            }

            @Nested
            class ToI32 {

                @Test
                public void nan() {
                    var ex = assertThrows(RuntimeException.class, () -> toI32(NAN));
                    assertEquals("Can't convert non-finite decimal to i32: NaN", ex.getMessage());
                }

                @Test
                public void negative_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toI32(NEGATIVE_INFINITY));
                    assertEquals("Can't convert non-finite decimal to i32: -Infinity", ex.getMessage());
                }

                @Test
                public void positive_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toI32(POSITIVE_INFINITY));
                    assertEquals("Can't convert non-finite decimal to i32: +Infinity", ex.getMessage());
                }

                @Test
                public void zero() {
                    assertEquals(0, toI32(ZERO));
                }

                @Test
                public void one() {
                    assertEquals(1, toI32(ONE));
                }

                @Test
                public void case_0000() {
                    var x = fromParts(5, 1);
                    assertEquals(0, toI32(x));
                }

                @Test
                public void case_0001() {
                    var x = fromParts(-15, 1);
                    assertEquals(-2, toI32(x));
                }

                @Test
                public void case_0002() {
                    var x = fromParts(-67, 1);
                    assertEquals(-7, toI32(x));
                }

                @Test
                public void case_0003() {
                    var x = fromParts(-95, 1);
                    assertEquals(-10, toI32(x));
                }

                @Test
                public void case_0004() {
                    var x = fromParts(-6065340641L, 0);
                    var ex = assertThrows(RuntimeException.class, () -> toI32(x));
                    assertEquals("Decimal is too large to convert to i32: -6065340641", ex.getMessage());
                }

                @Test
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        try {
                            var expected = toBigDecimal(decimal).setScale(0, RoundingMode.HALF_EVEN).intValueExact();
                            assertEquals(expected, toI32(decimal), triplet(decimal));
                        } catch (ArithmeticException e) {
                            var ex = assertThrows(RuntimeException.class, () -> toI32(decimal), triplet(decimal));
                            assertEquals("Decimal is too large to convert to i32: " + Decimal64.toString(decimal), ex.getMessage());
                        }
                    });
                }
            }
        }

        @Nested
        class F64 {

            @Nested
            class FromF64 {

                @Test
                public void nan() {
                    assertDecEquals(NAN, fromF64(Double.NaN));
                }

                @Test
                public void negative_infinity() {
                    assertDecEquals(NEGATIVE_INFINITY, fromF64(Double.NEGATIVE_INFINITY));
                }

                @Test
                public void positive_infinity() {
                    assertDecEquals(POSITIVE_INFINITY, fromF64(Double.POSITIVE_INFINITY));
                }

                @Test
                public void f64_max_positive() {
                    assertDecEquals(POSITIVE_INFINITY, fromF64(Double.MAX_VALUE));
                }

                @Test
                public void f64_max_negative() {
                    assertDecEquals(NEGATIVE_INFINITY, fromF64(-Double.MAX_VALUE));
                }

                @Test
                public void f64_min_normal_positive() {
                    assertDecEquals(ZERO, fromF64(Double.MIN_NORMAL));
                }

                @Test
                public void f64_min_normal_negative() {
                    assertDecEquals(ZERO, fromF64(-Double.MIN_NORMAL));
                }

                @Test
                public void f64_min_positive() {
                    assertDecEquals(ZERO, fromF64(Double.MIN_VALUE));
                }

                @Test
                public void f64_min_negative() {
                    assertDecEquals(ZERO, fromF64(-Double.MIN_VALUE));
                }

                @Test
                public void f64_zero_positive() {
                    assertDecEquals(ZERO, fromF64(0.0));
                }

                @Test
                public void f64_zero_negative() {
                    assertDecEquals(ZERO, fromF64(-0.0));
                }

                @Test
                public void case_0000() {
                    var expected = fromParts(1, 0);
                    assertDecEquals(expected, fromF64(1.0));
                }

                @Test
                public void case_0001() {
                    var expected = fromParts(1, 1);
                    assertDecEquals(expected, fromF64(0.1));
                }

                @Test
                public void case_0002() {
                    var expected = fromParts(3, 1);
                    assertDecEquals(expected, fromF64(0.3));
                }

                @Test
                public void case_0003() {
                    var expected = fromParts(314159, 5);
                    assertDecEquals(expected, fromF64(3.14159));
                }

                @Test
                public void random() {
                    var rng = new Random(RNG_SEED);
                    rng.doubles(FUZZ_N, Double.MIN_VALUE, Double.MAX_VALUE).forEach(flt -> {
                        String expected;
                        if (Math.abs(flt) > Internal.Convert.F64.MAX_REPRESENTABLE_F64) {
                            expected = flt > 0 ? "+Infinity" : "-Infinity";
                        } else if (Math.abs(flt) < Internal.Convert.F64.MIN_REPRESENTABLE_F64) {
                            expected = "0";
                        } else {
                            expected = BigDecimal.valueOf(flt).round(MathContext.DECIMAL64).stripTrailingZeros().toPlainString();
                        }

                        assertEquals(expected, Decimal64.toString(fromF64(flt)), flt + "");
                    });
                }
            }

            @Nested
            class ToF64 {

                @Test
                public void nan() {
                    assertEquals(Double.NaN, toF64(NAN));
                }

                @Test
                public void negative_infinity() {
                    assertEquals(Double.NEGATIVE_INFINITY, toF64(NEGATIVE_INFINITY));
                }

                @Test
                public void positive_infinity() {
                    assertEquals(Double.POSITIVE_INFINITY, toF64(POSITIVE_INFINITY));
                }

                @Test
                public void zero() {
                    assertEquals(0.0, toF64(ZERO));
                }

                @Test
                public void one() {
                    assertEquals(1.0, toF64(ONE));
                }

                @Test
                public void two() {
                    assertEquals(2.0, toF64(TWO));
                }

                @Test
                public void case_0000() {
                    // 0.1
                    var x = fromParts(1, 1);
                    assertEquals(0.1, toF64(x));
                }

                @Test
                public void case_0001() {
                    // -0.1
                    var x = fromParts(-1, 1);
                    assertEquals(-0.1, toF64(x));
                }

                @Test
                public void case_0002() {
                    // 3.14159
                    var x = fromParts(314159, 5);
                    assertEquals(3.14159, toF64(x));
                }

                @Test
                public void case_0003() {
                    // -92952.95737362807
                    var x = fromParts(-9295295737362807L, 11);
                    assertEquals(-92952.95737362807, toF64(x));
                }

                @Test
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        double expected = BigDecimal.valueOf(getMantissa(decimal), getExponent(decimal)).doubleValue();
                        assertEquals(expected, toF64(decimal), triplet(decimal));
                    });
                }
            }
        }

        @Nested
        class BigDec {

            @Nested
            class FromBigDecimal {

                @Test
                public void random() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var bigDecimal = BigDecimal.valueOf(mantissa, exponent);

                        long expected = fromParts(mantissa, exponent);
                        assertDecEquals(expected, fromBigDecimal(bigDecimal));
                    });
                }
            }

            @Nested
            class ToBigDecimal {

                @Test
                public void nan() {
                    var ex = assertThrows(RuntimeException.class, () -> toBigDecimal(NAN));
                    assertEquals("Can't convert non-finite decimal NaN to BigDecimal", ex.getMessage());
                }

                @Test
                public void negative_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toBigDecimal(NEGATIVE_INFINITY));
                    assertEquals("Can't convert non-finite decimal -Infinity to BigDecimal", ex.getMessage());
                }

                @Test
                public void positive_infinity() {
                    var ex = assertThrows(RuntimeException.class, () -> toBigDecimal(POSITIVE_INFINITY));
                    assertEquals("Can't convert non-finite decimal +Infinity to BigDecimal", ex.getMessage());
                }

                @Test
                public void zero() {
                    var bd = toBigDecimal(ZERO);
                    assertEquals(0, bd.longValue());
                    assertEquals(0, bd.scale());
                }

                @Test
                public void one() {
                    var bd = toBigDecimal(ONE);
                    assertEquals(1, bd.longValue());
                    assertEquals(0, bd.scale());
                }

                @Test
                public void two() {
                    var bd = toBigDecimal(TWO);
                    assertEquals(2, bd.longValue());
                    assertEquals(0, bd.scale());
                }

                @Test
                public void ten() {
                    // I don't know why BigDecimal does this,
                    // but a value of 10 returns longValue()=10 and scale()=-1, which would suggest the value is 100 ...
                    // This is purely a reporting artifact via BigInteger, it's stored correctly internally.
                    // Anyways it makes testing annoying, so we'll use toPlainString instead
                    var bd = toBigDecimal(fromParts(1, -1));
                    // assertEquals(1, bd.longValue());
                    assertEquals(-1, bd.scale());
                    assertEquals("10", bd.toPlainString());
                }

                @Test
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        var bd = toBigDecimal(decimal);
                        assertEquals(Decimal64.toString(decimal), bd.toPlainString(), triplet(decimal));
                    });
                }
            }
        }

        @Nested
        class Str {

            @Nested
            class ByteBufCharSeq {

                @Test
                public void array_backed() {
                    var buf = ByteBuffer.wrap("abc123xyz".getBytes(), 3, 4);
                    var seq = new Internal.Convert.Str.ByteBufferCharSequence(buf);

                    assertSame(buf, seq.buf);
                    assertEquals(4, seq.length());
                    assertEquals('1', seq.charAt(0));
                    assertEquals('2', seq.charAt(1));
                    assertEquals('3', seq.charAt(2));
                    assertEquals('x', seq.charAt(3));
                    assertEquals("123x", seq.subSequence(0, 4));
                    assertEquals("23x", seq.subSequence(1, 4));
                    assertEquals("23", seq.subSequence(1, 3));
                    assertEquals("x", seq.subSequence(3, 4));
                    assertEquals("123x", seq.toString());
                }

                @Test
                public void not_array_backed() {
                    var buf = ByteBuffer.allocateDirect(9);
                    buf.put("abc123xyz".getBytes());
                    buf.position(3).limit(7);
                    var seq = new Internal.Convert.Str.ByteBufferCharSequence(buf);

                    assertSame(buf, seq.buf);
                    assertEquals(4, seq.length());
                    assertEquals('1', seq.charAt(0));
                    assertEquals('2', seq.charAt(1));
                    assertEquals('3', seq.charAt(2));
                    assertEquals('x', seq.charAt(3));
                    assertEquals("123x", seq.subSequence(0, 4));
                    assertEquals("23x", seq.subSequence(1, 4));
                    assertEquals("23", seq.subSequence(1, 3));
                    assertEquals("x", seq.subSequence(3, 4));
                    assertEquals("123x", seq.toString());
                }
            }

            @Nested
            class FromString {

                private static void fuzz_str(String baseStr, long expected) {
                    boolean negative = baseStr.charAt(0) == '-';
                    if (negative) {
                        baseStr = baseStr.substring(1);
                    }
                    boolean scientificNotation = baseStr.contains("E");
                    boolean nonFinite = baseStr.contains("NaN") || baseStr.contains("Infinity");
                    for (var leadingGarbage : List.of("", "xyz")) {
                        for (var leadingWhitespace : List.of("", "   ")) {
                            for (var sign : negative ? List.of("-") : List.of("", "+")) {
                                for (var leadingZeros : List.of("", "000")) {
                                    for (var trailingZeros : scientificNotation || !baseStr.contains(".") ? List.of(".", ".000") : List.of("", "000")) {
                                        for (var trailingWhitespace : List.of("", "   ")) {
                                            for (var trailingGarbage : List.of("", "zyx")) {

                                                var str = leadingGarbage + leadingWhitespace + sign + leadingZeros
                                                        + baseStr
                                                        + trailingZeros + trailingWhitespace + trailingGarbage;

                                                boolean hasGarbage = !leadingGarbage.isEmpty() || !trailingGarbage.isEmpty();
                                                hasGarbage |= scientificNotation && (str.lastIndexOf('.') > str.indexOf('E'));
                                                hasGarbage |= nonFinite && (str.contains(".") || str.contains("0"));

                                                if (hasGarbage) {
                                                    var ex = assertThrows(NumberFormatException.class, () -> fromString(str), () -> "'" + str + "'");
                                                    assertEquals("Invalid decimal string: '" + str.strip() + "'", ex.getMessage());
                                                } else {
                                                    assertDecEquals(expected, fromString(str), () -> "'" + str + "'");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Test
                public void nan() {
                    assertDecEquals(NAN, fromString("NaN"));
                    fuzz_str("NaN", NAN);
                }

                @Test
                public void negative_infinity() {
                    assertDecEquals(NEGATIVE_INFINITY, fromString("-Infinity"));
                    fuzz_str("-Infinity", NEGATIVE_INFINITY);
                }

                @Test
                public void positive_infinity() {
                    assertDecEquals(POSITIVE_INFINITY, fromString("+Infinity"));
                    fuzz_str("+Infinity", POSITIVE_INFINITY);
                }

                @Test
                public void zero() {
                    assertDecEquals(ZERO, fromString("0"));
                }

                @Test
                public void one() {
                    assertDecEquals(ONE, fromString("1"));
                }

                @Test
                public void two() {
                    assertDecEquals(TWO, fromString("2"));
                }

                @Test
                public void case_0000() {
                    var expected = fromParts(-10, 0);
                    assertDecEquals(expected, fromString("-10."));
                }

                @Test
                public void case_0001() {
                    var ex = assertThrows(NumberFormatException.class, () -> fromString("-10.zyx"));
                    assertEquals("Invalid decimal string: '-10.zyx'", ex.getMessage());
                }

                @Test
                public void case_0002() {
                    var expected = fromParts(-10, 0);
                    assertDecEquals(expected, fromString("   -10.  "));
                }

                @Test
                public void case_0003() {
                    var expected = fromParts(-10, 0);
                    assertDecEquals(expected, fromString("-1E+1"));
                }

                @Test
                public void case_0004() {
                    var ex = assertThrows(NumberFormatException.class, () -> fromString("-1E+1."));
                    assertEquals("Invalid decimal string: '-1E+1.'", ex.getMessage());
                }

                @Test
                public void case_0005() {
                    var expected = fromParts(-1, 3);
                    assertDecEquals(expected, fromString("-0.001"));
                }

                @Test
                public void case_0006() {
                    var expected = fromParts(-1, 18);
                    assertDecEquals(expected, fromString("-0.000000000000000001"));
                }

                @Test
                public void case_0007() {
                    var ex = assertThrows(NumberFormatException.class, () -> fromString("   "));
                    assertEquals("Invalid decimal string: '   '", ex.getMessage());
                }

                @Test
                public void case_0008() {
                    var ex = assertThrows(NumberFormatException.class, () -> fromString(" + "));
                    assertEquals("Invalid decimal string: ' + '", ex.getMessage());
                }

                @Test
                public void case_0009() {
                    var ex = assertThrows(NumberFormatException.class, () -> fromString("- "));
                    assertEquals("Invalid decimal string: '- '", ex.getMessage());
                }

                @Test
                public void random_plain_string() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).stripTrailingZeros().toPlainString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(decimalStr), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                    });
                }

                @Test
                public void random_plain_string_fuzz() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N / 1_000, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var baseStr = BigDecimal.valueOf(mantissa, exponent).stripTrailingZeros().toPlainString();
                        long expected = fromParts(mantissa, exponent);

                        fuzz_str(baseStr, expected);
                    });
                }

                @Test
                public void random_scientific_notation() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).toString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(decimalStr), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                    });
                }

                @Test
                public void random_scientific_notation_fuzz() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N / 1_000, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var baseStr = BigDecimal.valueOf(mantissa, exponent).toString();
                        long expected = fromParts(mantissa, exponent);

                        fuzz_str(baseStr, expected);
                    });
                }
            }

            @Nested
            class FromStringOffset {

                @Test
                public void nan() {
                    assertDecEquals(NAN, fromString("NaN", 0));
                    assertDecEquals(NAN, fromString("abcNaN", 3));
                }

                @Test
                public void negative_infinity() {
                    assertDecEquals(NEGATIVE_INFINITY, fromString("-Infinity", 0));
                    assertDecEquals(NEGATIVE_INFINITY, fromString("abc-Infinity", 3));
                }

                @Test
                public void positive_infinity() {
                    assertDecEquals(POSITIVE_INFINITY, fromString("+Infinity", 0));
                    assertDecEquals(POSITIVE_INFINITY, fromString("abc+Infinity", 3));
                }

                @Test
                public void zero() {
                    assertDecEquals(ZERO, fromString("0", 0));
                    assertDecEquals(ZERO, fromString("1230", 3));
                }

                @Test
                public void one() {
                    assertDecEquals(ONE, fromString("1", 0));
                    assertDecEquals(ONE, fromString("1231", 3));
                }

                @Test
                public void two() {
                    assertDecEquals(TWO, fromString("2", 0));
                    assertDecEquals(TWO, fromString("1232", 3));
                }

                @Test
                public void random_plain_string() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).stripTrailingZeros().toPlainString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(decimalStr, 0), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString("123" + decimalStr, 3), () -> String.format("(%d, %d) '123%s'", mantissa, exponent, decimalStr));
                    });
                }

                @Test
                public void random_scientific_notation() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).toString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(decimalStr, 0), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString("123" + decimalStr, 3), () -> String.format("(%d, %d) '123%s'", mantissa, exponent, decimalStr));
                    });
                }
            }

            @Nested
            class FromStringOffsetLength {

                @Test
                public void nan() {
                    assertDecEquals(NAN, fromString("NaN", 0, 3));
                    assertDecEquals(NAN, fromString("abcNaN", 3, 3));
                    assertDecEquals(NAN, fromString("abcNaNabc", 3, 3));
                }

                @Test
                public void negative_infinity() {
                    assertDecEquals(NEGATIVE_INFINITY, fromString("-Infinity", 0, 9));
                    assertDecEquals(NEGATIVE_INFINITY, fromString("abc-Infinity", 3, 9));
                    assertDecEquals(NEGATIVE_INFINITY, fromString("abc-Infinityabc", 3, 9));
                }

                @Test
                public void positive_infinity() {
                    assertDecEquals(POSITIVE_INFINITY, fromString("+Infinity", 0, 9));
                    assertDecEquals(POSITIVE_INFINITY, fromString("abc+Infinity", 3, 9));
                    assertDecEquals(POSITIVE_INFINITY, fromString("abc+Infinityabc", 3, 9));
                }

                @Test
                public void zero() {
                    assertDecEquals(ZERO, fromString("0", 0, 1));
                    assertDecEquals(ZERO, fromString("1230", 3, 1));
                    assertDecEquals(ZERO, fromString("1230123", 3, 1));
                }

                @Test
                public void one() {
                    assertDecEquals(ONE, fromString("1", 0, 1));
                    assertDecEquals(ONE, fromString("1231", 3, 1));
                    assertDecEquals(ONE, fromString("1231", 3, 1));
                    assertDecEquals(ONE, fromString("1231123", 3, 1));
                }

                @Test
                public void two() {
                    assertDecEquals(TWO, fromString("2", 0, 1));
                    assertDecEquals(TWO, fromString("1232", 3, 1));
                    assertDecEquals(TWO, fromString("1232123", 3, 1));
                }

                @Test
                public void random_plain_string() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).stripTrailingZeros().toPlainString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(decimalStr, 0, decimalStr.length()), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString("123" + decimalStr, 3, decimalStr.length()), () -> String.format("(%d, %d) '123%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString("123" + decimalStr + "123", 3, decimalStr.length()), () -> String.format("(%d, %d) '123%s123'", mantissa, exponent, decimalStr));
                    });
                }

                @Test
                public void random_scientific_notation() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).toString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(decimalStr, 0, decimalStr.length()), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString("123" + decimalStr, 3, decimalStr.length()), () -> String.format("(%d, %d) '123%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString("123" + decimalStr + "123", 3, decimalStr.length()), () -> String.format("(%d, %d) '123%s123'", mantissa, exponent, decimalStr));
                    });
                }
            }

            @Nested
            class FromStringBuffer {

                @Test
                public void nan() {
                    assertDecEquals(NAN, fromString(ByteBuffer.wrap("NaN".getBytes(), 0, 3)));
                    assertDecEquals(NAN, fromString(ByteBuffer.wrap("abcNaN".getBytes(), 3, 3)));
                    assertDecEquals(NAN, fromString(ByteBuffer.wrap("abcNaNabc".getBytes(), 3, 3)));
                }

                @Test
                public void negative_infinity() {
                    assertDecEquals(NEGATIVE_INFINITY, fromString(ByteBuffer.wrap("-Infinity".getBytes(), 0, 9)));
                    assertDecEquals(NEGATIVE_INFINITY, fromString(ByteBuffer.wrap("abc-Infinity".getBytes(), 3, 9)));
                    assertDecEquals(NEGATIVE_INFINITY, fromString(ByteBuffer.wrap("abc-Infinityabc".getBytes(), 3, 9)));
                }

                @Test
                public void positive_infinity() {
                    assertDecEquals(POSITIVE_INFINITY, fromString(ByteBuffer.wrap("+Infinity".getBytes(), 0, 9)));
                    assertDecEquals(POSITIVE_INFINITY, fromString(ByteBuffer.wrap("abc+Infinity".getBytes(), 3, 9)));
                    assertDecEquals(POSITIVE_INFINITY, fromString(ByteBuffer.wrap("abc+Infinityabc".getBytes(), 3, 9)));
                }

                @Test
                public void zero() {
                    assertDecEquals(ZERO, fromString(ByteBuffer.wrap("0".getBytes(), 0, 1)));
                    assertDecEquals(ZERO, fromString(ByteBuffer.wrap("1230".getBytes(), 3, 1)));
                    assertDecEquals(ZERO, fromString(ByteBuffer.wrap("1230123".getBytes(), 3, 1)));
                }

                @Test
                public void one() {
                    assertDecEquals(ONE, fromString(ByteBuffer.wrap("1".getBytes(), 0, 1)));
                    assertDecEquals(ONE, fromString(ByteBuffer.wrap("1231".getBytes(), 3, 1)));
                    assertDecEquals(ONE, fromString(ByteBuffer.wrap("1231".getBytes(), 3, 1)));
                    assertDecEquals(ONE, fromString(ByteBuffer.wrap("1231123".getBytes(), 3, 1)));
                }

                @Test
                public void two() {
                    assertDecEquals(TWO, fromString(ByteBuffer.wrap("2".getBytes(), 0, 1)));
                    assertDecEquals(TWO, fromString(ByteBuffer.wrap("1232".getBytes(), 3, 1)));
                    assertDecEquals(TWO, fromString(ByteBuffer.wrap("1232123".getBytes(), 3, 1)));
                }

                @Test
                public void random_plain_string() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).stripTrailingZeros().toPlainString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(ByteBuffer.wrap(decimalStr.getBytes(), 0, decimalStr.length())), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString(ByteBuffer.wrap(("123" + decimalStr).getBytes(), 3, decimalStr.length())), () -> String.format("(%d, %d) '123%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString(ByteBuffer.wrap(("123" + decimalStr + "123").getBytes(), 3, decimalStr.length())), () -> String.format("(%d, %d) '123%s123'", mantissa, exponent, decimalStr));
                    });
                }

                @Test
                public void random_scientific_notation() {
                    var rng = new Random(RNG_SEED);
                    enumerate_bounds(FUZZ_N, 20, 300, (mantissaBound, exponentBound) -> {
                        var mantissa = rng.nextLong(-mantissaBound, mantissaBound);
                        var exponent = rng.nextInt(-exponentBound, exponentBound);
                        var decimalStr = BigDecimal.valueOf(mantissa, exponent).toString();
                        long expected = fromParts(mantissa, exponent);

                        assertDecEquals(expected, fromString(ByteBuffer.wrap(decimalStr.getBytes(), 0, decimalStr.length())), () -> String.format("(%d, %d) '%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString(ByteBuffer.wrap(("123" + decimalStr).getBytes(), 3, decimalStr.length())), () -> String.format("(%d, %d) '123%s'", mantissa, exponent, decimalStr));
                        assertDecEquals(expected, fromString(ByteBuffer.wrap(("123" + decimalStr + "123").getBytes(), 3, decimalStr.length())), () -> String.format("(%d, %d) '123%s123'", mantissa, exponent, decimalStr));
                    });
                }
            }

            @Nested
            class ToString {

                @Test
                public void nan() {
                    assertEquals("NaN", Decimal64.toString(NAN));
                }

                @Test
                public void negative_infinity() {
                    assertEquals("-Infinity", Decimal64.toString(NEGATIVE_INFINITY));
                }

                @Test
                public void positive_infinity() {
                    assertEquals("+Infinity", Decimal64.toString(POSITIVE_INFINITY));
                }

                @Test
                public void zero() {
                    assertEquals("0", Decimal64.toString(ZERO));
                }

                @Test
                public void one() {
                    assertEquals("1", Decimal64.toString(ONE));
                }

                @Test
                public void two() {
                    assertEquals("2", Decimal64.toString(TWO));
                }

                @Test
                public void case0001() {
                    var x = fromParts(2946149842243065L, 15);
                    assertEquals("2.946149842243065", Decimal64.toString(x));
                }

                @Test
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        var expected = toBigDecimal(decimal).stripTrailingZeros().toPlainString();
                        assertEquals(expected, Decimal64.toString(decimal), triplet(decimal));
                    });
                }
            }
        }
    }

    @Nested
    class Compare {

        @Nested
        class DecimalVsZero {

            @Test
            public void nan() {
                assertFalse(isZero(NAN));
                assertFalse(ltZero(NAN));
                assertFalse(leZero(NAN));
                assertTrue(geZero(NAN));
                assertTrue(gtZero(NAN));
            }

            @Test
            public void negative_infinity() {
                assertFalse(isZero(NEGATIVE_INFINITY));
                assertTrue(ltZero(NEGATIVE_INFINITY));
                assertTrue(leZero(NEGATIVE_INFINITY));
                assertFalse(geZero(NEGATIVE_INFINITY));
                assertFalse(gtZero(NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity() {
                assertFalse(isZero(POSITIVE_INFINITY));
                assertFalse(ltZero(POSITIVE_INFINITY));
                assertFalse(leZero(POSITIVE_INFINITY));
                assertTrue(geZero(POSITIVE_INFINITY));
                assertTrue(gtZero(POSITIVE_INFINITY));
            }

            @Test
            public void zero() {
                assertTrue(isZero(ZERO));
                assertFalse(ltZero(ZERO));
                assertTrue(leZero(ZERO));
                assertTrue(geZero(ZERO));
                assertFalse(gtZero(ZERO));
            }

            @Test
            public void one() {
                assertFalse(isZero(ONE));
                assertFalse(ltZero(ONE));
                assertFalse(leZero(ONE));
                assertTrue(geZero(ONE));
                assertTrue(gtZero(ONE));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, decimal -> {
                    var expected = toBigDecimal(decimal).compareTo(BigDecimal.ZERO);
                    switch (expected) {
                        case -1 -> {
                            assertFalse(isZero(decimal), triplet(decimal));
                            assertTrue(ltZero(decimal), triplet(decimal));
                            assertTrue(leZero(decimal), triplet(decimal));
                            assertFalse(geZero(decimal), triplet(decimal));
                            assertFalse(gtZero(decimal), triplet(decimal));
                        }
                        case 0 -> {
                            assertTrue(isZero(decimal), triplet(decimal));
                            assertFalse(ltZero(decimal), triplet(decimal));
                            assertTrue(leZero(decimal), triplet(decimal));
                            assertTrue(geZero(decimal), triplet(decimal));
                            assertFalse(gtZero(decimal), triplet(decimal));
                        }
                        case 1 -> {
                            assertFalse(isZero(decimal), triplet(decimal));
                            assertFalse(ltZero(decimal), triplet(decimal));
                            assertFalse(leZero(decimal), triplet(decimal));
                            assertTrue(geZero(decimal), triplet(decimal));
                            assertTrue(gtZero(decimal), triplet(decimal));
                        }
                    }
                });
            }
        }

        @Nested
        class DecimalVsDecimal {

            @Test
            public void nan_nan() {
                assertEquals(0, compare(NAN, NAN));
                assertTrue(equal(NAN, NAN));
                assertDecEquals(NAN, min(NAN, NAN));
                assertDecEquals(NAN, max(NAN, NAN));
            }

            @Test
            public void nan_negative_infinity() {
                assertEquals(1, compare(NAN, NEGATIVE_INFINITY));
                assertFalse(equal(NAN, NEGATIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, min(NAN, NEGATIVE_INFINITY));
                assertDecEquals(NAN, max(NAN, NEGATIVE_INFINITY));
            }

            @Test
            public void nan_positive_infinity() {
                assertEquals(1, compare(NAN, POSITIVE_INFINITY));
                assertFalse(equal(NAN, POSITIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, min(NAN, POSITIVE_INFINITY));
                assertDecEquals(NAN, max(NAN, POSITIVE_INFINITY));
            }

            @Test
            public void nan_zero() {
                assertEquals(1, compare(NAN, ZERO));
                assertFalse(equal(NAN, ZERO));
                assertDecEquals(ZERO, min(NAN, ZERO));
                assertDecEquals(NAN, max(NAN, ZERO));
            }

            @Test
            public void nan_one() {
                assertEquals(1, compare(NAN, ONE));
                assertFalse(equal(NAN, ONE));
                assertDecEquals(ONE, min(NAN, ONE));
                assertDecEquals(NAN, max(NAN, ONE));
            }

            @Test
            public void negative_infinity_nan() {
                assertEquals(-1, compare(NEGATIVE_INFINITY, NAN));
                assertFalse(equal(NEGATIVE_INFINITY, NAN));
                assertDecEquals(NEGATIVE_INFINITY, min(NEGATIVE_INFINITY, NAN));
                assertDecEquals(NAN, max(NEGATIVE_INFINITY, NAN));
            }

            @Test
            public void negative_infinity_negative_infinity() {
                assertEquals(0, compare(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
                assertTrue(equal(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, min(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, max(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void negative_infinity_positive_infinity() {
                assertEquals(-1, compare(NEGATIVE_INFINITY, POSITIVE_INFINITY));
                assertFalse(equal(NEGATIVE_INFINITY, POSITIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, min(NEGATIVE_INFINITY, POSITIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, max(NEGATIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void negative_infinity_zero() {
                assertEquals(-1, compare(NEGATIVE_INFINITY, ZERO));
                assertFalse(equal(NEGATIVE_INFINITY, ZERO));
                assertDecEquals(NEGATIVE_INFINITY, min(NEGATIVE_INFINITY, ZERO));
                assertDecEquals(ZERO, max(NEGATIVE_INFINITY, ZERO));
            }

            @Test
            public void negative_infinity_one() {
                assertEquals(-1, compare(NEGATIVE_INFINITY, ONE));
                assertFalse(equal(NEGATIVE_INFINITY, ONE));
                assertDecEquals(NEGATIVE_INFINITY, min(NEGATIVE_INFINITY, ONE));
                assertDecEquals(ONE, max(NEGATIVE_INFINITY, ONE));
            }

            @Test
            public void positive_infinity_nan() {
                assertEquals(-1, compare(POSITIVE_INFINITY, NAN));
                assertFalse(equal(POSITIVE_INFINITY, NAN));
                assertDecEquals(POSITIVE_INFINITY, min(POSITIVE_INFINITY, NAN));
                assertDecEquals(NAN, max(POSITIVE_INFINITY, NAN));
            }

            @Test
            public void positive_infinity_negative_infinity() {
                assertEquals(1, compare(POSITIVE_INFINITY, NEGATIVE_INFINITY));
                assertFalse(equal(POSITIVE_INFINITY, NEGATIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, min(POSITIVE_INFINITY, NEGATIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, max(POSITIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity_positive_infinity() {
                assertEquals(0, compare(POSITIVE_INFINITY, POSITIVE_INFINITY));
                assertTrue(equal(POSITIVE_INFINITY, POSITIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, min(POSITIVE_INFINITY, POSITIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, max(POSITIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void positive_infinity_zero() {
                assertEquals(1, compare(POSITIVE_INFINITY, ZERO));
                assertFalse(equal(POSITIVE_INFINITY, ZERO));
                assertDecEquals(ZERO, min(POSITIVE_INFINITY, ZERO));
                assertDecEquals(POSITIVE_INFINITY, max(POSITIVE_INFINITY, ZERO));
            }

            @Test
            public void positive_infinity_one() {
                assertEquals(1, compare(POSITIVE_INFINITY, ONE));
                assertFalse(equal(POSITIVE_INFINITY, ONE));
                assertDecEquals(ONE, min(POSITIVE_INFINITY, ONE));
                assertDecEquals(POSITIVE_INFINITY, max(POSITIVE_INFINITY, ONE));
            }

            @Test
            public void zero_nan() {
                assertEquals(-1, compare(ZERO, NAN));
                assertFalse(equal(ZERO, NAN));
                assertDecEquals(ZERO, min(ZERO, NAN));
                assertDecEquals(NAN, max(ZERO, NAN));
            }

            @Test
            public void zero_negative_infinity() {
                assertEquals(1, compare(ZERO, NEGATIVE_INFINITY));
                assertFalse(equal(ZERO, NEGATIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, min(ZERO, NEGATIVE_INFINITY));
                assertDecEquals(ZERO, max(ZERO, NEGATIVE_INFINITY));
            }

            @Test
            public void zero_positive_infinity() {
                assertEquals(-1, compare(ZERO, POSITIVE_INFINITY));
                assertFalse(equal(ZERO, POSITIVE_INFINITY));
                assertDecEquals(ZERO, min(ZERO, POSITIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, max(ZERO, POSITIVE_INFINITY));
            }

            @Test
            public void zero_zero() {
                assertEquals(0, compare(ZERO, ZERO));
                assertTrue(equal(ZERO, ZERO));
                assertDecEquals(ZERO, min(ZERO, ZERO));
                assertDecEquals(ZERO, max(ZERO, ZERO));
            }

            @Test
            public void zero_one() {
                assertEquals(-1, compare(ZERO, ONE));
                assertFalse(equal(ZERO, ONE));
                assertDecEquals(ZERO, min(ZERO, ONE));
                assertDecEquals(ONE, max(ZERO, ONE));
            }

            @Test
            public void one_nan() {
                assertEquals(-1, compare(ONE, NAN));
                assertFalse(equal(ONE, NAN));
                assertDecEquals(ONE, min(ONE, NAN));
                assertDecEquals(NAN, max(ONE, NAN));
            }

            @Test
            public void one_negative_infinity() {
                assertEquals(1, compare(ONE, NEGATIVE_INFINITY));
                assertFalse(equal(ONE, NEGATIVE_INFINITY));
                assertDecEquals(NEGATIVE_INFINITY, min(ONE, NEGATIVE_INFINITY));
                assertDecEquals(ONE, max(ONE, NEGATIVE_INFINITY));
            }

            @Test
            public void one_positive_infinity() {
                assertEquals(-1, compare(ONE, POSITIVE_INFINITY));
                assertFalse(equal(ONE, POSITIVE_INFINITY));
                assertDecEquals(ONE, min(ONE, POSITIVE_INFINITY));
                assertDecEquals(POSITIVE_INFINITY, max(ONE, POSITIVE_INFINITY));
            }

            @Test
            public void one_zero() {
                assertEquals(1, compare(ONE, ZERO));
                assertFalse(equal(ONE, ZERO));
                assertDecEquals(ZERO, min(ONE, ZERO));
                assertDecEquals(ONE, max(ONE, ZERO));
            }

            @Test
            public void one_one() {
                assertEquals(0, compare(ONE, ONE));
                assertTrue(equal(ONE, ONE));
                assertDecEquals(ONE, min(ONE, ONE));
                assertDecEquals(ONE, max(ONE, ONE));
            }

            @Test
            public void case_0000() {
                // 0 vs. 1
                assertEquals(-1, compare(ZERO, ONE));
                assertEquals(1, compare(ONE, ZERO));
            }

            @Test
            public void case_0001() {
                // -1 vs. 1
                var a = fromParts(-1, 0);
                var b = fromParts(1, 0);
                assertEquals(-1, compare(a, b));
                assertEquals(1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(a, min(a, b));
                assertDecEquals(b, max(a, b));
            }

            @Test
            public void case_0002() {
                // -123 vs. 1
                var a = fromParts(-123, 0);
                var b = fromParts(1, 0);
                assertEquals(-1, compare(a, b));
                assertEquals(1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(a, min(a, b));
                assertDecEquals(b, max(a, b));
            }

            @Test
            public void case_0003() {
                // -123 vs. -1234
                var a = fromParts(-123, 0);
                var b = fromParts(-1234, 0);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0004() {
                // -1230 vs. -1234
                var a = fromParts(-1230, 0);
                var b = fromParts(-1234, 0);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0005() {
                // 12300 vs. 1234
                var a = fromParts(12300, 0);
                var b = fromParts(1234, 0);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0006() {
                // 31400 vs. 31415
                var a = fromParts(31400, 0);
                var b = fromParts(31415, 0);
                assertEquals(-1, compare(a, b));
                assertEquals(1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(a, min(a, b));
                assertDecEquals(b, max(a, b));
            }

            @Test
            public void case_0007() {
                // 1 vs. 0.1
                var a = fromParts(1, 0);
                var b = fromParts(1, 1);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0008() {
                // 3 vs. 2.14
                var a = fromParts(3, 0);
                var b = fromParts(214, 2);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0009() {
                // 3 vs. 314
                var a = fromParts(3, 0);
                var b = fromParts(314, 2);
                assertEquals(-1, compare(a, b));
                assertEquals(1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(a, min(a, b));
                assertDecEquals(b, max(a, b));
            }

            @Test
            public void case_0010() {
                // 0.1 vs. 0.015
                var a = fromParts(1, 1);
                var b = fromParts(15, 3);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0011() {
                // 0.749186821717031 vs. 0.4623712111284055
                var a = fromParts(7491868217170316L, 15);
                var b = fromParts(4623712111284055L, 16);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0012() {
                // 0.000749186821717031 vs. 0.004623712111284055
                var a = fromParts(7491868217170316L, 19);
                var b = fromParts(4623712111284055L, 18);
                assertEquals(-1, compare(a, b));
                assertEquals(1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(a, min(a, b));
                assertDecEquals(b, max(a, b));
            }

            @Test
            public void case_0013() {
                // 0.000749186821717031 vs. -0.004623712111284055
                var a = fromParts(7491868217170316L, 19);
                var b = fromParts(-4623712111284055L, 18);
                assertEquals(1, compare(a, b));
                assertEquals(-1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(b, min(a, b));
                assertDecEquals(a, max(a, b));
            }

            @Test
            public void case_0014() {
                // -154242002030.3676 vs. -89837112701.43
                var a = fromParts(-1542420020303676L, 4);
                var b = fromParts(-8983711270143L, 2);
                assertEquals(-1, compare(a, b));
                assertEquals(1, compare(b, a));
                assertFalse(equal(a, b));
                assertFalse(equal(b, a));
                assertDecEquals(a, min(a, b));
                assertDecEquals(b, max(a, b));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, (a, b) -> {
                    var expected = toBigDecimal(a).compareTo(toBigDecimal(b));

                    assertEquals(expected, compare(a, b), triplet(a) + ", " + triplet(b));
                    assertEquals(expected * -1, compare(b, a), triplet(b) + ", " + triplet(a));

                    if (expected == 0) {
                        assertTrue(equal(a, b));
                        assertTrue(equal(b, a));
                        assertDecEquals(a, min(a, b));
                        assertDecEquals(a, max(a, b));
                    } else {
                        assertFalse(equal(a, b));
                        assertFalse(equal(b, a));
                        if (expected > 0) {
                            assertDecEquals(b, min(a, b));
                            assertDecEquals(a, max(a, b));
                        } else {
                            assertDecEquals(a, min(a, b));
                            assertDecEquals(b, max(a, b));
                        }
                    }
                });
            }
        }
    }

    @Nested
    class Maths {

        @Nested
        class Abs {

            @Test
            public void nan() {
                assertEquals(NAN, abs(NAN));
            }

            @Test
            public void negative_infinity() {
                assertEquals(POSITIVE_INFINITY, abs(NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity() {
                assertEquals(POSITIVE_INFINITY, abs(POSITIVE_INFINITY));
            }

            @Test
            public void zero() {
                assertEquals(ZERO, abs(ZERO));
            }

            @Test
            public void one() {
                assertEquals(ONE, abs(ONE));
            }

            @Test
            public void case_0000() {
                var x = fromParts(5211, 7);
                assertEquals(x, abs(x));
            }

            @Test
            public void case_0001() {
                var x = fromParts(-5211, 7);
                var expected = fromParts(5211, 7);
                assertEquals(expected, abs(x));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, decimal -> {
                    var expected = fromParts(Math.abs(getMantissa(decimal)), getExponent(decimal));
                    assertEquals(expected, abs(decimal), triplet(decimal));
                });
            }
        }

        @Nested
        class Negate {

            @Test
            public void nan() {
                assertDecEquals(NAN, negate(NAN));
            }

            @Test
            public void negative_infinity() {
                assertDecEquals(POSITIVE_INFINITY, negate(NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, negate(POSITIVE_INFINITY));
            }

            @Test
            public void zero() {
                assertDecEquals(ZERO, negate(ZERO));
            }

            @Test
            public void one() {
                var expected = fromParts(-1, 0);
                assertDecEquals(expected, negate(ONE));
            }

            @Test
            public void case_0000() {
                var x = fromParts(5211, 7);
                var expected = fromParts(-5211, 7);
                assertDecEquals(expected, negate(x));
            }

            @Test
            public void case_0001() {
                var x = fromParts(-5211, 7);
                var expected = fromParts(5211, 7);
                assertDecEquals(expected, negate(x));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, decimal -> {
                    var expected = fromParts(-getMantissa(decimal), getExponent(decimal));
                    assertDecEquals(expected, negate(decimal));
                });
            }
        }

        @Nested
        class Add {

            @Test
            public void nan_nan() {
                assertDecEquals(NAN, add(NAN, NAN));
            }

            @Test
            public void nan_negative_infinity() {
                assertDecEquals(NAN, add(NAN, NEGATIVE_INFINITY));
            }

            @Test
            public void nan_positive_infinity() {
                assertDecEquals(NAN, add(NAN, POSITIVE_INFINITY));
            }

            @Test
            public void nan_zero() {
                assertDecEquals(NAN, add(NAN, ZERO));
            }

            @Test
            public void nan_one() {
                assertDecEquals(NAN, add(NAN, ONE));
            }

            @Test
            public void negative_infinity_nan() {
                assertDecEquals(NAN, add(NEGATIVE_INFINITY, NAN));
            }

            @Test
            public void negative_infinity_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, add(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void negative_infinity_positive_infinity() {
                assertDecEquals(NAN, add(NEGATIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void negative_infinity_zero() {
                assertDecEquals(NEGATIVE_INFINITY, add(NEGATIVE_INFINITY, ZERO));
            }

            @Test
            public void negative_infinity_one() {
                assertDecEquals(NEGATIVE_INFINITY, add(NEGATIVE_INFINITY, ONE));
            }

            @Test
            public void positive_infinity_nan() {
                assertDecEquals(NAN, add(POSITIVE_INFINITY, NAN));
            }

            @Test
            public void positive_infinity_negative_infinity() {
                assertDecEquals(NAN, add(POSITIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, add(POSITIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void positive_infinity_zero() {
                assertDecEquals(POSITIVE_INFINITY, add(POSITIVE_INFINITY, ZERO));
            }

            @Test
            public void positive_infinity_one() {
                assertDecEquals(POSITIVE_INFINITY, add(POSITIVE_INFINITY, ONE));
            }

            @Test
            public void zero_nan() {
                assertDecEquals(NAN, add(ZERO, NAN));
            }

            @Test
            public void zero_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, add(ZERO, NEGATIVE_INFINITY));
            }

            @Test
            public void zero_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, add(ZERO, POSITIVE_INFINITY));
            }

            @Test
            public void zero_zero() {
                assertDecEquals(ZERO, add(ZERO, ZERO));
            }

            @Test
            public void zero_one() {
                assertDecEquals(ONE, add(ZERO, ONE));
            }

            @Test
            public void one_nan() {
                assertDecEquals(NAN, add(ONE, NAN));
            }

            @Test
            public void one_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, add(ONE, NEGATIVE_INFINITY));
            }

            @Test
            public void one_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, add(ONE, POSITIVE_INFINITY));
            }

            @Test
            public void one_zero() {
                assertDecEquals(ONE, add(ONE, ZERO));
            }

            @Test
            public void one_one() {
                assertDecEquals(TWO, add(ONE, ONE));
            }

            @Test
            public void case_0000() {
                // 0 + -1 = -1
                var a = ZERO;
                var b = fromParts(-1, 0);
                var expected = fromParts(-1, 0);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0001() {
                // -1.1 + -2.2 = -3.3
                var a = fromParts(-11, 1);
                var b = fromParts(-22, 1);
                var expected = fromParts(-33, 1);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0002() {
                // 3.14159 + 0.0001 = 3.14169
                var a = fromParts(314159, 5);
                var b = fromParts(1, 4);
                var expected = fromParts(314169, 5);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0003() {
                // 3.14159 + 150 = 153.14159
                var a = fromParts(314159, 5);
                var b = fromParts(150, 0);
                var expected = fromParts(15314159, 5);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0004() {
                // 3.14159 + 0.0000012 = 3.1415912
                var a = fromParts(314159, 5);
                var b = fromParts(12, 7);
                var expected = fromParts(31415912, 7);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0005() {
                // -0.00000005 + 1000000000 = 999999999.99999995
                var a = fromParts(-5, 8);
                var b = fromParts(1, -9);
                var expected = fromParts(1, -9);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0006() {
                // -0.000000008104 + 39950000 = 39949999.999999991896
                var a = fromParts(-8104, 12);
                var b = fromParts(39950000, 0);
                var expected = fromParts(3994999999999999L, 8);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0007() {
                // -0.00000006 + 100000000 = 99999999.99999994
                var a = fromParts(-6, 8);
                var b = fromParts(1, -8);
                var expected = fromParts(9999999999999994L, 8);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0008() {
                // 0.00000002 + -200000000 = -199999999.99999998
                var a = fromParts(2, 8);
                var b = fromParts(-2, -8);
                // var expected = fromParts(-1999999999999999L, 7); // if truncating
                var expected = fromParts(-2, -8);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0009() {
                // -0.00000053 + -800000000 = -800000000.00000053
                var a = fromParts(-53, 8);
                var b = fromParts(-8, -8);
                var expected = fromParts(-8000000000000005L, 7);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0010() {
                // 0.0000032 + -7000000000 = -6999999999.9999968
                var a = fromParts(32, 7);
                var b = fromParts(-7, -9);
                // var expected = fromParts(-6999999999999996L, 6); // if truncating
                var expected = fromParts(-6999999999999997L, 6);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0011() {
                // -3800000000 + 0.00000078 = -3799999999.99999922
                var a = fromParts(-38, -8);
                var b = fromParts(78, 8);
                var expected = fromParts(-3799999999999999L, 6);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0020() {
                // 1230.0000123 + 7.89 = 1237.8900123
                var a = fromParts(12300000123L, 7);
                var b = fromParts(789, 2);
                var expected = fromParts(12378900123L, 7);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0021() {
                // 123 + 1e-150 = 123
                var a = fromParts(123, 0);
                var b = fromParts(1, 150);
                var expected = fromParts(123, 0);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0022() {
                // -1230 + 0.0000000000000123 = -1229.9999999999999877
                var a = fromParts(-1230, 0);
                var b = fromParts(123, 16);
                // var expected = fromParts(-1229999999999999L, 12); // if truncating
                var expected = fromParts(-123, -1);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0023() {
                // 1230 + -0.0000000000000123 = 1229.9999999999999877
                var a = fromParts(1230, 0);
                var b = fromParts(-123, 16);
                // var expected = fromParts(1229999999999999L, 12); // if truncating
                var expected = fromParts(123, -1);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0024() {
                // 1e+28 + 19e+12 = 10000000000000019000000000000
                var a = fromParts(1, -28);
                var b = fromParts(19, -12);
                var expected = fromParts(1000000000000002L, -13);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0025() {
                // 5e-11 + 19e-27 = 0.000000000050000000000000019
                var a = fromParts(5, 11);
                var b = fromParts(19, 27);
                var expected = fromParts(5000000000000002L, 26);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0026() {
                // 1e+17 + 1e-18 = 1e17
                var a = fromParts(1, -17);
                var b = fromParts(1, 18);
                var expected = fromParts(1, -17);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0027() {
                //   -878594030459625900000
                // + -4310444807979995000000000000
                // = -4310445686574025459625900000
                var a = fromParts(-8785940304596259L, -5);
                var b = fromParts(-4310444807979995L, -12);
                var expected = fromParts(-4310445686574025L, -12);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0028() {
                // -100000000000000000000000 + -0.1
                var a = fromParts(-1, -23);
                var b = fromParts(-1, 1);
                var expected = fromParts(-1, -23);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0029() {
                // -4475801326363716e-174 + -8653470778602982e-174 = -13129272104966698e-174
                var a = fromParts(-4475801326363716L, 174);
                var b = fromParts(-8653470778602982L, 174);
                var expected = fromParts(-131292721049667L, 172);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0030() {
                // -0.00000059 + -8000000000 = -8000000000.00000059
                var a = fromParts(-59, 8);
                var b = fromParts(-8, -9);
                var expected = fromParts(-8000000000000001L, 6);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0031() {
                //    0.0000000000000000000000000000000000558053661
                // + -0.00000000000000000000007287311484
                // = -0.00000000000000000000007287311483994419
                var a = fromParts(558053661, 43);
                var b = fromParts(-7287311484L, 32);
                var expected = fromParts(-7287311483994419L, 38);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0032() {
                //    0.000000000000000000000000000000000000000000000905
                // + -0.0000000000000000000000000000000346
                // = -0.0000000000000000000000000000000345999999999991
                var a = fromParts(905, 48);
                var b = fromParts(-346, 34);
                var expected = fromParts(-345999999999991L, 46);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0033() {
                //            -367544450000000
                // + -413841870000000000000000
                // = -413841870367544450000000
                // = -413841870367544400000000
                var a = fromParts(-36754445, -7);
                var b = fromParts(-41384187, -16);
                var expected = fromParts(-4138418703675444L, -8);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0034() {
                //            568638595800000000000000000
                // + -66693442800000000000000000000000000
                // = -66693442231361404200000000000000000
                // = -66693442231361400000000000000000000
                var a = fromParts(5686385958L, -17);
                var b = fromParts(-666934428, -26);
                var expected = fromParts(-666934422313614L, -20);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0035() {
                //           6484106154174000000000000000000000000000
                // + 112976361541600000000000000000000000000000000000
                // = 112976368025706154174000000000000000000000000000
                // = 112976368025706200000000000000000000000000000000
                var a = fromParts(6484106154174L, -27);
                var b = fromParts(1129763615416L, -35);
                var expected = fromParts(1129763680257062L, -32);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0036() {
                //    -6651618724675.716
                // + 106249257170282.4
                // =  99597638445606.684
                // =  99597638445606.68
                var a = fromParts(-6651618724675716L, 3);
                var b = fromParts(1062492571702824L, 1);
                var expected = fromParts(9959763844560668L, 2);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0037() {
                //   -0.000000000000000000000000000000000000000060453245
                // +  0.0000000000000000000000000000000000000000000000000000080545057
                // = -0.0000000000000000000000000000000000000000604532449999919454943
                // = -0.00000000000000000000000000000000000000006045324499999195 ==>
                var a = fromParts(-60453245, 48);
                var b = fromParts(80545057, 61);
                var expected = fromParts(-6045324499999195L, 56);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0038() {
                //                 -25024963.58732
                // + 91140510223610000000000
                // = 91140510223609974975036.41268
                // = 91140510223609970000000
                var a = fromParts(-2502496358732L, 5);
                var b = fromParts(9114051022361L, -10);
                var expected = fromParts(9114051022360997L, -7);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0039() {
                //   -0.0000001
                //  + 0.000000000000000000000084
                // = -0.000000099999999999999916
                // = -0.00000009999999999999992
                var a = fromParts(-1, 7);
                var b = fromParts(84, 24);
                var expected = fromParts(-9999999999999992L, 23);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0040() {
                //     0.000000053530774993981
                // + -83.902273458533
                // = -83.902273405002225006019
                // = -83.90227340500223
                var a = fromParts(53530774993981L, 21);
                var b = fromParts(-83902273458533L, 12);
                var expected = fromParts(-8390227340500223L, 14);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void case_0041() {
                //    0.0000000000000000009
                // + -0.01
                // = -0.0099999999999999991
                // = -0.009999999999999999
                var a = fromParts(9, 19);
                var b = fromParts(-1, 2);
                var expected = fromParts(-9999999999999999L, 18);

                assertDecEquals(expected, add(a, b));
                assertDecEquals(expected, add(b, a));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, (a, b) -> {
                    var expected = toBigDecimal(a).add(toBigDecimal(b), MathContext.DECIMAL64).stripTrailingZeros();

                    assertEquals(expected, toBigDecimal(add(a, b)), () -> {
                        var _a = toBigDecimal(a);
                        var _b = toBigDecimal(b);
                        return String.format("%s %s + %s %s = %s -> %s",
                                _a.toPlainString(), Decimal64.tuple(a),
                                _b.toPlainString(), Decimal64.tuple(b),
                                _a.add(_b, MathContext.UNLIMITED).toPlainString(),
                                _a.add(_b, MathContext.DECIMAL64).toPlainString()
                        );
                    });
                    assertEquals(expected, toBigDecimal(add(b, a)), () -> Decimal64.tuple(b) + " + " + Decimal64.tuple(a));
                });
            }
        }

        @Nested
        class Sub {

            @Test
            public void nan_nan() {
                assertDecEquals(NAN, sub(NAN, NAN));
            }

            @Test
            public void nan_negative_infinity() {
                assertDecEquals(NAN, sub(NAN, NEGATIVE_INFINITY));
            }

            @Test
            public void nan_positive_infinity() {
                assertDecEquals(NAN, sub(NAN, POSITIVE_INFINITY));
            }

            @Test
            public void nan_zero() {
                assertDecEquals(NAN, sub(NAN, ZERO));
            }

            @Test
            public void nan_one() {
                assertDecEquals(NAN, sub(NAN, ONE));
            }

            @Test
            public void negative_infinity_nan() {
                assertDecEquals(NAN, sub(NEGATIVE_INFINITY, NAN));
            }

            @Test
            public void negative_infinity_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, sub(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void negative_infinity_positive_infinity() {
                assertDecEquals(NAN, sub(NEGATIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void negative_infinity_zero() {
                assertDecEquals(NEGATIVE_INFINITY, sub(NEGATIVE_INFINITY, ZERO));
            }

            @Test
            public void negative_infinity_one() {
                assertDecEquals(NEGATIVE_INFINITY, sub(NEGATIVE_INFINITY, ONE));
            }

            @Test
            public void positive_infinity_nan() {
                assertDecEquals(NAN, sub(POSITIVE_INFINITY, NAN));
            }

            @Test
            public void positive_infinity_negative_infinity() {
                assertDecEquals(NAN, sub(POSITIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, sub(POSITIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void positive_infinity_zero() {
                assertDecEquals(POSITIVE_INFINITY, sub(POSITIVE_INFINITY, ZERO));
            }

            @Test
            public void positive_infinity_one() {
                assertDecEquals(POSITIVE_INFINITY, sub(POSITIVE_INFINITY, ONE));
            }

            @Test
            public void zero_nan() {
                assertDecEquals(NAN, sub(ZERO, NAN));
            }

            @Test
            public void zero_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, sub(ZERO, NEGATIVE_INFINITY));
            }

            @Test
            public void zero_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, sub(ZERO, POSITIVE_INFINITY));
            }

            @Test
            public void zero_zero() {
                assertDecEquals(ZERO, sub(ZERO, ZERO));
            }

            @Test
            public void zero_one() {
                var expected = fromParts(-1, 0);
                assertDecEquals(expected, sub(ZERO, ONE));
            }

            @Test
            public void one_nan() {
                assertDecEquals(NAN, sub(ONE, NAN));
            }

            @Test
            public void one_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, sub(ONE, NEGATIVE_INFINITY));
            }

            @Test
            public void one_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, sub(ONE, POSITIVE_INFINITY));
            }

            @Test
            public void one_zero() {
                assertDecEquals(ONE, sub(ONE, ZERO));
            }

            @Test
            public void one_one() {
                assertDecEquals(ZERO, sub(ONE, ONE));
            }

            @Test
            public void case_0000() {
                // 0 - -1 = 1
                var b = fromParts(-1, 0);
                var expected = fromParts(1, 0);

                assertDecEquals(expected, sub(ZERO, b));
            }

            @Test
            public void case_0001() {
                // -1.1 - -2.2 = -3.3
                var a = fromParts(-11, 1);
                var b = fromParts(-22, 1);
                var expected = fromParts(11, 1);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0002() {
                // 3.14159 - 0.0001 = 3.14149
                var a = fromParts(314159, 5);
                var b = fromParts(1, 4);
                var expected = fromParts(314149, 5);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0003() {
                // 3.14159 - 150 = -146.85841
                var a = fromParts(314159, 5);
                var b = fromParts(150, 0);
                var expected = fromParts(-14685841, 5);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0004() {
                // 3.14159 - 0.0000012 = 3.1415888
                var a = fromParts(314159, 5);
                var b = fromParts(12, 7);
                var expected = fromParts(31415888, 7);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0005() {
                // -0.00000005 - 1000000000 = -1000000000.00000005
                var a = fromParts(-5, 8);
                var b = fromParts(1, -9);
                var expected = fromParts(-1, -9);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0006() {
                // -0.000000008104 - 39950000 = -39950000.000000008104
                var a = fromParts(-8104, 12);
                var b = fromParts(39950000, 0);
                var expected = fromParts(-3995000000000001L, 8);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0007() {
                // -0.00000006 - 100000000 = -100000000.00000006
                var a = fromParts(-6, 8);
                var b = fromParts(1, -8);
                var expected = fromParts(-1000000000000001L, 7);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0008() {
                // 0.00000002 - -200000000 = 200000000.00000002
                var a = fromParts(2, 8);
                var b = fromParts(-2, -8);
                var expected = fromParts(2, -8);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0009() {
                // -0.00000053 - -800000000 = 799999999.99999947
                var a = fromParts(-53, 8);
                var b = fromParts(-8, -8);
                var expected = fromParts(7999999999999995L, 7);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0010() {
                // 0.0000032 - -7000000000 = 7000000000.0000032
                var a = fromParts(32, 7);
                var b = fromParts(-7, -9);
                var expected = fromParts(7000000000000003L, 6);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0011() {
                // -3800000000 - 0.00000078 = -3800000000.00000078
                var a = fromParts(-38, -8);
                var b = fromParts(78, 8);
                var expected = fromParts(-3800000000000001L, 6);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0020() {
                // 1230.0000123 - 7.89 = 1222.1100123
                var a = fromParts(12300000123L, 7);
                var b = fromParts(789, 2);
                var expected = fromParts(12221100123L, 7);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0021() {
                // 123 - 1e-150 = -122.99999999999999999999999999...
                var a = fromParts(123, 0);
                var b = fromParts(1, 150);
                var expected = fromParts(123, 0);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0022() {
                // -1230 - 0.0000000000000123 = -1230.0000000000000123
                var a = fromParts(-1230, 0);
                var b = fromParts(123, 16);
                var expected = fromParts(-123, -1);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0023() {
                // 1230 - -0.0000000000000123 = 1230.0000000000000123
                var a = fromParts(1230, 0);
                var b = fromParts(-123, 16);
                var expected = fromParts(123, -1);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0024() {
                // 1e+28 - 19e+12 = 9999999999999981000000000000
                var a = fromParts(1, -28);
                var b = fromParts(19, -12);
                var expected = fromParts(9999999999999981L, -12);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0025() {
                // 5e-11 - 19e-27 = 0.000000000049999999999999981
                var a = fromParts(5, 11);
                var b = fromParts(19, 27);
                var expected = fromParts(4999999999999998L, 26);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0026() {
                // 1e+17 - 1e-18 = 99999999999999999.999999999999999999
                var a = fromParts(1, -17);
                var b = fromParts(1, 18);
                var expected = fromParts(1, -17);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0027() {
                // -878594030459625900000 - -4310444807979995000000000000 = 4310443929385964540374100000
                var a = fromParts(-8785940304596259L, -5);
                var b = fromParts(-4310444807979995L, -12);
                var expected = fromParts(4310443929385965L, -12);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0028() {
                // -100000000000000000000000 - -0.1 = -99999999999999999999999.9
                var a = fromParts(-1, -23);
                var b = fromParts(-1, 1);
                var expected = fromParts(-1, -23);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void case_0029() {
                // -4475801326363716e-174 - -8653470778602982e-174 = 4177669452239266e-174
                var a = fromParts(-4475801326363716L, 174);
                var b = fromParts(-8653470778602982L, 174);
                var expected = fromParts(4177669452239266L, 174);

                assertDecEquals(expected, sub(a, b));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, (a, b) -> {
                    var expected = toBigDecimal(a).subtract(toBigDecimal(b), MathContext.DECIMAL64).stripTrailingZeros();

                    assertEquals(expected, toBigDecimal(sub(a, b)), () -> Decimal64.toString(a) + " - " + Decimal64.toString(b));
                });
            }
        }

        @Nested
        class Mul {

            @Test
            public void nan_nan() {
                assertDecEquals(NAN, mul(NAN, NAN));
            }

            @Test
            public void nan_negative_infinity() {
                assertDecEquals(NAN, mul(NAN, NEGATIVE_INFINITY));
            }

            @Test
            public void nan_positive_infinity() {
                assertDecEquals(NAN, mul(NAN, POSITIVE_INFINITY));
            }

            @Test
            public void nan_zero() {
                assertDecEquals(NAN, mul(NAN, ZERO));
            }

            @Test
            public void nan_one() {
                assertDecEquals(NAN, mul(NAN, ONE));
            }

            @Test
            public void negative_infinity_nan() {
                assertDecEquals(NAN, mul(NEGATIVE_INFINITY, NAN));
            }

            @Test
            public void negative_infinity_negative_infinity() {
                assertDecEquals(POSITIVE_INFINITY, mul(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void negative_infinity_positive_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, mul(NEGATIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void negative_infinity_zero() {
                assertDecEquals(NAN, mul(NEGATIVE_INFINITY, ZERO));
            }

            @Test
            public void negative_infinity_one() {
                assertDecEquals(NEGATIVE_INFINITY, mul(NEGATIVE_INFINITY, ONE));
            }

            @Test
            public void positive_infinity_nan() {
                assertDecEquals(NAN, mul(POSITIVE_INFINITY, NAN));
            }

            @Test
            public void positive_infinity_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, mul(POSITIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, mul(POSITIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void positive_infinity_zero() {
                assertDecEquals(NAN, mul(POSITIVE_INFINITY, ZERO));
            }

            @Test
            public void positive_infinity_one() {
                assertDecEquals(POSITIVE_INFINITY, mul(POSITIVE_INFINITY, ONE));
            }

            @Test
            public void zero_nan() {
                assertDecEquals(NAN, mul(ZERO, NAN));
            }

            @Test
            public void zero_negative_infinity() {
                assertDecEquals(NAN, mul(ZERO, NEGATIVE_INFINITY));
            }

            @Test
            public void zero_positive_infinity() {
                assertDecEquals(NAN, mul(ZERO, POSITIVE_INFINITY));
            }

            @Test
            public void zero_zero() {
                assertDecEquals(ZERO, mul(ZERO, ZERO));
            }

            @Test
            public void zero_one() {
                assertDecEquals(ZERO, mul(ZERO, ONE));
            }

            @Test
            public void one_nan() {
                assertDecEquals(NAN, mul(ONE, NAN));
            }

            @Test
            public void one_negative_infinity() {
                assertDecEquals(NEGATIVE_INFINITY, mul(ONE, NEGATIVE_INFINITY));
            }

            @Test
            public void one_positive_infinity() {
                assertDecEquals(POSITIVE_INFINITY, mul(ONE, POSITIVE_INFINITY));
            }

            @Test
            public void one_zero() {
                assertDecEquals(ZERO, mul(ONE, ZERO));
            }

            @Test
            public void one_one() {
                assertDecEquals(ONE, mul(ONE, ONE));
            }

            @Test
            public void case_0000() {
                // 0 * -1 = -1
                var a = ZERO;
                var b = fromParts(-1, 0);
                var expected = ZERO;

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0001() {
                // -1.1 * -2.2 = -2.42
                var a = fromParts(-11, 1);
                var b = fromParts(-22, 1);
                var expected = fromParts(242, 2);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0002() {
                // 3.14159 * 0.0001 = 0.000314159
                var a = fromParts(314159, 5);
                var b = fromParts(1, 4);
                var expected = fromParts(314159, 9);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0003() {
                // 3.14159 * 150 = 471.2385
                var a = fromParts(314159, 5);
                var b = fromParts(150, 0);
                var expected = fromParts(4712385, 4);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0004() {
                // 3.14159 * 0.0000012 = 0.000003769908
                var a = fromParts(314159, 5);
                var b = fromParts(12, 7);
                var expected = fromParts(3769908, 12);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0005() {
                // -0.00000005 * 1000000000 = -50
                var a = fromParts(-5, 8);
                var b = fromParts(1, -9);
                var expected = fromParts(-50, 0);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0006() {
                // -0.000000008104 * 39950000 = -0.3237548
                var a = fromParts(-8104, 12);
                var b = fromParts(39950000, 0);
                var expected = fromParts(-3237548, 7);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0007() {
                // -0.00000006 * 100000000 = -6
                var a = fromParts(-6, 8);
                var b = fromParts(1, -8);
                var expected = fromParts(-6, 0);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0008() {
                // 0.00000002 * -200000000 = -4
                var a = fromParts(2, 8);
                var b = fromParts(-2, -8);
                var expected = fromParts(-4, 0);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0009() {
                // 0.00000053 * -800000000 = -424
                var a = fromParts(53, 8);
                var b = fromParts(-8, -8);
                var expected = fromParts(-424, 0);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0010() {
                // 0.0000032 * -7000000000 = -22400
                var a = fromParts(32, 7);
                var b = fromParts(-7, -9);
                var expected = fromParts(-22400, 0);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0011() {
                // -3800000000 * 0.00000078 = -2964
                var a = fromParts(-38, -8);
                var b = fromParts(78, 8);
                var expected = fromParts(-2964, 0);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0020() {
                // 1230.0000123 * 7.89 = 9704.700097047
                var a = fromParts(12300000123L, 7);
                var b = fromParts(789, 2);
                var expected = fromParts(9704700097047L, 9);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0021() {
                // 123 * 1e-150 = 123e-150
                var a = fromParts(123, 0);
                var b = fromParts(1, 150);
                var expected = fromParts(123, 150);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0022() {
                // -1230 * 0.0000000000000123 = -0.000000000015129
                var a = fromParts(-1230, 0);
                var b = fromParts(123, 16);
                var expected = fromParts(-15129, 15);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0023() {
                // 1230 * -0.0000000000000123 = -0.000000000015129
                var a = fromParts(1230, 0);
                var b = fromParts(-123, 16);
                var expected = fromParts(-15129, 15);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0024() {
                // 1e+28 * 19e+12 = 19e40
                var a = fromParts(1, -28);
                var b = fromParts(19, -12);
                var expected = fromParts(19, -40);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0025() {
                // 5e-11 * 19e-27 = 95e-38
                var a = fromParts(5, 11);
                var b = fromParts(19, 27);
                var expected = fromParts(95, 38);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0026() {
                // 1e+17 * 1e-18 = 0.1
                var a = fromParts(1, -17);
                var b = fromParts(1, 18);
                var expected = fromParts(1, 1);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0027() {
                // -878594030459625900000 * -4310444807979995000000000000 = 3787131076916912040697982383870500000000000000000
                var a = fromParts(-8785940304596259L, -5);
                var b = fromParts(-4310444807979995L, -12);
                var expected = fromParts(3787131076916912L, -33);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0028() {
                // -4475801326363716e-174 * -8653470778602982e-174 = -13129272104966698e-174
                var a = fromParts(-4475801326363716L, 174);
                var b = fromParts(-8653470778602982L, 174);
                var expected = ZERO;

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0029() {
                // -6e-128 * -17e-128 = 102e-256 -> 1e-254
                var a = fromParts(-6, 128);
                var b = fromParts(-17, 128);
                var expected = fromParts(1, 254);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0030() {
                // 74743267600 * -9741.28827 = -728095715933351.052 -> -728095715933351.1
                var a = fromParts(74743267600L, 0);
                var b = fromParts(-974128827, 5);
                var expected = fromParts(-7280957159333511L, 1);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0031() {
                // 93735e-173 * -65215e-83 = 6112928025e-256 -> -611292802e-255
                var a = fromParts(93735, 173);
                var b = fromParts(-65215, 83);
                var expected = fromParts(-611292802, 255);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0032() {
                // 3583642e-85 * -1828025e-174 = -6550987167050e-259 -> 655098717e-255
                var a = fromParts(3583642, 85);
                var b = fromParts(-1828025, 174);
                var expected = fromParts(-655098717, 255);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0033() {
                // 3436348972-e166 * -20632984e-106 = -70902133357692448e-272 -> 0
                var a = fromParts(3436348972L, 166);
                var b = fromParts(-20632984, 106);
                var expected = ZERO;

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void case_0034() {
                // 61322761611395e-149 * 158969636387037e-133 = 9748457115612414e-270 -> 1e-254
                var a = fromParts(61322761611395L, 149);
                var b = fromParts(158969636387037L, 133);
                var expected = fromParts(1, 254);

                assertDecEquals(expected, mul(a, b));
                assertDecEquals(expected, mul(b, a));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, (a, b) -> {
                    var expected = toBigDecimal(a).multiply(toBigDecimal(b), MathContext.DECIMAL64).stripTrailingZeros();

                    if (expected.scale() < MIN_EXPONENT) {
                        // overflow to +/- inf
                        var expectedDec = FastMath.sameSign(a, b) ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                        assertEquals(expectedDec, mul(a, b), () -> Decimal64.toString(a) + " * " + Decimal64.toString(b));
                        assertEquals(expectedDec, mul(b, a), () -> Decimal64.toString(a) + " * " + Decimal64.toString(b));
                        return;
                    }

                    if (expected.scale() - expected.precision() >= MAX_EXPONENT) {
                        // underflow to zero
                        expected = BigDecimal.ZERO;
                    } else if (expected.scale() > MAX_EXPONENT) {
                        // e.g. 102e-256 -> 1e-254
                        expected = expected.setScale(MAX_EXPONENT, RoundingMode.HALF_EVEN).stripTrailingZeros();
                    }

                    // normal finite result
                    assertEquals(expected, toBigDecimal(mul(a, b)), () -> Decimal64.toString(a) + " * " + Decimal64.toString(b));
                    assertEquals(expected, toBigDecimal(mul(b, a)), () -> Decimal64.toString(b) + " * " + Decimal64.toString(a));
                });
            }
        }

        @Nested
        class Div {

            @Test
            public void nan_nan() {
                assertDecEquals(NAN, div(NAN, NAN));
            }

            @Test
            public void nan_negative_infinity() {
                assertDecEquals(NAN, div(NAN, NEGATIVE_INFINITY));
            }

            @Test
            public void nan_positive_infinity() {
                assertDecEquals(NAN, div(NAN, POSITIVE_INFINITY));
            }

            @Test
            public void nan_zero() {
                assertDecEquals(NAN, div(NAN, ZERO));
            }

            @Test
            public void nan_one() {
                assertDecEquals(NAN, div(NAN, ONE));
            }

            @Test
            public void negative_infinity_nan() {
                assertDecEquals(NAN, div(NEGATIVE_INFINITY, NAN));
            }

            @Test
            public void negative_infinity_negative_infinity() {
                assertDecEquals(NAN, div(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void negative_infinity_positive_infinity() {
                assertDecEquals(NAN, div(NEGATIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void negative_infinity_zero() {
                assertDecEquals(NAN, div(NEGATIVE_INFINITY, ZERO));
            }

            @Test
            public void negative_infinity_one() {
                assertDecEquals(NEGATIVE_INFINITY, div(NEGATIVE_INFINITY, ONE));
            }

            @Test
            public void positive_infinity_nan() {
                assertDecEquals(NAN, div(POSITIVE_INFINITY, NAN));
            }

            @Test
            public void positive_infinity_negative_infinity() {
                assertDecEquals(NAN, div(POSITIVE_INFINITY, NEGATIVE_INFINITY));
            }

            @Test
            public void positive_infinity_positive_infinity() {
                assertDecEquals(NAN, div(POSITIVE_INFINITY, POSITIVE_INFINITY));
            }

            @Test
            public void positive_infinity_zero() {
                assertDecEquals(NAN, div(POSITIVE_INFINITY, ZERO));
            }

            @Test
            public void positive_infinity_one() {
                assertDecEquals(POSITIVE_INFINITY, div(POSITIVE_INFINITY, ONE));
            }

            @Test
            public void zero_nan() {
                assertDecEquals(NAN, div(ZERO, NAN));
            }

            @Test
            public void zero_negative_infinity() {
                assertDecEquals(ZERO, div(ZERO, NEGATIVE_INFINITY));
            }

            @Test
            public void zero_positive_infinity() {
                assertDecEquals(ZERO, div(ZERO, POSITIVE_INFINITY));
            }

            @Test
            public void zero_zero() {
                assertDecEquals(NAN, div(ZERO, ZERO));
            }

            @Test
            public void zero_one() {
                assertDecEquals(ZERO, div(ZERO, ONE));
            }

            @Test
            public void one_nan() {
                assertDecEquals(NAN, div(ONE, NAN));
            }

            @Test
            public void one_negative_infinity() {
                assertDecEquals(ZERO, div(ONE, NEGATIVE_INFINITY));
            }

            @Test
            public void one_positive_infinity() {
                assertDecEquals(ZERO, div(ONE, POSITIVE_INFINITY));
            }

            @Test
            public void one_zero() {
                assertDecEquals(NAN, div(ONE, ZERO));
            }

            @Test
            public void one_one() {
                assertDecEquals(ONE, div(ONE, ONE));
            }

            @Test
            public void case_0000() {
                // 0 / -1 = 0
                var b = fromParts(-1, 0);

                assertDecEquals(ZERO, div(ZERO, b));
            }

            @Test
            public void case_0001() {
                // -1.1 / -2.2 = 0.5
                var a = fromParts(-11, 1);
                var b = fromParts(-22, 1);
                var expected = fromParts(5, 1);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0002() {
                // 3.14159 / 0.0001 = 31415.9
                var a = fromParts(314159, 5);
                var b = fromParts(1, 4);
                var expected = fromParts(314159, 1);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0003() {
                // 3.14159 / 150 = 0.020943933333333333...
                var a = fromParts(314159, 5);
                var b = fromParts(150, 0);
                var expected = fromParts(2094393333333333L, 17);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0004() {
                // 3.14159 / 0.0000012 = 2617991.666666666...
                var a = fromParts(314159, 5);
                var b = fromParts(12, 7);
                var expected = fromParts(2617991666666667L, 9);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0005() {
                // -0.00000005 / 1000000000 = -0.00000000000000005
                var a = fromParts(-5, 8);
                var b = fromParts(1, -9);
                var expected = fromParts(-5, 17);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0006() {
                // -0.000000008104 / 39950000 = -0.00000000000000020285356695869837296620775969962...
                var a = fromParts(-8104, 12);
                var b = fromParts(39950000, 0);
                var expected = fromParts(-2028535669586984L, 31);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0007() {
                // -0.00000006 / 100000000 = -0.0000000000000006
                var a = fromParts(-6, 8);
                var b = fromParts(1, -8);
                var expected = fromParts(-6, 16);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0008() {
                // 0.00000002 / -200000000 = -0.0000000000000001
                var a = fromParts(2, 8);
                var b = fromParts(-2, -8);
                var expected = fromParts(-1, 16);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0009() {
                // 0.00000053 / -800000000 = -0.0000000000000006625
                var a = fromParts(53, 8);
                var b = fromParts(-8, -8);
                var expected = fromParts(-6625, 19);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0010() {
                // 0.0000032 / -7000000000 = -0.00000000000000045714285714285714285714285714...
                var a = fromParts(32, 7);
                var b = fromParts(-7, -9);
                var expected = fromParts(-4571428571428571L, 31);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0011() {
                // -3800000000 / 0.00000078 = -4871794871794871.79487179487179487179487...
                var a = fromParts(-38, -8);
                var b = fromParts(78, 8);
                var expected = fromParts(-4871794871794872L, 0);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0020() {
                // 1230.0000123 / 7.89 = 155.893537680608365019011406...
                var a = fromParts(12300000123L, 7);
                var b = fromParts(789, 2);
                var expected = fromParts(1558935376806084L, 13);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0021() {
                // 123 / 1e-150 = 123e150
                var a = fromParts(123, 0);
                var b = fromParts(1, 150);
                var expected = fromParts(123, -150);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0022() {
                // -1230 / 0.0000000000000123 = 100000000000000000
                var a = fromParts(-1230, 0);
                var b = fromParts(123, 16);
                var expected = fromParts(-1, -17);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0023() {
                // -0.0000000000000123 / 1230 = -0.00000000000000001
                var a = fromParts(-123, 16);
                var b = fromParts(1230, 0);
                var expected = fromParts(-1, 17);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0024() {
                // 1e+28 / 19e+12 = 526315789473684.2105263157894736...
                var a = fromParts(1, -28);
                var b = fromParts(19, -12);
                var expected = fromParts(5263157894736842L, 1);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0025() {
                // 5e-11 / 19e-27 = 2631578947368421.05263157894736842105...
                var a = fromParts(5, 11);
                var b = fromParts(19, 27);
                var expected = fromParts(2631578947368421L, 0);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0026() {
                // 1e+17 / 1e-18 = 1e35
                var a = fromParts(1, -17);
                var b = fromParts(1, 18);
                var expected = fromParts(1, -35);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0027() {
                // -878594030459625900000 / -4310444807979995000000000000 = 0.0000002038290871589564964657389773...
                var a = fromParts(-8785940304596259L, -5);
                var b = fromParts(-4310444807979995L, -12);
                var expected = fromParts(2038290871589565L, 22);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0028() {
                // -4475801326363716e-174 / -8653470778602982e-174 = -0.5172261443848419
                var a = fromParts(-4475801326363716L, 174);
                var b = fromParts(-8653470778602982L, 174);
                var expected = fromParts(5172261443848419L, 16);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0029() {
                // -6e-128 / -17e-128 = 102e-256 -> 1e-254
                var a = fromParts(-6, 128);
                var b = fromParts(-17, 128);
                var expected = fromParts(3529411764705882L, 16);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0030() {
                // 74743267600 / -9741.28827 = -7672831.92205541824089761794...
                var a = fromParts(74743267600L, 0);
                var b = fromParts(-974128827, 5);
                var expected = fromParts(-7672831922055418L, 9);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0031() {
                // 93735e-173 / -65215e-83 = -1437322701832401e-105
                var a = fromParts(93735, 173);
                var b = fromParts(-65215, 83);
                var expected = fromParts(-1437322701832401L, 105);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0032() {
                // 3583642e-85 / -1828025e-174 = -1960390038429453e-74
                var a = fromParts(3583642, 85);
                var b = fromParts(-1828025, 174);
                var expected = fromParts(-1960390038429453L, -74);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0033() {
                // 3436348972-e166 / -20632984e-106 = -1665463886367575e-73
                var a = fromParts(3436348972L, 166);
                var b = fromParts(-20632984, 106);
                var expected = fromParts(-1665463886367575L, 73);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void case_0034() {
                // 61322761611395e-149 / 158969636387037e-133 = 3857514114336585e-32
                var a = fromParts(61322761611395L, 149);
                var b = fromParts(158969636387037L, 133);
                var expected = fromParts(3857514114336585L, 32);

                assertDecEquals(expected, div(a, b));
            }

            @Test
            public void random() {
                fuzz(FUZZ_N, (a, b) -> {
                    if (b == ZERO) {
                        assertEquals(NAN, div(a, b), () -> Decimal64.toString(a) + " / " + Decimal64.toString(b));
                        return;
                    }

                    var expected = toBigDecimal(a).divide(toBigDecimal(b), MathContext.DECIMAL64).stripTrailingZeros();

                    if (expected.scale() < MIN_EXPONENT) {
                        // overflow to +/- inf
                        var expectedDec = FastMath.sameSign(a, b) ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                        assertEquals(expectedDec, div(a, b), () -> Decimal64.toString(a) + " / " + Decimal64.toString(b));
                        return;
                    }

                    if (expected.scale() - expected.precision() >= MAX_EXPONENT) {
                        // underflow to zero
                        expected = BigDecimal.ZERO;
                    } else if (expected.scale() > MAX_EXPONENT) {
                        // e.g. 102e-256 -> 1e-254
                        expected = expected.setScale(MAX_EXPONENT, RoundingMode.HALF_EVEN).stripTrailingZeros();
                    }

                    // normal finite result
                    assertEquals(expected, toBigDecimal(div(a, b)), () -> Decimal64.toString(a) + " / " + Decimal64.toString(b));
                });
            }

            @Nested
            class Round {

                @Test
                public void nan() {
                    var ex = assertThrows(IllegalArgumentException.class, () -> round(NAN, 0));
                    assertEquals("Can't convert non-finite decimal NaN to BigDecimal", ex.getMessage());
                }

                @Test
                public void negative_infinity() {
                    var ex = assertThrows(IllegalArgumentException.class, () -> round(NEGATIVE_INFINITY, 0));
                    assertEquals("Can't convert non-finite decimal -Infinity to BigDecimal", ex.getMessage());
                }

                @Test
                public void positive_infinity() {
                    var ex = assertThrows(IllegalArgumentException.class, () -> round(POSITIVE_INFINITY, 0));
                    assertEquals("Can't convert non-finite decimal +Infinity to BigDecimal", ex.getMessage());
                }

                @Test
                public void zero() {
                    assertEquals(ZERO, round(ZERO, 0));
                }

                @Test
                public void one() {
                    assertEquals(ONE, round(ONE, 0));
                }

                @Test
                public void case_0001() {
                    assertEquals(ZERO, round(ONE, -1));
                }

                @Test
                public void case_0002() {
                    var x = fromParts(314159, 5);
                    var expected = fromParts(314, 2);
                    assertEquals(expected, round(x, 2));
                }

                @Test
                @SuppressWarnings("fenum:binary")
                public void random() {
                    fuzz(FUZZ_N, decimal -> {
                        int exponent = (int) (decimal % 300);
                        var bigDecimal = toBigDecimal(decimal).setScale(exponent, RoundingMode.HALF_EVEN);
                        var expected = fromBigDecimal(bigDecimal);

                        assertEquals(expected, round(decimal, exponent));
                    });
                }
            }
        }
    }
}
