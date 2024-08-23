package io.github.loganmallory.java.decimal;

public class FastMath {

    public static final long[] LOG10_THRESHOLDS = new long[]{
            9,
            99,
            999,
            9999,
            99999,
            999999,
            9999999,
            99999999,
            999999999,
            9999999999L,
            99999999999L,
            999999999999L,
            9999999999999L,
            99999999999999L,
            999999999999999L,
            9999999999999999L,
            99999999999999999L,
            999999999999999999L,
            Long.MAX_VALUE,
    };

    public static int log10I64(long val) {
        if (val <= 0) {
            if (val == 0) {
                return 0;
            }
            if (val == Long.MIN_VALUE) {
                return 18;
            }
            val *= -1;
        }

        //int guess = (Long.numberOfLeadingZeros(val) * 19) >>> 6;
        int guess = (int) ((Math.log(val) / Math.log(2)) * 19) >>> 6;

        long ttg = LOG10_THRESHOLDS[guess];

        return guess + (val > ttg ? 1 : 0);
    }

    public static int nDigits(long val) {
        return log10I64(val) + 1;
    }

    public static final long[] I64_TEN_TO_THE = new long[]{
            1,
            10,
            100,
            1000,
            10000,
            100000,             // 10^5
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,       // 10^10
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,  // 10^15
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L
    };

    public static long i64TenToThe(int exp) {
        return I64_TEN_TO_THE[exp];
    }

    public static final double[] F64_TEN_TO_THE = {
            1.0e0,  1.0e1,  1.0e2,  1.0e3,  1.0e4,  1.0e5, 1.0e6,  1.0e7,  1.0e8,  1.0e9,
            1.0e10, 1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19,
            1.0e20, 1.0e21, 1.0e22
    };

    public static double f64TenToThe(int exp) {
        return F64_TEN_TO_THE[exp];
    }

    public static boolean sameSign(long a, long b) {
        return (a ^ b) >= 0;
    }
}
