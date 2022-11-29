package com.tvd12.gamebox.entity.octree;

import com.tvd12.gamebox.math.Vec3;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class OcVolume {
    private Vec3 topLeftFront;
    private Vec3 bottomRightBack;

    public boolean containsPoint(Vec3 position) {
        return position.x >= this.topLeftFront.x && position.x < this.bottomRightBack.x
            && position.y >= this.topLeftFront.y && position.y < this.bottomRightBack.y
            && position.z >= this.topLeftFront.z && position.z < this.bottomRightBack.z;
    }
    
    public boolean doesOverlap(OcVolume otherVolume) {
        if (otherVolume.getBottomRightBack().x < this.topLeftFront.x) {
            return false;
        }
        if (otherVolume.getTopLeftFront().x > this.bottomRightBack.x) {
            return false;
        }
        if (otherVolume.getBottomRightBack().y < this.topLeftFront.y) {
            return false;
        }
        if (otherVolume.getTopLeftFront().y > this.bottomRightBack.y) {
            return false;
        }
        if (otherVolume.getBottomRightBack().z < this.topLeftFront.z) {
            return false;
        }
        if (otherVolume.getTopLeftFront().z > this.bottomRightBack.z) {
            return false;
        }
        return true;
    }

    public OcVolume getOctant(OcLocation ocLocation) {
        float midX = (topLeftFront.x + bottomRightBack.x) / 2;
        float midY = (topLeftFront.y + bottomRightBack.y) / 2;
        float midZ = (topLeftFront.z + bottomRightBack.z) / 2;
        switch (ocLocation) {
            case TOP_LEFT_FRONT:
                return new OcVolume(
                    topLeftFront,
                    new Vec3(midX, midY, midZ)
                );
            case TOP_RIGHT_FRONT:
                return new OcVolume(
                    new Vec3(midX, topLeftFront.y, topLeftFront.z),
                    new Vec3(bottomRightBack.x, midY, midZ)
                );
            case BOTTOM_RIGHT_FRONT:
                return new OcVolume(
                    new Vec3(midX, midY, topLeftFront.z),
                    new Vec3(bottomRightBack.x, bottomRightBack.y, midZ)
                );
            case BOTTOM_LEFT_FRONT:
                return new OcVolume(
                    new Vec3(topLeftFront.x, midY, topLeftFront.z),
                    new Vec3(midX, bottomRightBack.y, midZ)
                );
            case TOP_LEFT_BACK:
                return new OcVolume(
                    new Vec3(topLeftFront.x, topLeftFront.y, midZ),
                    new Vec3(midX, midY, bottomRightBack.z)
                );
            case TOP_RIGHT_BACK:
                return new OcVolume(
                    new Vec3(midX, topLeftFront.y, midZ),
                    new Vec3(bottomRightBack.x, midY, bottomRightBack.z)
                );
            case BOTTOM_RIGHT_BACK:
                return new OcVolume(
                    new Vec3(midX, midY, midZ),
                    bottomRightBack
                );
            case BOTTOM_LEFT_BACK:
                return new OcVolume(
                    new Vec3(topLeftFront.x, midY, midZ),
                    new Vec3(midX, bottomRightBack.y, bottomRightBack.z)
                );
            default:
                return null;
        }
    }
}
