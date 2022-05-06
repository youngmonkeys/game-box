package com.tvd12.gamebox.math;

@SuppressWarnings("MemberName")
public class Rect {

    public float x;
    public float y;
    public float width;
    public float height;

    public Rect() {}

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

    public void setRect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getMaxX() {
        return x + width;
    }

    public float getMidX() {
        return x + width / 2.0f;
    }

    public float getMinX() {
        return x;
    }

    public float getMaxY() {
        return y + height;
    }

    public float getMidY() {
        return y + height / 2.0f;
    }

    public float getMinY() {
        return y;
    }

    public boolean containsPoint(Vec2 point) {
        return (point.x >= getMinX()
            && point.x <= getMaxX()
            && point.y >= getMinY()
            && point.y <= getMaxY());
    }

    public boolean intersectsRect(Rect rect) {
        return !(getMaxX() < rect.getMinX()
            || rect.getMaxX() < getMinX()
            || getMaxY() < rect.getMinY()
            || rect.getMaxY() < getMinY());
    }

    public boolean intersectsCircle(Vec2 center, float radius) {
        float centerX = x + width / 2;
        float centerY = y + height / 2;

        float w = width / 2;
        float h = height / 2;

        float dx = Math.abs(center.x - centerX);
        float dy = Math.abs(center.y - centerY);

        if (dx > (radius + w) || dy > (radius + h)) {
            return false;
        }

        float circleDistanceX = Math.abs(center.x - x - w);
        float circleDistanceY = Math.abs(center.y - y - h);
        if (circleDistanceX <= w) {
            return true;
        }

        if (circleDistanceY <= h) {
            return true;
        }

        float cornerDistanceSq = (float) (Math.pow(circleDistanceX - w, 2)
            + Math.pow(circleDistanceY - h, 2));

        return cornerDistanceSq <= (float) Math.pow(radius, 2);
    }

    public void merge(Rect rect) {
        float minX = Math.min(getMinX(), rect.getMinX());
        float minY = Math.min(getMinY(), rect.getMinY());
        float maxX = Math.max(getMaxX(), rect.getMaxX());
        float maxY = Math.max(getMaxY(), rect.getMaxY());
        setRect(minX, minY, maxX - minX, maxY - minY);
    }

    public Rect unionWithRect(Rect rect) {
        float thisLeftX = x;
        float thisRightX = x + width;
        float thisTopY = y + height;
        float thisBottomY = y;

        // This rect has negative width
        if (thisRightX < thisLeftX) {
            float tmp = thisRightX;
            thisRightX = thisLeftX;
            thisLeftX = tmp;
        }

        // This rect has negative height
        if (thisTopY < thisBottomY) {
            float tmp = thisTopY;
            thisTopY = thisBottomY;
            thisBottomY = tmp;
        }

        float otherLeftX = rect.x;
        float otherRightX = rect.x + rect.width;
        float otherTopY = rect.y + rect.height;
        float otherBottomY = rect.y;

        // Other rect has negative width
        if (otherRightX < otherLeftX) {
            float tmp = otherRightX;
            otherRightX = otherLeftX;
            otherLeftX = tmp;
        }

        // Other rect has negative height
        if (otherTopY < otherBottomY) {
            float tmp = otherTopY;
            otherTopY = otherBottomY;
            otherBottomY = tmp;
        }

        float combinedLeftX = Math.min(thisLeftX, otherLeftX);
        float combinedRightX = Math.max(thisRightX, otherRightX);
        float combinedTopY = Math.max(thisTopY, otherTopY);
        float combinedBottomY = Math.min(thisBottomY, otherBottomY);

        return new Rect(
            combinedLeftX,
            combinedBottomY,
            combinedRightX - combinedLeftX,
            combinedTopY - combinedBottomY
        );
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + " | " + width + ", " + height + ")";
    }
}
