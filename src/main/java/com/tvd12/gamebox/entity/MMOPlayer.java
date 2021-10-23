package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.util.ReadOnlySet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@SuppressWarnings("AbbreviationAsWordInName")
public class MMOPlayer extends Player {

    protected Vec3 position = new Vec3();
    protected Vec3 rotation = new Vec3();
    protected AtomicBoolean stateChanged = new AtomicBoolean(false);
    @Setter
    protected int clientTimeTick;

    @Getter(AccessLevel.NONE)
    protected final Map<String, MMOPlayer> nearbyPlayers = new ConcurrentHashMap<>();

    public MMOPlayer(String name) {
        super(name);
    }

    void addNearbyPlayer(MMOPlayer otherPlayer) {
        this.nearbyPlayers.put(otherPlayer.getName(), otherPlayer);
    }

    void removeNearByPlayer(MMOPlayer otherPlayer) {
        this.nearbyPlayers.remove(otherPlayer.getName());
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

    public void setStateChanged(boolean changed) {
        this.stateChanged.set(changed);
    }

    public boolean isStateChanged() {
        return this.stateChanged.get();
    }

    /**
     * To be used after onRoomUpdated to sync neighbor's positions for current player.
     *
     * @param buffer initialized only once to maintain performance
     */
    public void getNearbyPlayerNames(List<String> buffer) {
        buffer.addAll(nearbyPlayers.keySet());
    }

    public ReadOnlySet<String> getNearbyPlayerNames() {
        return new ReadOnlySet<>(nearbyPlayers.keySet());
    }

    protected MMOPlayer(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Player.Builder<Builder> {

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
