package com.tvd12.gamebox.octree;

import com.tvd12.gamebox.math.Vec3;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OcBound {
    private final Vec3 topLeftFront;
    private final Vec3 bottomRightBack;
    
    public static OcBound fromCenterAndRange(Vec3 center, float range) {
        Vec3 topLeftFront = new Vec3(center);
        topLeftFront.subtract(new Vec3(range, range, range));
        Vec3 bottomRightBack = new Vec3(center);
        bottomRightBack.add(new Vec3(range, range, range));
        return new OcBound(topLeftFront, bottomRightBack);
    }

    public boolean containsPosition(Vec3 position) {
        return position.x >= this.topLeftFront.x && position.x <= this.bottomRightBack.x
            && position.y >= this.topLeftFront.y && position.y <= this.bottomRightBack.y
            && position.z >= this.topLeftFront.z && position.z <= this.bottomRightBack.z;
    }
    
    public boolean doesOverlap(OcBound otherBound) {
        if (otherBound.bottomRightBack.x < this.topLeftFront.x) {
            return false;
        }
        if (otherBound.topLeftFront.x > this.bottomRightBack.x) {
            return false;
        }
        if (otherBound.bottomRightBack.y < this.topLeftFront.y) {
            return false;
        }
        if (otherBound.topLeftFront.y > this.bottomRightBack.y) {
            return false;
        }
        if (otherBound.bottomRightBack.z < this.topLeftFront.z) {
            return false;
        }
        if (otherBound.topLeftFront.z > this.bottomRightBack.z) {
            return false;
        }
        return true;
    }

    public OcBound getOctant(OcLocation ocLocation) {
        float midX = (topLeftFront.x + bottomRightBack.x) / 2;
        float midY = (topLeftFront.y + bottomRightBack.y) / 2;
        float midZ = (topLeftFront.z + bottomRightBack.z) / 2;
        switch (ocLocation) {
            case TOP_LEFT_FRONT:
                return new OcBound(
                    topLeftFront,
                    new Vec3(midX, midY, midZ)
                );
            case TOP_RIGHT_FRONT:
                return new OcBound(
                    new Vec3(midX, topLeftFront.y, topLeftFront.z),
                    new Vec3(bottomRightBack.x, midY, midZ)
                );
            case BOTTOM_RIGHT_FRONT:
                return new OcBound(
                    new Vec3(midX, midY, topLeftFront.z),
                    new Vec3(bottomRightBack.x, bottomRightBack.y, midZ)
                );
            case BOTTOM_LEFT_FRONT:
                return new OcBound(
                    new Vec3(topLeftFront.x, midY, topLeftFront.z),
                    new Vec3(midX, bottomRightBack.y, midZ)
                );
            case TOP_LEFT_BACK:
                return new OcBound(
                    new Vec3(topLeftFront.x, topLeftFront.y, midZ),
                    new Vec3(midX, midY, bottomRightBack.z)
                );
            case TOP_RIGHT_BACK:
                return new OcBound(
                    new Vec3(midX, topLeftFront.y, midZ),
                    new Vec3(bottomRightBack.x, midY, bottomRightBack.z)
                );
            case BOTTOM_RIGHT_BACK:
                return new OcBound(
                    new Vec3(midX, midY, midZ),
                    bottomRightBack
                );
            default: // BOTTOM_LEFT_BACK:
                return new OcBound(
                    new Vec3(topLeftFront.x, midY, midZ),
                    new Vec3(midX, bottomRightBack.y, bottomRightBack.z)
                );
        }
    }
    
    public float getMaxDimension() {
        return Math.max(
            Math.abs(topLeftFront.x - bottomRightBack.x),
            Math.max(
                Math.abs(topLeftFront.y - bottomRightBack.y),
                Math.abs(topLeftFront.z - bottomRightBack.z)
            )
        );
    }
    
    @Override
    public String toString() {
        return topLeftFront + "->" + bottomRightBack;
    }
}
