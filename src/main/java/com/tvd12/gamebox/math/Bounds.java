package com.tvd12.gamebox.math;

import com.tvd12.ezyfox.util.EzyEnums;

import java.util.Map;

public class Bounds {
    private final Vec3 leftBottomBack;
    private final Vec3 rightTopFront;
    private final float maxDimension;
    
    public Bounds(Vec3 leftBottomBack, Vec3 rightTopFront) {
        if (leftBottomBack.x > rightTopFront.x) {
            throw new IllegalArgumentException(
                "invalid bounds, required: leftBottomBack.x < rightTopFront.x"
            );
        }
        if (leftBottomBack.y > rightTopFront.y) {
            throw new IllegalArgumentException(
                "invalid bounds, required: leftBottomBack.y < rightTopFront.y"
            );
        }
        if (leftBottomBack.z > rightTopFront.z) {
            throw new IllegalArgumentException(
                "invalid bounds, required: leftBottomBack.z < rightTopFront.z"
            );
        }
        this.leftBottomBack = leftBottomBack;
        this.rightTopFront = rightTopFront;
        this.maxDimension = Math.max(
            Math.abs(leftBottomBack.x - rightTopFront.x),
            Math.max(
                Math.abs(leftBottomBack.y - rightTopFront.y),
                Math.abs(leftBottomBack.z - rightTopFront.z)
            )
        );
    }
    
    public static Bounds fromCenterAndRange(Vec3 center, float range) {
        Vec3 leftBottomBack = new Vec3(center);
        leftBottomBack.subtract(new Vec3(range, range, range));
        Vec3 rightTopFront = new Vec3(center);
        rightTopFront.add(new Vec3(range, range, range));
        return new Bounds(leftBottomBack, rightTopFront);
    }
    
    public boolean containsPosition(Vec3 position) {
        return !(position.x < this.leftBottomBack.x
            || position.x > this.rightTopFront.x
            || position.y < this.leftBottomBack.y
            || position.y > this.rightTopFront.y
            || position.z < this.leftBottomBack.z
            || position.z > this.rightTopFront.z);
    }
    
    public boolean doesOverlap(Bounds other) {
        return !(other.rightTopFront.x < this.leftBottomBack.x
            || other.leftBottomBack.x > this.rightTopFront.x
            || other.rightTopFront.y < this.leftBottomBack.y
            || other.leftBottomBack.y > this.rightTopFront.y
            || other.rightTopFront.z < this.leftBottomBack.z
            || other.leftBottomBack.z > this.rightTopFront.z);
    }
    
    public Bounds getOctant(int index) {
        OcLocation ocLocation = Bounds.OcLocation.of(index);
        float midX = (leftBottomBack.x + rightTopFront.x) / 2;
        float midY = (leftBottomBack.y + rightTopFront.y) / 2;
        float midZ = (leftBottomBack.z + rightTopFront.z) / 2;
        switch (ocLocation) {
            case LEFT_BOTTOM_BACK:
                return new Bounds(
                    leftBottomBack,
                    new Vec3(midX, midY, midZ)
                );
            case LEFT_BOTTOM_FRONT:
                return new Bounds(
                    new Vec3(leftBottomBack.x, leftBottomBack.y, midZ),
                    new Vec3(midX, midY, rightTopFront.z)
                );
            case LEFT_TOP_BACK:
                return new Bounds(
                    new Vec3(leftBottomBack.x, midY, leftBottomBack.z),
                    new Vec3(midX, rightTopFront.y, midZ)
                );
            case LEFT_TOP_FRONT:
                return new Bounds(
                    new Vec3(leftBottomBack.x, midY, midZ),
                    new Vec3(midX, rightTopFront.y, rightTopFront.z)
                );
            case RIGHT_BOTTOM_BACK:
                return new Bounds(
                    new Vec3(midX, leftBottomBack.y, leftBottomBack.z),
                    new Vec3(rightTopFront.x, midY, midZ)
                );
            case RIGHT_BOTTOM_FRONT:
                return new Bounds(
                    new Vec3(midX, leftBottomBack.y, midZ),
                    new Vec3(rightTopFront.x, midY, rightTopFront.z)
                );
            case RIGHT_TOP_BACK:
                return new Bounds(
                    new Vec3(midX, midY, leftBottomBack.z),
                    new Vec3(rightTopFront.x, rightTopFront.y, midZ)
                );
            default: // RIGHT_TOP_FRONT
                return new Bounds(
                    new Vec3(midX, midY, midZ),
                    rightTopFront
                );
        }
    }
    
    public float getMaxDimension() {
        return maxDimension;
    }
    
    @Override
    public String toString() {
        return leftBottomBack + "->" + rightTopFront;
    }
    
    private enum OcLocation {
        LEFT_BOTTOM_BACK(0),
        LEFT_BOTTOM_FRONT(1),
        LEFT_TOP_BACK(2),
        LEFT_TOP_FRONT(3),
        RIGHT_BOTTOM_BACK(4),
        RIGHT_BOTTOM_FRONT(5),
        RIGHT_TOP_BACK(6),
        RIGHT_TOP_FRONT(7);
        
        private final int location;
        
        private static final Map<Integer, OcLocation> OC_LOCATION_BY_INDEX =
            EzyEnums.enumMap(OcLocation.class, it -> it.location);
        
        OcLocation(int location) {
            this.location = location;
        }
        
        public static OcLocation of(int index) {
            if (index < 0 || index > 7) {
                throw new IllegalArgumentException(
                    "invalid ocLocation index, required: 0 <= index <= 7"
                );
            }
            return OC_LOCATION_BY_INDEX.get(index);
        }
    }
}
