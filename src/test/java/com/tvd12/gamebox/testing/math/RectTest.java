package com.tvd12.gamebox.testing.math;

import com.tvd12.gamebox.math.Rect;
import com.tvd12.gamebox.math.Vec2;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class RectTest {

    @Test
    public void test() {
        // given
        Rect sut = new Rect(1.0F, 2.0F, 3.0F, 4.0F);

        // when
        // then
        Asserts.assertEquals(sut.x, 1.0F);
        Asserts.assertEquals(sut.y, 2.0F);
        Asserts.assertEquals(sut.width, 3.0F);
        Asserts.assertEquals(sut.height, 4.0F);

        Asserts.assertEquals(sut.getMaxX(), 1.0F + 3.0F);
        Asserts.assertEquals(sut.getMaxY(), 2.0F + 4.0F);
        Asserts.assertEquals(sut.getMinX(), 1.0F);
        Asserts.assertEquals(sut.getMinY(), 2.0F);
        Asserts.assertEquals(sut.getMidX(), 2.5F);
        Asserts.assertEquals(sut.getMidY(), 4.0F);

        Asserts.assertTrue(sut.containsPoint(new Vec2(1.5F, 2.5F)));
        Asserts.assertFalse(sut.containsPoint(new Vec2(0.0F, 0.0F)));
        Asserts.assertFalse(sut.containsPoint(new Vec2(1.0F, 0.0F)));
        Asserts.assertFalse(sut.containsPoint(new Vec2(5.0F, 0.0F)));
        Asserts.assertFalse(sut.containsPoint(new Vec2(2.0F, 8.0F)));

        Asserts.assertTrue(sut.intersectsRect(new Rect(1.5F, 2.5F, 1.0F, 1.0F)));
        Asserts.assertTrue(sut.intersectsRect(new Rect(1.5F, 2.5F, 3.0F, 4.0F)));
        Asserts.assertTrue(sut.intersectsRect(new Rect(0.0F, 0.0F, 3.0F, 4.0F)));

        // getMaxX() < rect.getMinX()
        Asserts.assertFalse(sut.intersectsRect(new Rect(5.0F, 1.0F, 3.0F, 4.0F)));

        // rect.getMaxX() < getMinX()
        Asserts.assertFalse(sut.intersectsRect(new Rect(-10.0F, 1.0F, 3.0F, 4.0F)));

        // getMaxY() < rect.getMinY()
        Asserts.assertFalse(sut.intersectsRect(new Rect(1.5F, 10F, 1.0F, 1.0F)));

        // rect.getMaxY() < getMinY())
        Asserts.assertFalse(sut.intersectsRect(new Rect(1.5F, -10.0F, 1.0F, 1.0F)));

        Asserts.assertTrue(sut.intersectsCircle(new Vec2(1.5F, 2.5F), 1));
        Asserts.assertTrue(sut.intersectsCircle(new Vec2(15.0F, 2.5F), 100));
        Asserts.assertTrue(sut.intersectsCircle(new Vec2(0.0F, 0.0F), 3));
        Asserts.assertTrue(sut.intersectsCircle(new Vec2(0.0F, 0.0F), 10));

        // dx > (radius + w)
        Asserts.assertFalse(sut.intersectsCircle(new Vec2(-10.0F, 2.50F), 2));

        // dy > (radius + h
        Asserts.assertFalse(sut.intersectsCircle(new Vec2(1.50F, -10.0F), 2));

        Asserts.assertFalse(sut.intersectsCircle(new Vec2(0.0F, 0.0F), 2.2299984F));

        sut.setRect(1.0F, 2.0F, 3.0F, 4.0F);
        sut.merge(new Rect());
        Asserts.assertEquals(sut, new Rect(0.0F, 0.0F, 4.0F, 6.0F));
        sut.merge(new Rect(new Rect(2.0F, 3.0F, 8.0F, 9.0F)));
        Asserts.assertEquals(sut, new Rect(0.0F, 0.0F, 10.0F, 12.0F));

        sut.setRect(1.0F, 2.0F, 3.0F, 4.0F);
        Asserts.assertEquals(
            sut.unionWithRect(new Rect(1.0F, 2.0F, 3.0F, 4.0F)),
            new Rect(1.0F, 2.0F, 3.0F, 4.0F)
        );

        Asserts.assertEquals(
            sut.unionWithRect(new Rect(1.0F, 2.0F, -3.0F, -4.0F)),
            new Rect(-2.0F, -2.0F, 6.0F, 8.0F)
        );

        sut.setRect(1.0F, 2.0F, -3.0F, -4.0F);
        Asserts.assertEquals(
            sut.unionWithRect(new Rect(1.0F, 2.0F, 3.0F, 4.0F)),
            new Rect(-2.0F, -2.0F, 6.0F, 8.0F)
        );

        System.out.println(sut);
    }
}
