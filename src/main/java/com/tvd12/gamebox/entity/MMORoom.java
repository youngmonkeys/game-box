package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.manager.SynchronizedPlayerManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class MMORoom extends NormalRoom {
	
	protected final List<MMOPlayer> playersBuffer;
	protected final List<MMORoomUpdatedHandler> roomUpdatedHandlers;
	protected final double distanceOfInterest;
	
	public MMORoom(Builder builder) {
		super(builder);
		this.playersBuffer = new ArrayList<>();
		this.roomUpdatedHandlers = builder.roomUpdatedHandlers;
		this.distanceOfInterest = builder.distanceOfInterest;
	}
	
	public void update() {
		playersBuffer.clear();
		playerManager.getPlayerList(playersBuffer);
		
		for (MMOPlayer player : playersBuffer) {
			for (MMOPlayer other : playersBuffer) {
				if (!other.equals(player)) {
					double distance = player.getPosition().distance(other.getPosition());
					if (distance <= this.distanceOfInterest) {
						player.addNearbyPlayer(other);
					} else {
						player.removeNearByPlayer(other);
					}
				}
			}
		}
		
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
		
		public Builder() {
			this.roomUpdatedHandlers = new ArrayList<>();
		}
		
		public Builder addRoomUpdatedHandler(MMORoomUpdatedHandler handler) {
			this.roomUpdatedHandlers.add(handler);
			return this;
		}
		
		public Builder distanceOfInterest(double distance) {
			this.distanceOfInterest = distance;
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
				playerManager = new SynchronizedPlayerManager<>();
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
