package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.constant.RoomStatus;
import com.tvd12.gamebox.manager.DefaultMMOPlayerManager;
import com.tvd12.gamebox.manager.DefaultPlayerManager;
import com.tvd12.gamebox.manager.PlayerManager;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.List;

public class MMORoom extends NormalRoom {
	
	public static final long MS_PER_FRAME = 100;
	
	@Setter(AccessLevel.NONE)
	protected final PlayerManager<MMOPlayer> playerManager;
	
	protected final double distanceOfInterest;
	
	public MMORoom(Builder<?> builder) {
		super(builder);
		this.playerManager = builder.playerManager;
		this.distanceOfInterest = builder.distanceOfInterest;
	}
	
	public void start() {
		status = RoomStatus.PLAYING;
		
		runGameLoop();
	}
	
	private void runGameLoop() {
		while (status == RoomStatus.PLAYING) {
			long now = System.currentTimeMillis();
			
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
			
			long elapsedTime = System.currentTimeMillis() - now;
			try {
				Thread.sleep(MS_PER_FRAME - elapsedTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	public static class Builder<B extends Builder<B>> extends NormalRoom.Builder<B> {
		
		protected PlayerManager<MMOPlayer> playerManager;
		protected double distanceOfInterest;
		
		public B distanceOfInterest(double distanceOfInterest) {
			this.distanceOfInterest = distanceOfInterest;
			return (B) this;
		}
		
		@Override
		public B defaultPlayerManager(int maxPlayer) {
			this.playerManager = new DefaultMMOPlayerManager(maxPlayer);
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
