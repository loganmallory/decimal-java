package io.github.loganmallory.java.decimal;

import java.nio.ByteBuffer;

public class FastEncoding {

    private static final byte[] DigitOnes_i32 = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    static final byte[] DigitTens_i32 = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    } ;

    public static void write_i64(long val, ByteBuffer buf) {
        // ported from Long.getChars(..)

        int idx = buf.position() + FastMath.nDigits(val);

        long q;
        int r;

        boolean negative = val < 0;
        if (!negative) {
            val = -val;
        } else {
            idx++; // for '-'
        }

        // reserve space in buffer, will throw if no room
        buf.position(idx);

        // get 2 digits per iteration using longs until quotient fits into an int
        while (val <= Integer.MIN_VALUE) {
            q = val / 100;
            r = (int) ((q * 100) - val);
            val = q;
            buf.put(--idx, DigitOnes_i32[r]);
            buf.put(--idx, DigitTens_i32[r]);
        }

        // get 2 digits per iteration using ints
        int q2;
        int val2 = (int) val;
        while (val2 <= -100) {
            q2 = val2 / 100;
            r  = (q2 * 100) - val2;
            val2 = q2;
            buf.put(--idx, DigitOnes_i32[r]);
            buf.put(--idx, DigitTens_i32[r]);
        }

        // we know there are at most two digits left
        buf.put(--idx, DigitOnes_i32[-val2]);
        if (val2 < -9) {
            buf.put(--idx, DigitTens_i32[-val2]);
        }

        if (negative) {
            buf.put(--idx, (byte) '-');
        }
    }
}
