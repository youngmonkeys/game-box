package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MMOPlayer extends Player {
	
	@Setter
	protected Vec3 position;
	
	@Setter
	protected Vec3 rotation;
	
	protected List<MMOPlayer> nearbyPlayers;
	
	public MMOPlayer(String name) {
		super(name);
		
		nearbyPlayers = new ArrayList<>();
	}
	
	public void addNearbyPlayer(MMOPlayer otherPlayer) {
		this.nearbyPlayers.add(otherPlayer);
	}
	
	public void removeNearByPlayer(MMOPlayer otherPlayer) {
		this.nearbyPlayers.remove(otherPlayer);
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
