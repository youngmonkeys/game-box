package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.manager.SynchronizedPlayerManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class MMORoom extends NormalRoom {
	
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
				master = (MMOPlayer) playerManager.getPlayerByIndex(0);
			}
		}
	}
	
	public boolean isEmpty() {
		return this.getPlayerManager().isEmpty();
	}
	
	public void update() {
		playerManager.getPlayerList().forEach(player -> {
			((MMOPlayer) player).clearNearByPlayers();
			playerManager.getPlayerList().forEach(other -> {
				double distance = ((MMOPlayer) player).getPosition().distance(((MMOPlayer) other).getPosition());
				if (distance <= this.distanceOfInterest) {
					((MMOPlayer) player).addNearbyPlayer((MMOPlayer) other);
				} else {
					((MMOPlayer) player).removeNearByPlayer((MMOPlayer) other);
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
