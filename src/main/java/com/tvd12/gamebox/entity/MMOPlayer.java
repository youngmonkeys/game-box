package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MMOPlayer extends Player {
	
	@Setter
	protected Vec3 position;
	
	@Setter
	protected Vec3 rotation;
	
	@Getter(AccessLevel.NONE)
	protected final Map<String, MMOPlayer> nearbyPlayers = new ConcurrentHashMap<>();
	
	public MMOPlayer(String name) {
		super(name);
	}
	
	public void addNearbyPlayer(MMOPlayer otherPlayer) {
		this.nearbyPlayers.put(otherPlayer.getName(), otherPlayer);
	}
	
	public void removeNearByPlayer(MMOPlayer otherPlayer) {
		this.nearbyPlayers.remove(otherPlayer.getName());
	}
	
	/**
	 * To be used after onRoomUpdated to sync neighbor's positions for current player
	 *
	 * @param buffer initialized only once to maintain performance
	 */
	public void getNearbyPlayerNames(List<String> buffer) {
		buffer.addAll(nearbyPlayers.keySet());
	}
	
	protected MMOPlayer(Builder<?> builder) {
		super(builder);
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	public static class Builder<B extends Builder<B>> extends Player.Builder<B> {
		
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
