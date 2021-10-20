package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.manager.SynchronizedPlayerManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MMORoom extends NormalRoom<MMOPlayer> {
	
	protected final List<MMORoomUpdatedHandler> roomUpdatedHandlers;
	@Getter
	protected final double distanceOfInterest;
	@Getter
	protected MMOPlayer master;
	@Getter
	protected int maxPlayer;
	
	public MMORoom(Builder builder) {
		super(builder);
		this.roomUpdatedHandlers = builder.roomUpdatedHandlers;
		this.distanceOfInterest = builder.distanceOfInterest;
		this.maxPlayer = builder.maxPlayer;
	}
	
	@Override
	public void addPlayer(MMOPlayer player) {
		if (playerManager.containsPlayer(player)) {
			return;
		}
		
		synchronized (this) {
			if (playerManager.isEmpty()) {
				master = player;
			}
			super.addPlayer(player);
		}
	}
	
	@Override
	public void removePlayer(MMOPlayer player) {
		synchronized (this) {
			super.removePlayer(player);
			if (master == player && !playerManager.isEmpty()) {
				master = playerManager.getPlayerCollection().getFirst();
			}
		}
	}
	
	public boolean isEmpty() {
		return this.getPlayerManager().isEmpty();
	}
	
	public void update() {
		playerManager.getPlayerCollection().forEach(player -> {
			player.clearNearByPlayers();
			playerManager.getPlayerCollection().forEach(other -> {
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
	
	private void notifyUpdatedHandlers() {
		for (MMORoomUpdatedHandler handler : this.roomUpdatedHandlers) {
			handler.onRoomUpdated(this);
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
			this.playerManager = new SynchronizedPlayerManager<MMOPlayer>(maxPlayer);
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
