package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class MMOPlayer extends Player implements PositionAware {

    protected Vec3 position = new Vec3();
    protected Vec3 rotation = new Vec3();
    @Setter
    protected int clientTimeTick;
    protected final boolean interestEnabled;

    @Getter(AccessLevel.NONE)
    protected final AtomicBoolean stateChanged
        = new AtomicBoolean(false);

    @Getter(AccessLevel.NONE)
    protected final Map<String, MMOPlayer> nearbyPlayers
        = new ConcurrentHashMap<>();

    public MMOPlayer(String name) {
        this(name, true);
    }

    public MMOPlayer(String name, boolean interestEnabled) {
        super(name);
        this.interestEnabled = interestEnabled;
    }

    protected MMOPlayer(Builder builder) {
        super(builder);
        this.interestEnabled = builder.interestEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    static void link(MMOPlayer player, MMOPlayer otherPlayer) {
        if (player.interestEnabled) {
            player.addNearbyPlayer(otherPlayer);
        }
        if (otherPlayer.interestEnabled) {
            otherPlayer.addNearbyPlayer(player);
        }
    }

    static void unlink(MMOPlayer player, MMOPlayer otherPlayer) {
        if (player.interestEnabled) {
            player.removeNearByPlayer(otherPlayer);
        }
        if (otherPlayer.interestEnabled) {
            otherPlayer.removeNearByPlayer(player);
        }
    }

    void replaceNearbyPlayers(List<MMOPlayer> players, Set<MMOPlayer> buffer) {
        buffer.clear();
        buffer.addAll(players);
        for (MMOPlayer currentPlayer : nearbyPlayers.values()) {
            if (!buffer.contains(currentPlayer)) {
                nearbyPlayers.remove(currentPlayer.getName());
            }
        }
        for (MMOPlayer player : players) {
            nearbyPlayers.put(player.getName(), player);
        }
        buffer.clear();
    }

    void addNearbyPlayer(MMOPlayer otherPlayer) {
        this.nearbyPlayers.put(otherPlayer.getName(), otherPlayer);
    }

    void removeNearByPlayer(MMOPlayer otherPlayer) {
        this.nearbyPlayers.remove(otherPlayer.getName());
    }

    Collection<MMOPlayer> getNearbyPlayers() {
        return this.nearbyPlayers.values();
    }

    void clearNearByPlayers() {
        this.nearbyPlayers.clear();
    }

    public void setPosition(Vec3 position) {
        this.setPosition(position.x, position.y, position.z);
    }

    public void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        this.stateChanged.set(true);
    }

    public void setRotation(Vec3 rotation) {
        this.setRotation(rotation.x, rotation.y, rotation.z);
    }

    public void setRotation(double x, double y, double z) {
        this.rotation.set(x, y, z);
        this.stateChanged.set(true);
    }

    public boolean isStateChanged() {
        return this.stateChanged.get();
    }

    public void setStateChanged(boolean changed) {
        this.stateChanged.set(changed);
    }

    /**
     * To be used after onRoomUpdated to sync neighbor's positions for current player.
     *
     * @param buffer initialized only once to maintain performance
     */
    public void getNearbyPlayerNames(List<String> buffer) {
        buffer.addAll(nearbyPlayers.keySet());
    }

    public List<String> getNearbyPlayerNames() {
        return new ArrayList<>(nearbyPlayers.keySet());
    }

    public void getNearbyPlayers(List<MMOPlayer> buffer) {
        for (MMOPlayer nearbyPlayer : nearbyPlayers.values()) {
            buffer.add(nearbyPlayer);
        }
    }

    public int getNearbyPlayerCount() {
        return nearbyPlayers.size();
    }

    public static class Builder extends Player.Builder<Builder> {

        protected boolean interestEnabled = true;

        public Builder interestEnabled(boolean interestEnabled) {
            this.interestEnabled = interestEnabled;
            return this;
        }

        @Override
        protected Player newProduct() {
            return new MMOPlayer(this);
        }

        @Override
        public MMOPlayer build() {
            return (MMOPlayer) super.build();
        }
    }
}
