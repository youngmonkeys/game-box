package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.entity.octree.OcTree;
import com.tvd12.gamebox.entity.octree.OcTreeNode;
import com.tvd12.gamebox.entity.octree.OcVolume;
import com.tvd12.gamebox.math.Vec3;

import java.util.List;

public class MMOOcTreeRoom extends MMORoom {

    private final OcTree ocTree;

    public MMOOcTreeRoom(Builder builder) {
        super(builder);
        this.ocTree = new OcTree(
            builder.maxPointsPerNode,
            new OcVolume(builder.topLeftFront, builder.bottomRightBack)
        );
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        this.ocTree.insert((MMOPlayer) player);
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        if (!this.ocTree.containsPlayer(player)) {
            super.addPlayer(player);
            this.ocTree.insert(player, position);
            return;
        }
        OcTreeNode currentNode = this.ocTree.getNodeByPlayerName(player.getName());
        OcTreeNode newNode = this.ocTree.findNodeByPosition(position);
        if (currentNode == newNode) {
            player.setPosition(position);
            return;
        }
        this.ocTree.remove(player);
        this.ocTree.insert(player, position);
    }

    @Override
    protected void updatePlayers() {
        for (MMOPlayer player : playerBuffer) {
            player.clearNearByPlayers();
        }
        for (MMOPlayer player : playerBuffer) {
            List<MMOPlayer> nearByPlayers = this.ocTree.search(
                player,
                (float) this.distanceOfInterest
            );
            nearByPlayers.forEach(player::addNearbyPlayer);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MMORoom.Builder {

        private Vec3 topLeftFront;
        private Vec3 bottomRightBack;
        private int maxPointsPerNode;

        public Builder topLeftFront(Vec3 topLeftFront) {
            this.topLeftFront = topLeftFront;
            return this;
        }

        public Builder bottomRightBack(Vec3 bottomRightBack) {
            this.bottomRightBack = bottomRightBack;
            return this;
        }

        public Builder maxPointsPerNode(int maxPointsPerNode) {
            this.maxPointsPerNode = maxPointsPerNode;
            return this;
        }

        @Override
        protected void preBuild() {
            super.preBuild();
            if (maxPointsPerNode <= 0) {
                throw new IllegalArgumentException("maxPointsPerNode must be set!");
            }
        }

        @Override
        public MMORoom.Builder distanceOfInterest(double distance) {
            this.distanceOfInterest = distance;
            return this;
        }

        @Override
        protected MMORoom newProduct() {
            return new MMOOcTreeRoom(this);
        }
    }
}
