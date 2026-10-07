package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.grid.UniformGrid;
import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;

import java.util.List;

public class MMOUniformGridRoom extends MMORoom {

    @Getter
    private final float cellSize;
    private final UniformGrid<MMOPlayer> grid;

    public MMOUniformGridRoom(Builder builder) {
        super(builder);
        this.cellSize = builder.cellSize;
        this.grid = new UniformGrid<>(builder.cellSize);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void addPlayer(Player player) {
        synchronized (this) {
            super.addPlayer(player);
            if (this.grid.insert((MMOPlayer) player)) {
                updateNearbyPlayers((MMOPlayer) player);
            }
        }
    }

    @Override
    public void removePlayer(Player player) {
        synchronized (this) {
            if (player instanceof MMOPlayer) {
                this.grid.remove((MMOPlayer) player);
                clearCurrentNearbyPlayers((MMOPlayer) player);
            }
            super.removePlayer(player);
        }
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        synchronized (this) {
            player.setPosition(position);
            if (!this.grid.contains(player)) {
                addPlayer(player);
                return;
            }
            this.grid.update(player);
            updateNearbyPlayers(player);
        }
    }

    public List<MMOPlayer> searchPlayers(Vec3 center, float range) {
        synchronized (this) {
            return this.grid.search(center, range);
        }
    }

    private void updateNearbyPlayers(MMOPlayer player) {
        clearCurrentNearbyPlayers(player);
        List<MMOPlayer> nearbyPlayers = this.grid.search(
            player,
            (float) this.distanceOfInterest
        );
        for (MMOPlayer nearbyPlayer : nearbyPlayers) {
            player.addNearbyPlayer(nearbyPlayer);
            nearbyPlayer.addNearbyPlayer(player);
        }
    }

    private void clearCurrentNearbyPlayers(MMOPlayer player) {
        for (String otherPlayerName : player.getNearbyPlayerNames()) {
            MMOPlayer otherPlayer = (MMOPlayer) playerManager.getPlayer(otherPlayerName);
            if (otherPlayer != null) {
                otherPlayer.removeNearByPlayer(player);
            }
        }
        player.clearNearByPlayers();
    }

    @Override
    protected void updatePlayers() {}

    public static class Builder extends MMORoom.Builder {

        private float cellSize;

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
