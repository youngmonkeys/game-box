package com.tvd12.gamebox.entity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class MMORoom extends NormalRoom {
	
	protected final List<MMORoomUpdateFinishedHandler> updateFinishedHandlers;
	protected final double distanceOfInterest;
	
	public MMORoom(Builder<?> builder) {
		super(builder);
		this.updateFinishedHandlers = builder.updateFinishedHandlers;
		this.distanceOfInterest = builder.distanceOfInterest;
	}
	
	public void update() {
		List<MMOPlayer> playerList = playerManager.getPlayerList();
		
		for (MMOPlayer player : playerList) {
			for (MMOPlayer other : playerList) {
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
		
		notifyUpdateFinishedHandlers();
	}
	
	private void notifyUpdateFinishedHandlers() {
		for (MMORoomUpdateFinishedHandler handler : this.updateFinishedHandlers) {
			handler.onRoomUpdated(this);
		}
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	public static class Builder<B extends Builder<B>> extends NormalRoom.Builder<B> {
		protected List<MMORoomUpdateFinishedHandler> updateFinishedHandlers = new ArrayList<>();
		protected double distanceOfInterest;
		
		public B addRoomUpdateFinishedHandler(MMORoomUpdateFinishedHandler handler) {
			this.updateFinishedHandlers.add(handler);
			return (B) this;
		}
		
		public B distanceOfInterest(double distance) {
			this.distanceOfInterest = distance;
			return (B) this;
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
