package com.tvd12.gamebox.entity;

import java.util.ArrayList;
import java.util.List;

import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.manager.SynchronizedPlayerManager;

import lombok.Getter;

@SuppressWarnings("AbbreviationAsWordInName")
public class MMORoom extends NormalRoom {

    @Getter
    protected MMOPlayer master;
    @Getter
    protected final int maxPlayer;
    
    @Getter
    protected final double distanceOfInterest;
    
    protected final List<MMOPlayer> playerBuffer;
    
    protected final List<MMORoomUpdatedHandler> roomUpdatedHandlers;
    
    public MMORoom(Builder builder) {
        super(builder);
        this.playerBuffer = new ArrayList<>();
        this.maxPlayer = builder.maxPlayer;
        this.roomUpdatedHandlers = builder.roomUpdatedHandlers;
        this.distanceOfInterest = builder.distanceOfInterest;
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
            if (master == player && !playerManager.isEmpty()) {
                master = (MMOPlayer) playerManager.getFirstPlayer();
            }
        }
    }
    
    public boolean isEmpty() {
        return this.getPlayerManager().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public void update() {
        List<MMOPlayer> playerList = playerManager.getPlayerList();
        playerList.forEach(player -> {
            player.clearNearByPlayers();
            playerList.forEach(other -> {
                double distance = player.getPosition().distance(other.getPosition());
                if (distance <= this.distanceOfInterest) {
                    player.addNearbyPlayer(other);
                } else {
                    player.removeNearByPlayer(other);
                }
            });
        });

        notifyUpdatedHandlers();
    }

    @SuppressWarnings("unchecked")
    private void notifyUpdatedHandlers() {
        playerBuffer.clear();
        playerManager.getPlayerList(playerBuffer);
        for (MMORoomUpdatedHandler handler : this.roomUpdatedHandlers) {
            handler.onRoomUpdated(this, playerBuffer);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends NormalRoom.Builder<Builder> {
        protected List<MMORoomUpdatedHandler> roomUpdatedHandlers = new ArrayList<>();
        protected double distanceOfInterest;
        protected int maxPlayer = 999;

        public Builder() {
        }

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
