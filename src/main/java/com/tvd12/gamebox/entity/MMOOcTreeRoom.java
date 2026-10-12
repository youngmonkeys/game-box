package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Bounds;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTree;
import com.tvd12.gamebox.octree.SynchronizedOcTree;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MMOOcTreeRoom extends MMORoom {
    
    @Getter
    private final Vec3 leftBottomBack;
    
    @Getter
    private final Vec3 rightTopFront;
    
    @Getter
    private final boolean updateNearbyOnTick;
    @Getter
    private final int nearbyUpdateIntervalTicks;
    private long nearbyUpdateCount;
    private final List<MMOPlayer> nearbyPlayerBuffer = new ArrayList<>();
    private final Bounds bounds;
    private final OcTree<MMOPlayer> ocTree;
    private final Set<MMOPlayer> nearbyPlayerSet = new HashSet<>();

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
        this.updateNearbyOnTick = builder.updateNearbyOnTick;
        this.nearbyUpdateIntervalTicks = Math.max(1, builder.nearbyUpdateIntervalTicks);
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        if (!this.bounds.containsPosition(position)) {
            throw new IllegalArgumentException(
                "Can't set " + player.getName() + " to position=" + position
            );
        }
        synchronized (this) {
            if (!this.ocTree.contains(player)) {
                if (!addNewPlayer(player, position)) {
                    throw new IllegalArgumentException(
                        "Can't set " + player.getName() + " to position=" + position
                    );
                }
                if (updateNearbyOnTick) {
                    return;
                }
                for (MMOPlayer nearbyPlayer : searchNearbyPlayers(player)) {
                    MMOPlayer.link(player, nearbyPlayer);
                }
                return;
            }
            if (updateNearbyOnTick) {
                if (!updateExistingPlayer(player, position)) {
                    throw new IllegalArgumentException(
                        "Can't set " + player.getName() + " to position=" + position
                    );
                }
                return;
            }
            List<MMOPlayer> oldNearbyPlayers = searchNearbyPlayers(player);
            if (!updateExistingPlayer(player, position)) {
                throw new IllegalArgumentException(
                    "Can't set " + player.getName() + " to position=" + position
                );
            }
            List<MMOPlayer> newNearbyPlayers = searchNearbyPlayers(player);
            nearbyPlayerSet.clear();
            nearbyPlayerSet.addAll(newNearbyPlayers);
            for (MMOPlayer oldNearbyPlayer : oldNearbyPlayers) {
                if (!nearbyPlayerSet.contains(oldNearbyPlayer)) {
                    MMOPlayer.unlink(player, oldNearbyPlayer);
                }
            }
            for (MMOPlayer currentNearbyPlayer : player.getNearbyPlayers()) {
                if (!nearbyPlayerSet.contains(currentNearbyPlayer)) {
                    MMOPlayer.unlink(player, currentNearbyPlayer);
                }
            }
            for (MMOPlayer newNearbyPlayer : newNearbyPlayers) {
                MMOPlayer.link(player, newNearbyPlayer);
            }
            nearbyPlayerSet.clear();
        }
    }

    public List<MMOPlayer> searchPlayers(Vec3 center, float range) {
        List<MMOPlayer> buffer = new ArrayList<>();
        searchPlayers(center, range, buffer);
        return buffer;
    }

    public void searchPlayers(Vec3 center, float range, List<MMOPlayer> buffer) {
        synchronized (this) {
            int startIndex = buffer.size();
            this.ocTree.search(center, range, buffer);
            float rangeSquare = range * range;
            int writeIndex = startIndex;
            for (int i = startIndex; i < buffer.size(); ++i) {
                MMOPlayer player = buffer.get(i);
                if (center.distanceSquare(player.getPosition()) <= rangeSquare) {
                    buffer.set(writeIndex++, player);
                }
            }
            for (int i = buffer.size() - 1; i >= writeIndex; --i) {
                buffer.remove(i);
            }
        }
    }

    private List<MMOPlayer> searchNearbyPlayers(MMOPlayer player) {
        return this.ocTree.search(player, (float) this.distanceOfInterest);
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
                MMOPlayer mmoPlayer = (MMOPlayer) player;
                if (this.ocTree.contains(mmoPlayer)) {
                    for (MMOPlayer nearbyPlayer : searchNearbyPlayers(mmoPlayer)) {
                        MMOPlayer.unlink(mmoPlayer, nearbyPlayer);
                    }
                    this.ocTree.remove(mmoPlayer);
                }
                for (MMOPlayer nearbyPlayer : mmoPlayer.getNearbyPlayers()) {
                    MMOPlayer.unlink(mmoPlayer, nearbyPlayer);
                }
                mmoPlayer.clearNearByPlayers();
            }
            super.removePlayer(player);
        }
    }

    @Override
    protected void updatePlayers() {
        if (!updateNearbyOnTick) {
            return;
        }
        synchronized (this) {
            long updateCount = ++nearbyUpdateCount;
            float range = (float) this.distanceOfInterest;
            for (MMOPlayer player : playerBuffer) {
                if (!player.isInterestEnabled()
                    || !isNearbyUpdateTurn(player, updateCount, nearbyUpdateIntervalTicks)
                    || !this.ocTree.contains(player)
                ) {
                    continue;
                }
                nearbyPlayerBuffer.clear();
                this.ocTree.search(player.getPosition(), range, nearbyPlayerBuffer);
                player.replaceNearbyPlayers(nearbyPlayerBuffer, nearbyPlayerSet);
            }
            nearbyPlayerBuffer.clear();
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MMORoom.Builder {

        private Vec3 leftBottomBack;
        private Vec3 rightTopFront;
        private int maxPointsPerNode;
        private float minNodeSize = 0.000001f;
        private boolean updateNearbyOnTick;
        private int nearbyUpdateIntervalTicks = 1;

        public Builder updateNearbyOnTick(boolean updateNearbyOnTick) {
            this.updateNearbyOnTick = updateNearbyOnTick;
            return this;
        }

        public Builder nearbyUpdateIntervalTicks(int nearbyUpdateIntervalTicks) {
            this.nearbyUpdateIntervalTicks = nearbyUpdateIntervalTicks;
            return this;
        }

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
