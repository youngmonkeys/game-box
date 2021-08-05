package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class MMOPlayer extends Player {
	
	protected Vec3 position = Vec3.ZERO;
	protected Vec3 rotation = Vec3.ZERO;
	protected AtomicBoolean stateChanged = new AtomicBoolean(false);
	
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
	
	public void setPosition(Vec3 position) {
		this.position.set(position);
		this.stateChanged.set(true);
	}
	
	public void setRotation(Vec3 rotation) {
		this.rotation.set(rotation);
		this.stateChanged.set(true);
	}
	
	public void setStateChanged(boolean state) {
		this.stateChanged.set(state);
	}
	
	public boolean getStateChanged() {
		return this.stateChanged.get();
	}
	
	/**
	 * To be used after onRoomUpdated to sync neighbor's positions for current player
	 *
	 * @param buffer initialized only once to maintain performance
	 */
	public void getNearbyPlayerNames(List<String> buffer) {
		buffer.addAll(nearbyPlayers.keySet());
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
