package com.tvd12.gamebox.testing.math;

import org.testng.annotations.Test;

import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.gamebox.math.Vec2;
import com.tvd12.test.assertion.Asserts;

public class Vec2Test {

    @Test
    public void test() {
        // given
        Vec2 a = new Vec2(new float[] {1.0F, 2.0F});
        Vec2 b = new Vec2(a);
        Vec2 c = new Vec2(1.0D, 2.0D);
        
        // when
        // then
        Asserts.assertEquals(b.getX(), 1.0F);
        Asserts.assertEquals(b.getY(), 2.0F);
        Asserts.assertEquals(a, b);
        Asserts.assertEquals(a, c);
        Asserts.assertEquals(a.hashCode(), b.hashCode());
        
        a.add(new Vec2(1.0F, 2.0F));
        Asserts.assertEquals(a.getX(), 2.0F);
        Asserts.assertEquals(a.getY(), 4.0F);
        Asserts.assertEquals(a.length(), Math.sqrt(a.x * a.x + a.y * a.y));
        a.negate();
        Asserts.assertEquals(a.getX(), -2.0F);
        Asserts.assertEquals(a.getY(), -4.0F);
        
        a.set(1.0F, 2.0F);
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        
        a.set(2.0F, 4.0F);
        Asserts.assertEquals(a.getX(), 2.0F);
        Asserts.assertEquals(a.getY(), 4.0F);
        
        a.set(new Vec2(1.0F, 2.0F));
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        
        a.set(1.0D, 2.0D);
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        
        a.set(new double[] {2.0F, 4.0F});
        a.subtract(new Vec2(1.0F, 2.0F));
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        
        a.multiply(2.0D);
        Asserts.assertEquals(a.getX(), 2.0F);
        Asserts.assertEquals(a.getY(), 4.0F);
        
        a.multiply(2.0F);
        Asserts.assertEquals(a.getX(), 4.0F);
        Asserts.assertEquals(a.getY(), 8.0F);
        
        Vec2 an = a.multipleNew(2.0F);
        Asserts.assertEquals(an.getX(), 8.0F);
        Asserts.assertEquals(an.getY(), 16.0F);
        
        Asserts.assertEquals(a.distance(b), Math.sqrt(a.distanceSquare(b)));
        
        Asserts.assertNotEquals(a, b);
        a.set(3.0, 6.0);
        Asserts.assertNotEquals(a, new Vec2(1.0F, 2.0F));
        Asserts.assertNotEquals(a, new Vec2(3.0F, 3.0F));
        
        Asserts.assertEquals(a.toFloatArray(), new float[] { 3.0F, 6.0F });
        Asserts.assertEquals(a.toArray(), EzyEntityFactory.newArrayBuilder().append(3.0F, 6.0F).build());
        System.out.println(a);
    }
}
