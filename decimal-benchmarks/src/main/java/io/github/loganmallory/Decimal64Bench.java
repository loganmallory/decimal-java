package io.github.loganmallory;


import io.github.loganmallory.decimaljava.Decimal64;
import io.github.loganmallory.decimaljava.FastMath;
import io.github.loganmallory.decimaljava.annotations.Decimal;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static io.github.loganmallory.decimaljava.Decimal64.Internal.MAX_MANTISSA;
import static io.github.loganmallory.decimaljava.Decimal64.fromParts;

@State(Scope.Thread)
@Fork(value = 3, warmups = 1)
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 3, timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class Decimal64Bench {

    private static final int N = 100_000_000;

    private final @Decimal long[] decimalSamples = new long[N];

    private final BigDecimal[] bigDecimalSamples = new BigDecimal[N];

    private int index = 0;

    @Setup(Level.Trial)
    public void setup() {
        for (int i = 0; i < N; i++) {
            var x = Decimal64.fromParts(i, i % 255);
            decimalSamples[i] = x;
            bigDecimalSamples[i] = Decimal64.toBigDecimal(x);
        }
        shuffle(decimalSamples, new Random(111));
        Collections.shuffle(Arrays.asList(bigDecimalSamples));
    }

    public static void shuffle(long[] samples, Random rng) {
        for (int i = 0; i < samples.length; i++) {
            int randomIndexToSwap = rng.nextInt(samples.length);
            long temp = samples[randomIndexToSwap];
            samples[randomIndexToSwap] = samples[i];
            samples[i] = temp;
        }
    }

//    @Setup(Level.Trial)
//    public void setup() {
//        for (int i = 0; i < 1; i++) {
//            for (int m = 0; m <= 17; m++) {
//                for (int e = 1; e < (255+1) - m; e++) {
//                    // samples[m][e][i] = ...
//                }
//            }
//        }
//    }

    @Benchmark
    public long addDecimal64() {
        long a = decimalSamples[(index++) % N];
        long b = decimalSamples[(index++) % N];
        if (index < 0) index = 0;
        return Decimal64.add(a, b);
    }

//    @Benchmark
//    public BigDecimal addBigDecimal() {
//        BigDecimal a = bigDecimalSamples[(index++) % N];
//        BigDecimal b = bigDecimalSamples[(index++) % N];
//        if (index < 0) index = 0;
//        return a.add(b, MathContext.DECIMAL64);
//    }

    public static void main(String[] args) throws Exception {
        var jmhOpts = new OptionsBuilder()
                .include(Decimal64Bench.class.getName() + ".*")
                .addProfiler(AsyncProfiler.class, "libPath=/Applications/IntelliJ IDEA.app/Contents/lib/async-profiler/libasyncProfiler.dylib;output=jfr;event=cpu;alloc;dir=decimal-benchmarks/results")
                .build();

        System.out.println("decimal64 version = " + Decimal64.version);
        new Runner(jmhOpts).run();
    }

    // 0. fast-math but with Math.log
    // 1. fixed log10
    // 2.
}