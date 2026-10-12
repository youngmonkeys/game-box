package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.grid.UniformGrid;
import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MMOUniformGridRoom extends MMORoom {

    @Getter
    private final float cellSize;
    @Getter
    private final boolean updateNearbyOnTick;
    private final UniformGrid<MMOPlayer> grid;
    private final Set<MMOPlayer> nearbyPlayerSet = new HashSet<>();
    private final List<MMOPlayer> nearbyPlayerBuffer = new ArrayList<>();
    private final List<MMOPlayer> oldNearbyPlayerBuffer = new ArrayList<>();

    public MMOUniformGridRoom(Builder builder) {
        super(builder);
        this.cellSize = builder.cellSize;
        this.updateNearbyOnTick = builder.updateNearbyOnTick;
        this.grid = new UniformGrid<>(builder.cellSize);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void addPlayer(Player player) {
        synchronized (this) {
            super.addPlayer(player);
            MMOPlayer mmoPlayer = (MMOPlayer) player;
            if (this.grid.insert(mmoPlayer) && !updateNearbyOnTick) {
                linkNearbyPlayers(mmoPlayer);
            }
        }
    }

    @Override
    public void removePlayer(Player player) {
        synchronized (this) {
            if (player instanceof MMOPlayer) {
                MMOPlayer mmoPlayer = (MMOPlayer) player;
                if (this.grid.contains(mmoPlayer)) {
                    unlinkNearbyPlayers(mmoPlayer);
                    this.grid.remove(mmoPlayer);
                }
                mmoPlayer.clearNearByPlayers();
            }
            super.removePlayer(player);
        }
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        synchronized (this) {
            if (!this.grid.contains(player)) {
                player.setPosition(position);
                addPlayer(player);
                return;
            }
            if (updateNearbyOnTick) {
                player.setPosition(position);
                this.grid.update(player);
                return;
            }
            oldNearbyPlayerBuffer.clear();
            this.grid.search(
                player.getPosition(),
                (float) this.distanceOfInterest,
                oldNearbyPlayerBuffer
            );
            player.setPosition(position);
            this.grid.update(player);
            for (MMOPlayer oldNearbyPlayer : oldNearbyPlayerBuffer) {
                unlinkIfOutOfRange(player, oldNearbyPlayer);
            }
            for (MMOPlayer currentNearbyPlayer : player.getNearbyPlayers()) {
                unlinkIfOutOfRange(player, currentNearbyPlayer);
            }
            linkNearbyPlayers(player);
        }
    }

    public List<MMOPlayer> searchPlayers(Vec3 center, float range) {
        synchronized (this) {
            return this.grid.search(center, range);
        }
    }

    public void searchPlayers(Vec3 center, float range, List<MMOPlayer> buffer) {
        synchronized (this) {
            this.grid.search(center, range, buffer);
        }
    }

    private void linkNearbyPlayers(MMOPlayer player) {
        nearbyPlayerBuffer.clear();
        this.grid.search(
            player.getPosition(),
            (float) this.distanceOfInterest,
            nearbyPlayerBuffer
        );
        for (MMOPlayer nearbyPlayer : nearbyPlayerBuffer) {
            MMOPlayer.link(player, nearbyPlayer);
        }
    }

    private void unlinkNearbyPlayers(MMOPlayer player) {
        nearbyPlayerBuffer.clear();
        this.grid.search(
            player.getPosition(),
            (float) this.distanceOfInterest,
            nearbyPlayerBuffer
        );
        for (MMOPlayer nearbyPlayer : nearbyPlayerBuffer) {
            MMOPlayer.unlink(player, nearbyPlayer);
        }
        for (MMOPlayer nearbyPlayer : player.getNearbyPlayers()) {
            MMOPlayer.unlink(player, nearbyPlayer);
        }
    }

    private void unlinkIfOutOfRange(MMOPlayer player, MMOPlayer otherPlayer) {
        float range = (float) this.distanceOfInterest;
        if (player.getPosition().distanceSquare(otherPlayer.getPosition()) > range * range) {
            MMOPlayer.unlink(player, otherPlayer);
        }
    }

    @Override
    protected void updatePlayers() {
        if (!updateNearbyOnTick) {
            return;
        }
        synchronized (this) {
            for (MMOPlayer player : playerBuffer) {
                if (!player.isInterestEnabled() || !this.grid.contains(player)) {
                    continue;
                }
                nearbyPlayerBuffer.clear();
                this.grid.search(
                    player.getPosition(),
                    (float) this.distanceOfInterest,
                    nearbyPlayerBuffer
                );
                player.replaceNearbyPlayers(nearbyPlayerBuffer, nearbyPlayerSet);
            }
            nearbyPlayerBuffer.clear();
        }
    }

    public static class Builder extends MMORoom.Builder {

        private float cellSize;
        private boolean updateNearbyOnTick;

        public Builder updateNearbyOnTick(boolean updateNearbyOnTick) {
            this.updateNearbyOnTick = updateNearbyOnTick;
            return this;
        }

        public Builder cellSize(float cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        @Override
        public Builder distanceOfInterest(double distance) {
            this.distanceOfInterest = distance;
            return this;
        }

        @Override
        protected void preBuild() {
            super.preBuild();
            if (cellSize <= 0) {
                cellSize = (float) distanceOfInterest;
            }
        }

        @Override
        protected MMORoom newProduct() {
            return new MMOUniformGridRoom(this);
        }
    }
}
