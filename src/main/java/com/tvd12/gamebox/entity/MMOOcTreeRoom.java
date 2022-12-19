package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTree;
import com.tvd12.gamebox.octree.SynchronizedOcTree;
import lombok.Getter;

import java.util.List;

public class MMOOcTreeRoom extends MMORoom {
    
    @Getter
    private final Vec3 leftBottomBack;
    
    @Getter
    private final Vec3 rightTopFront;
    
    private final OcTree<MMOPlayer> ocTree;

    public MMOOcTreeRoom(Builder builder) {
        super(builder);
        this.ocTree = new SynchronizedOcTree<>(
            builder.leftBottomBack,
            builder.rightTopFront,
            builder.maxPointsPerNode,
            builder.minNodeSize
        );
        this.leftBottomBack = builder.leftBottomBack;
        this.rightTopFront = builder.rightTopFront;
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        if (!this.ocTree.contains(player)) {
            addNewPlayer(player, position);
        } else {
            updateExistingPlayer(player, position);
        }
        updateNearbyPlayers(player);
    }
    
    private void updateNearbyPlayers(MMOPlayer player) {
        clearCurrentNearbyPlayers(player);
        addNewNearbyPlayers(player);
    }
    
    private void clearCurrentNearbyPlayers(MMOPlayer player) {
        for (String otherPlayerName : player.getNearbyPlayerNames()) {
            MMOPlayer otherPlayer = (MMOPlayer) playerManager.getPlayer(otherPlayerName);
            otherPlayer.removeNearByPlayer(player);
        }
        player.clearNearByPlayers();
    }
    
    private void addNewNearbyPlayers(MMOPlayer player) {
        List<MMOPlayer> nearByPlayers = this.ocTree.search(
            player,
            (float) this.distanceOfInterest
        );
        nearByPlayers.forEach(nearbyPlayer -> {
            player.addNearbyPlayer(nearbyPlayer);
            nearbyPlayer.addNearbyPlayer(player);
        });
    }
    
    private void addNewPlayer(MMOPlayer player, Vec3 position) {
        super.addPlayer(player);
        boolean isPlayerInserted = this.ocTree.insert(player, position);
        if (isPlayerInserted) {
            player.setPosition(position);
        }
    }

    private void updateExistingPlayer(MMOPlayer player, Vec3 position) {
        if (this.ocTree.isItemRemainingAtSameNode(player, position)) {
            player.setPosition(position);
        } else {
            this.ocTree.remove(player);
            boolean isPlayerInserted = this.ocTree.insert(player, position);
            if (isPlayerInserted) {
                player.setPosition(position);
            }
        }
    }
    
    @Override
    public void update() {
        notifyUpdatedHandlers();
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MMORoom.Builder {

        private Vec3 leftBottomBack;
        private Vec3 rightTopFront;
        private int maxPointsPerNode;
        private float minNodeSize = 0.000001f;

        public Builder leftBottomBack(Vec3 leftBottomBack) {
            this.leftBottomBack = leftBottomBack;
            return this;
        }

        public Builder rightTopFront(Vec3 rightTopFront) {
            this.rightTopFront = rightTopFront;
            return this;
        }

        public Builder maxPointsPerNode(int maxPointsPerNode) {
            this.maxPointsPerNode = maxPointsPerNode;
            return this;
        }
        
        public Builder minNodeSize(float minNodeSize) {
            this.minNodeSize = minNodeSize;
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
