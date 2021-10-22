package com.tvd12.gamebox.math;

@SuppressWarnings("MemberName")
public class Rect {

    public float x;
    public float y;
    public float width;
    public float height;

    public Rect() {
    }

    public Rect(Rect r) {
        this.x = r.x;
        this.y = r.y;
        this.width = r.width;
        this.height = r.height;
    }

    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getTopY() {
        float value = y + height;
        return value;
    }

    public float getRightX() {
        float value = x + width;
        return value;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("(")
                .append(x).append(", ")
                .append(y).append(" | ")
                .append(width).append(", ")
                .append(height)
                .append(")")
                .toString();
    }
}
