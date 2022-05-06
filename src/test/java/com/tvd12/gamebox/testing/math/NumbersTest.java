package com.tvd12.gamebox.testing.math;

import com.tvd12.gamebox.math.Numbers;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class NumbersTest {

    @Test
    public void test() {
        float d1 = 0;
        for (int i = 1; i <= 8; i++) {
            d1 += 0.1F;
        }

        float d2 = 0.1F * 8;
        Asserts.assertTrue(Numbers.equals(d1, d2));
        Asserts.assertTrue(Numbers.equals(1.25F, 1.25F));
        Asserts.assertTrue(Numbers.equals(0.0F, 0.0F));
        Asserts.assertTrue(Numbers.equals(0.0F, -0.0F));
        Asserts.assertTrue(Numbers.equals(Float.MAX_VALUE, Float.MAX_VALUE - 1));
        Asserts.assertFalse(Numbers.equals(Float.MAX_VALUE, Float.MAX_VALUE / 1000));

        Asserts.assertFalse(Numbers.equals(0x80000000 + 0F, 0));
        Asserts.assertFalse(Numbers.equals(0F, 0x80000000 + 0F));

        Asserts.assertFalse(Numbers.equals(0x80000000 + 0F, 0x10000000 + 1F));

        Asserts.assertTrue(Numbers.equals(-2147483648.0F, Float.MIN_VALUE, 1325400065));

        //noinspection divzero
        Asserts.assertFalse(Numbers.equals(0.0F / 0.0F, 1.0F, 1));
        //noinspection divzero
        Asserts.assertFalse(Numbers.equals(1.0F, 0.0F / 0.0F, 1));
    }
}
