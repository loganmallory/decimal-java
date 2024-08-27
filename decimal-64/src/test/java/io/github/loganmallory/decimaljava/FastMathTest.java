package io.github.loganmallory.decimaljava;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FastMathTest {

    public static final long RNG_SEED = 111;

    @Test
    public void log10_i64__zero() {
        assertEquals(0, FastMath.log10I64(0));
    }

    @Test
    public void log10_i64__min() {
        assertEquals(18, FastMath.log10I64(Long.MIN_VALUE));
    }

    @Test
    public void log10_i64__max() {
        assertEquals(18, FastMath.log10I64(Long.MAX_VALUE));
    }

    @Test
    public void log10_i64__random() {
        var rng = new Random(RNG_SEED);

        // test 10k values in each [-9.2ei, 9.2ei] window
        for (int i = 0; i < 19; i++) {
            long bound = Long.MAX_VALUE / (int) Math.pow(10, i);
            rng.longs(10_000, -bound, bound).forEach(val -> {
                if (val == 0) {
                    assertEquals(0, FastMath.log10I64(val));
                } else {
                    int expected = (int) Math.floor(Math.log10(Math.abs(val)));
                    assertEquals(expected, FastMath.log10I64(val), "val="+val);
                }
            });
        }
    }

    @Test
    public void i64_ten_to_the_valid() {
        for (short i = 0; i < 19; i++) {
            assertEquals(Math.round(Math.pow(10, i)), FastMath.i64TenToThe(i));
        }
    }
}
