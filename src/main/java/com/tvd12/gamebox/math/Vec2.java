package com.tvd12.gamebox.math;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.util.EzyEntityArrays;

import lombok.Getter;

@Getter
@SuppressWarnings("MemberName")
public class Vec2 {

    public float x;
    public float y;

    public static final Vec2 ZERO = new Vec2();
    public static final Vec2 LEFT = new Vec2(-1, 0);
    public static final Vec2 UP = new Vec2(0, 1);
    public static final Vec2 RIGHT = new Vec2(1, 0);
    public static final Vec2 DOWN = new Vec2(0, -1);

    public Vec2() {
        this(0.0F, 0.0F);
    }

    public Vec2(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    
    public Vec2(float[] xy) {
        this.x = xy[0];
        this.y = xy[1];
    }

    public Vec2(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(double[] xy) {
        x = (float)xy[0];
        y = (float)xy[1];
    }
    
    public void set(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void set(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    
    public void add(Vec2 v) {
        x += v.x;
        y += v.y;
    }
    
    public void negate() {
        x = -x;
        y = -y;
    }
    
    public void subtract(Vec2 v) {
        x -= v.x;
        y -= v.y;
    }

    public void multiply(double a) {
        this.x *= a;
        this.y *= a;
    }

    public Vec2 multipleNew(float a) {
        return new Vec2(this.x * a, this.y * a);
    }
    
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    public double distance(Vec2 v) {
        double dx = v.x - x;
        double dy = v.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public float distanceSquare(Vec2 v) {
        float dx = x - v.x;
        float dy = y - v.y;
        return dx * dx + dy * dy;
    }

    @Override
    public boolean equals(Object obj) {
        Vec2 other = (Vec2)obj;
        return Numbers.equals(x, other.x) 
            && Numbers.equals(y, other.y);
    }
    
    @Override
    public int hashCode() {
        int hashCode = 31 + Float.hashCode(x);
        hashCode += 31 * hashCode  + Float.hashCode(y);
        return hashCode;
    }
    
    public float[] toFloatArray() {
        return new float[] { x, y };
    }
    
    public EzyArray toArray() {
        return EzyEntityArrays.newArray(x, y);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("(")
                .append(x).append(", ")
                .append(y)
                .append(")")
                .toString();
    }
}
