package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Bounds;
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
    
    private final Bounds bounds;
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
        this.bounds = new Bounds(builder.leftBottomBack, builder.rightTopFront);
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        if (!this.bounds.containsPosition(position)) {
            throw new IllegalArgumentException(
                "Can't set " + player.getName() + " to position=" + position
            );
        }
        synchronized (this) {
            boolean isSuccessful;
            if (!this.ocTree.contains(player)) {
                isSuccessful = addNewPlayer(player, position);
            } else {
                isSuccessful = updateExistingPlayer(player, position);
            }
            if (!isSuccessful) {
                throw new IllegalArgumentException(
                    "Can't set " + player.getName() + " to position=" + position
                );
            }
            updateNearbyPlayers(player);
        }
    }
    
    private void updateNearbyPlayers(MMOPlayer player) {
        clearCurrentNearbyPlayers(player);
        addNewNearbyPlayers(player);
    }
    
    private void clearCurrentNearbyPlayers(MMOPlayer player) {
        for (String otherPlayerName : player.getNearbyPlayerNames()) {
            MMOPlayer otherPlayer = (MMOPlayer) playerManager.getPlayer(otherPlayerName);
            if (otherPlayer != null) {
                otherPlayer.removeNearByPlayer(player);
            }
            MMOPlayer otherPlayer = (MMOPlayer) playerManager
                .getPlayer(otherPlayerName);
            if (otherPlayer != null) {
                otherPlayer.removeNearByPlayer(player);
            }
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
    
    private boolean addNewPlayer(MMOPlayer player, Vec3 newPosition) {
        super.addPlayer(player);
        player.setPosition(newPosition);
        if (this.ocTree.insert(player)) {
            return true;
        }
        super.removePlayer(player);
        return false;
    }

    private boolean updateExistingPlayer(MMOPlayer player, Vec3 newPosition) {
        if (this.ocTree.isItemRemainingAtSameNode(player, newPosition)) {
            player.setPosition(newPosition);
            return true;
        } else {
            this.ocTree.remove(player);
            player.setPosition(newPosition);
            return this.ocTree.insert(player);
        }
    }
    
    @Override
    public void removePlayer(Player player) {
        synchronized (this) {
            if (player instanceof MMOPlayer) {
                this.ocTree.remove((MMOPlayer) player);
                clearCurrentNearbyPlayers((MMOPlayer) player);
            }
            super.removePlayer(player);
        }
    }

    @Override
    protected void updatePlayers() {
        // do nothing
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
                throw new IllegalArgumentException(
                    "maxPointsPerNode must be set!"
                );
            }
        }

        @Override
        protected MMORoom newProduct() {
            return new MMOOcTreeRoom(this);
        }
    }
}
