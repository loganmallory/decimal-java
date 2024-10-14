package io.github.loganmallory;


import io.github.loganmallory.decimaljava.Decimal64;
import io.github.loganmallory.decimaljava.annotations.Decimal;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static io.github.loganmallory.decimaljava.Decimal64.Internal.PRECISION;


public class Decimal64Bench {

    @State(Scope.Thread)
    public static class JmhState {

        private final int K = PRECISION + 1;
        private final int N = 1_000_000;

        private final @Decimal long[][] decimalSamples = new long[K][N];

        private final BigDecimal[][] bigDecimalSamples = new BigDecimal[K][N];

        private int idx = 0;

        @Setup(Level.Trial)
        public void setup() {
            var rng = new Random(111);
            int bound = 1;
            for (int p = 0; p < K; p++) {

                long[] decimals = decimalSamples[p];
                BigDecimal[] bigDecimals = bigDecimalSamples[p];

                for (int i = 0; i < N; i++) {
                    // create random decimal
                    long mantissa = rng.nextInt() % bound;
                    int exponent = rng.nextInt(-255, 256);
                    decimals[i] = Decimal64.fromParts(mantissa, exponent);

                    // create random big decimal
                    bigDecimals[i] = BigDecimal.valueOf(mantissa, exponent).stripTrailingZeros();
                }

                bound *= 10;
            }
        }

        @Setup(Level.Invocation)
        public void reset() {
            idx = 0;
        }
    }

    public static class Maths {

//        @Fork(value = 1, warmups = 0)
//        @Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
//        @Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.MILLISECONDS)
//        @OutputTimeUnit(TimeUnit.NANOSECONDS)
//        @BenchmarkMode(Mode.AverageTime)
        public static class Add {

            @Benchmark
            public long decimal64_add_0_0(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[0][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_0(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[0][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_1(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[1][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_1(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[1][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_2(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[2][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_2(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[2][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_3(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_3(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_4(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_4(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_5(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_5(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_6(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_7(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_8(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_9(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_10(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_11(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_12(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_13(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_14(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_15(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_0_16(JmhState s) {
                @Decimal long a = s.decimalSamples[0][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_0_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[0][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_1(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[1][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_1(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[1][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_2(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[2][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_2(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[2][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_3(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_3(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_4(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_4(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_5(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_5(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_6(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_7(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_8(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_9(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_10(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_11(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_12(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_13(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_14(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_15(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_1_16(JmhState s) {
                @Decimal long a = s.decimalSamples[1][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_1_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[1][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_2(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[2][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_2(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[2][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_3(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_3(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_4(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_4(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_5(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_5(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_6(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_7(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_8(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_9(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_10(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_11(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_12(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_13(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_14(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_15(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_2_16(JmhState s) {
                @Decimal long a = s.decimalSamples[2][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_2_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[2][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_3(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_3(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[3][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_4(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_4(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_5(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_5(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_6(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_7(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_8(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_9(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_10(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_11(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_12(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_13(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_14(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_15(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_3_16(JmhState s) {
                @Decimal long a = s.decimalSamples[3][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_3_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[3][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_4(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_4(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[4][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_5(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_5(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_6(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_7(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_8(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_9(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_10(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_11(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_12(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_13(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_14(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_15(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_4_16(JmhState s) {
                @Decimal long a = s.decimalSamples[4][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_4_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[4][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_5(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_5(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[5][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_6(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_7(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_8(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_9(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_10(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_11(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_12(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_13(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_14(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_15(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_5_16(JmhState s) {
                @Decimal long a = s.decimalSamples[5][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_5_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[5][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_6(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_6(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[6][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_7(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_8(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_9(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_10(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_11(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_12(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_13(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_14(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_15(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_6_16(JmhState s) {
                @Decimal long a = s.decimalSamples[6][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_6_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[6][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_7(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_7(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[7][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_8(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_9(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_10(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_11(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_12(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_13(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_14(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_15(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_7_16(JmhState s) {
                @Decimal long a = s.decimalSamples[7][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_7_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[7][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_8(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_8(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[8][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_9(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_10(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_11(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_12(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_13(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_14(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_15(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_8_16(JmhState s) {
                @Decimal long a = s.decimalSamples[8][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_8_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[8][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_9(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_9(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[9][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_10(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_11(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_12(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_13(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_14(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_15(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_9_16(JmhState s) {
                @Decimal long a = s.decimalSamples[9][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_9_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[9][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_10(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_10(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[10][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_11(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_12(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_13(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_14(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_15(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_10_16(JmhState s) {
                @Decimal long a = s.decimalSamples[10][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_10_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[10][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_11_11(JmhState s) {
                @Decimal long a = s.decimalSamples[11][s.idx++];
                @Decimal long b = s.decimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_11_11(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[11][s.idx++];
                BigDecimal b = s.bigDecimalSamples[11][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_11_12(JmhState s) {
                @Decimal long a = s.decimalSamples[11][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_11_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[11][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_11_13(JmhState s) {
                @Decimal long a = s.decimalSamples[11][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_11_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[11][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_11_14(JmhState s) {
                @Decimal long a = s.decimalSamples[11][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_11_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[11][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_11_15(JmhState s) {
                @Decimal long a = s.decimalSamples[11][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_11_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[11][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_11_16(JmhState s) {
                @Decimal long a = s.decimalSamples[11][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_11_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[11][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_12_12(JmhState s) {
                @Decimal long a = s.decimalSamples[12][s.idx++];
                @Decimal long b = s.decimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_12_12(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[12][s.idx++];
                BigDecimal b = s.bigDecimalSamples[12][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_12_13(JmhState s) {
                @Decimal long a = s.decimalSamples[12][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_12_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[12][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_12_14(JmhState s) {
                @Decimal long a = s.decimalSamples[12][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_12_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[12][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_12_15(JmhState s) {
                @Decimal long a = s.decimalSamples[12][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_12_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[12][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_12_16(JmhState s) {
                @Decimal long a = s.decimalSamples[12][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_12_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[12][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_13_13(JmhState s) {
                @Decimal long a = s.decimalSamples[13][s.idx++];
                @Decimal long b = s.decimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_13_13(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[13][s.idx++];
                BigDecimal b = s.bigDecimalSamples[13][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_13_14(JmhState s) {
                @Decimal long a = s.decimalSamples[13][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_13_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[13][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_13_15(JmhState s) {
                @Decimal long a = s.decimalSamples[13][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_13_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[13][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_13_16(JmhState s) {
                @Decimal long a = s.decimalSamples[13][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_13_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[13][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_14_14(JmhState s) {
                @Decimal long a = s.decimalSamples[14][s.idx++];
                @Decimal long b = s.decimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_14_14(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[14][s.idx++];
                BigDecimal b = s.bigDecimalSamples[14][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_14_15(JmhState s) {
                @Decimal long a = s.decimalSamples[14][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_14_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[14][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_14_16(JmhState s) {
                @Decimal long a = s.decimalSamples[14][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_14_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[14][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_15_15(JmhState s) {
                @Decimal long a = s.decimalSamples[15][s.idx++];
                @Decimal long b = s.decimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_15_15(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[15][s.idx++];
                BigDecimal b = s.bigDecimalSamples[15][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_15_16(JmhState s) {
                @Decimal long a = s.decimalSamples[15][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_15_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[15][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }

            @Benchmark
            public long decimal64_add_16_16(JmhState s) {
                @Decimal long a = s.decimalSamples[16][s.idx++];
                @Decimal long b = s.decimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return Decimal64.add(a, b);
            }

            @Benchmark
            public BigDecimal bigDecimal_add_16_16(JmhState s) {
                BigDecimal a = s.bigDecimalSamples[16][s.idx++];
                BigDecimal b = s.bigDecimalSamples[16][s.idx++];

                if (s.idx == (s.N - 2)) s.idx = 0;

                return a.add(b, MathContext.DECIMAL64);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        var classes = Decimal64Bench.class.getName() + ".*";
        boolean profile = true;
        var mode = Mode.All;
        var outFmt = ResultFormatType.JSON;

        var outFolder = Paths.get(String.format("decimal-benchmarks/results/%s-%s", classes.replaceAll("\\*", ""), mode));
        Files.createDirectories(outFolder);

        var jmhOpts = new OptionsBuilder()
                .include(classes)
                .forks(1)
                .mode(mode)
                // warmup
                .warmupForks(0)
                .warmupIterations(1)
                .warmupTime(TimeValue.milliseconds(100))
                // measurement
                .measurementIterations(5)
                .measurementTime(TimeValue.milliseconds(10))
                // output
                .result(outFolder + "/summary." + outFmt.name().toLowerCase())
                .resultFormat(outFmt);

        if (profile) {
            jmhOpts.addProfiler(AsyncProfiler.class, "libPath=/Applications/IntelliJ IDEA.app/Contents/lib/async-profiler/libasyncProfiler.dylib;output=jfr;event=cpu;alloc;dir=decimal-benchmarks/results");
        }

        switch (mode) {
            case All, AverageTime, SingleShotTime, SampleTime -> {
                jmhOpts.timeUnit(TimeUnit.NANOSECONDS);
            }
            case Throughput -> {
                jmhOpts.timeUnit(TimeUnit.SECONDS);
            }
        }

        new Runner(jmhOpts.build()).run();
    }
}