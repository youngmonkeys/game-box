package com.tvd12.gamebox.testing.math;

import com.tvd12.gamebox.math.Bounds;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

public class BoundsTest {
    
    @Test
    public void getOctantWithInvalidIndexTest() {
        // given
        int index1 = RandomUtil.randomInt(-100, 0);
        int index2 = RandomUtil.randomInt(8, 100);
        Vec3 leftBottomBack = new Vec3(0, 0, 0);
        Vec3 rightTopFront = new Vec3(2, 2, 2);
        Bounds bounds = new Bounds(leftBottomBack, rightTopFront);
        
        // when
        Throwable e1 = Asserts.assertThrows(() -> bounds.getOctant(index1));
        Throwable e2 = Asserts.assertThrows(() -> bounds.getOctant(index2));
        
        // then
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e1.getClass().toString());
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e2.getClass().toString());
    }
    
    @Test
    public void createInvalidBoundsTest() {
        // given
        Vec3 leftBottomBack1 = new Vec3(1, 0, 0);
        Vec3 rightTopFront1 = new Vec3(0, 1, 1);
        
        Vec3 leftBottomBack2 = new Vec3(0, 1, 0);
        Vec3 rightTopFront2 = new Vec3(1, 0, 1);
    
        Vec3 leftBottomBack3 = new Vec3(0, 0, 1);
        Vec3 rightTopFront3 = new Vec3(1, 1, 0);
        
        // when
        Throwable e1 = Asserts.assertThrows(() -> {
            new Bounds(leftBottomBack1, rightTopFront1);
        });
        Throwable e2 = Asserts.assertThrows(() -> {
            new Bounds(leftBottomBack2, rightTopFront2);
        });
        Throwable e3 = Asserts.assertThrows(() -> {
            new Bounds(leftBottomBack3, rightTopFront3);
        });
    
        // then
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e1.getClass().toString());
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e2.getClass().toString());
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e3.getClass().toString());
    }
}
