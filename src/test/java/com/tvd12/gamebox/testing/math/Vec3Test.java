package com.tvd12.gamebox.testing.math;

import org.testng.annotations.Test;

import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;

public class Vec3Test {

    @Test
    public void test() {
        // given
        Vec3 a = new Vec3(new float[] {1.0F, 2.0F, 3.0F});
        Vec3 b = new Vec3(a);
        
        // when
        // then
        Asserts.assertEquals(b.getX(), 1.0F);
        Asserts.assertEquals(b.getY(), 2.0F);
        Asserts.assertEquals(b.getZ(), 3.0F);
        Asserts.assertEquals(a, b);
        Asserts.assertEquals(a.hashCode(), b.hashCode());
        
        a.add(new Vec3(1.0F, 2.0F, 3.0F));
        Asserts.assertEquals(a.getX(), 2.0F);
        Asserts.assertEquals(a.getY(), 4.0F);
        Asserts.assertEquals(a.getZ(), 6.0F);
        Asserts.assertEquals(a.length(), Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z));
        a.negate();
        Asserts.assertEquals(a.getX(), -2.0F);
        Asserts.assertEquals(a.getY(), -4.0F);
        Asserts.assertEquals(a.getZ(), -6.0F);
        
        a.set(1.0F, 2.0F, 3.0F);
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        Asserts.assertEquals(a.getZ(), 3.0F);
        
        a.set(2.0F, 4.0F, 6.0F);
        Asserts.assertEquals(a.getX(), 2.0F);
        Asserts.assertEquals(a.getY(), 4.0F);
        Asserts.assertEquals(a.getZ(), 6.0F);
        
        a.set(new Vec3(1.0F, 2.0F, 3.0F));
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        Asserts.assertEquals(a.getZ(), 3.0F);
        
        a.set(new double[] {2.0F, 4.0F, 6.0F});
        a.subtract(new Vec3(1.0F, 2.0F, 3.0F));
        Asserts.assertEquals(a.getX(), 1.0F);
        Asserts.assertEquals(a.getY(), 2.0F);
        Asserts.assertEquals(a.getZ(), 3.0F);
        
        a.multiply(2.0D);
        Asserts.assertEquals(a.getX(), 2.0F);
        Asserts.assertEquals(a.getY(), 4.0F);
        Asserts.assertEquals(a.getZ(), 6.0F);
        
        Asserts.assertEquals(a.distance(b), Math.sqrt(a.distanceSquare(b)));
        
        Asserts.assertNotEquals(a, b);
        Asserts.assertNotEquals(a, new Vec3(2.0F, 3.0F, 4.0F));
        Asserts.assertNotEquals(a, new Vec3(2.0F, 4.0F, 5.0F));
        
        Asserts.assertEquals(a.toFloatArray(), new float[] { 2.0F, 4.0F, 6.0F });
        Asserts.assertEquals(a.toArray(), EzyEntityFactory.newArrayBuilder().append(2.0F, 4.0F, 6.0F).build());
        System.out.println(a);
    }
}
