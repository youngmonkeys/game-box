package com.tvd12.gamebox.entity.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;

import java.util.ArrayList;
import java.util.List;

public class OcTreeNode {

    private final int maxPoints;
    private List<MMOPlayer> players;
    private OcTreeNode[] children = new OcTreeNode[8];
    private Vec3 topLeftFront, bottomRightBack;

    public OcTreeNode(
        int maxPoints,
        Vec3 topLeftFront,
        Vec3 bottomRightBack
    ) {
        this.maxPoints = maxPoints;
        this.players = new ArrayList<>();
        this.topLeftFront = topLeftFront;
        this.bottomRightBack = bottomRightBack;
    }
    
    public void insert(MMOPlayer player, Vec3 position) {
        if (position.x < topLeftFront.x || position.x > bottomRightBack.x
            || position.y < topLeftFront.y || position.y > bottomRightBack.y
            || position.z < topLeftFront.z || position.z > bottomRightBack.z
        ) {
            return;
        }
        
        float midX = (topLeftFront.x + bottomRightBack.x) / 2;
        float midY = (topLeftFront.y + bottomRightBack.y) / 2;
        float midZ = (topLeftFront.z + bottomRightBack.z) / 2;
        
        int location;
        
        
    }
}
