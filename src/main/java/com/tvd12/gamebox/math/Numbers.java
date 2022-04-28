/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tvd12.gamebox.math;

public final class Numbers {

    /**
     * Offset to order signed double numbers lexicographically.
     */
    private static final int SGN_MASK_FLOAT = 0x80000000;
    /**
     * Positive zero bits.
     */
    private static final int POSITIVE_ZERO_FLOAT_BITS = Float.floatToRawIntBits(+0.0f);
    /**
     * Negative zero bits.
     */
    private static final int NEGATIVE_ZERO_FLOAT_BITS = Float.floatToRawIntBits(-0.0f);

    private Numbers() {
    }

    /**
     * Returns true if they are equal as defined by
     * {@link #equals(float, float, int) equals(x, y, 1)}.
     *
     * @param x first value
     * @param y second value
     * @return {@code true} if the values are equal.
     */
    public static boolean equals(float x, float y) {
        return x == y || equals(x, y, 1);
    }

    /**
     * Returns true if the arguments are equal or within the range of allowed
     * error (inclusive). Returns {@code false} if either of the arguments is NaN.
     * <p>
     * Two double numbers are considered equal if there are {@code (maxUlps - 1)}
     * (or fewer) floating point numbers between them, i.e. two adjacent
     * floating point numbers are considered equal.
     * </p>
     * <p>
     * Adapted from <a
     * href="http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">
     * Bruce Dawson</a>.
     * </p>
     *
     * @param x       first value
     * @param y       second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     *                values between {@code x} and {@code y}.
     * @return {@code true} if there are fewer than {@code maxUlps} floating
     * point values between {@code x} and {@code y}.
     */
    public static boolean equals(final float x, final float y, final int maxUlps) {
        if (Float.isNaN(x) || Float.isNaN(y)) {
            return false;
        }
        final int xInt = Float.floatToRawIntBits(x);
        final int yInt = Float.floatToRawIntBits(y);

        final boolean isEqual;
        if (((xInt ^ yInt) & SGN_MASK_FLOAT) == 0) {
            // number have same sign, there is no risk of overflow
            isEqual = Math.abs(xInt - yInt) <= maxUlps;
        } else {
            // number have opposite signs, take care of overflow
            final int deltaPlus;
            final int deltaMinus;
            if (xInt < yInt) {
                deltaPlus = yInt - POSITIVE_ZERO_FLOAT_BITS;
                deltaMinus = xInt - NEGATIVE_ZERO_FLOAT_BITS;
            } else {
                deltaPlus = xInt - POSITIVE_ZERO_FLOAT_BITS;
                deltaMinus = yInt - NEGATIVE_ZERO_FLOAT_BITS;
            }

            if (deltaPlus > maxUlps) {
                isEqual = false;
            } else {
                isEqual = deltaMinus <= (maxUlps - deltaPlus);
            }

        }
        return isEqual;
    }
}
