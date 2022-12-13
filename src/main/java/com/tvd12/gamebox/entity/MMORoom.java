package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.manager.SynchronizedPlayerManager;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MMORoom extends NormalRoom {

    @Getter
    @Setter
    protected MMOPlayer master;
    @Getter
    protected final double distanceOfInterest;
    protected final List<MMOPlayer> playerBuffer;
    protected final List<MMORoomUpdatedHandler> roomUpdatedHandlers;

    public MMORoom(Builder builder) {
        super(builder);
        this.playerBuffer = new ArrayList<>();
        this.roomUpdatedHandlers = builder.roomUpdatedHandlers;
        this.distanceOfInterest = builder.distanceOfInterest;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addPlayer(Player player) {
        if (!(player instanceof MMOPlayer)) {
            throw new IllegalArgumentException("Player " + player.getName() + " must be MMOPlayer");
        }

        if (playerManager.containsPlayer(player)) {
            return;
        }

        synchronized (this) {
            if (playerManager.isEmpty()) {
                master = (MMOPlayer) player;
            }
            super.addPlayer(player);
        }
    }

    @Override
    public void removePlayer(Player player) {
        if (!(player instanceof MMOPlayer)) {
            throw new IllegalArgumentException("Player " + player.getName() + " must be MMOPlayer");
        }

        synchronized (this) {
            super.removePlayer(player);
            if (master == player) {
                master = (playerManager.isEmpty())
                    ? null
                    : (MMOPlayer) playerManager.getFirstPlayer();
            }
        }
    }

    public boolean isEmpty() {
        return this.getPlayerManager().isEmpty();
    }

    public int getMaxPlayer() {
        return this.getPlayerManager().getMaxPlayer();
    }

    @SuppressWarnings("unchecked")
    public void update() {
        playerManager.getPlayerList(playerBuffer);
        try {
            updatePlayers();
            notifyUpdatedHandlers();
        } finally {
            playerBuffer.clear();
        }
    }

    protected void updatePlayers() {
        for (MMOPlayer player : playerBuffer) {
            player.clearNearByPlayers();
        }
        for (int i = 0; i < playerBuffer.size(); ++i) {
            MMOPlayer player = playerBuffer.get(i);
            for (int k = i; k < playerBuffer.size(); ++k) {
                MMOPlayer other = playerBuffer.get(k);
                double distance = player.getPosition().distance(other.getPosition());
                if (distance <= this.distanceOfInterest) {
                    player.addNearbyPlayer(other);
                    other.addNearbyPlayer(player);
                }
            }
        }
    }

    protected void notifyUpdatedHandlers() {
        for (MMORoomUpdatedHandler handler : this.roomUpdatedHandlers) {
            handler.onRoomUpdated(this, playerBuffer);
        }
    }

    public static class Builder extends NormalRoom.Builder<Builder> {

        protected int maxPlayer = 999;
        protected double distanceOfInterest;
        protected List<MMORoomUpdatedHandler> roomUpdatedHandlers = new ArrayList<>();

        public Builder addRoomUpdatedHandler(MMORoomUpdatedHandler handler) {
            this.roomUpdatedHandlers.add(handler);
            return this;
        }

        public Builder distanceOfInterest(double distance) {
            this.distanceOfInterest = distance;
            return this;
        }

        public Builder maxPlayer(int maxPlayer) {
            this.maxPlayer = maxPlayer;
            return this;
        }

        @Override
        public Builder defaultPlayerManager(int maxPlayer) {
            this.playerManager = new SynchronizedPlayerManager<>(maxPlayer);
            return this;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Builder playerManager(PlayerManager playerManager) {
            if (playerManager instanceof SynchronizedPlayerManager) {
                return super.playerManager(playerManager);
            }
            throw new IllegalArgumentException("playerManager must be SynchronizedPlayerManager");
        }

        @Override
        protected void preBuild() {
            if (playerManager == null) {
                playerManager = new SynchronizedPlayerManager<>(maxPlayer);
            }

            if (distanceOfInterest <= 0.0f) {
                throw new IllegalArgumentException("distanceOfInterest must be set!");
            }
            super.preBuild();
        }

        @Override
        public MMORoom build() {
            return (MMORoom) super.build();
        }

        @Override
        protected MMORoom newProduct() {
            return new MMORoom(this);
        }
    }
}
