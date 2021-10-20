package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.manager.DefaultPlayerManager;
import com.tvd12.gamebox.manager.PlayerManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@SuppressWarnings({"unchecked"})
public class NormalRoom<P extends Player> extends Room {
	
	@Setter(AccessLevel.NONE)
	protected final PlayerManager<P> playerManager;
	
	public NormalRoom(Builder<P, ?> builder) {
		super(builder);
		this.playerManager = builder.playerManager;
	}
	
	public void addPlayer(P player) {
		playerManager.addPlayer(player);
	}
	
	public void removePlayer(P player) {
		playerManager.removePlayer(player.getName());
	}
	
	public <T extends P> PlayerManager<T> getPlayerManager() {
		return (PlayerManager<T>) playerManager;
	}
	
	@SuppressWarnings("rawtypes")
	public static Builder builder() {
		return new Builder<>();
	}
	
	public static class Builder<P extends Player, B extends Builder<P, B>> extends Room.Builder<B> {
		protected PlayerManager<P> playerManager;
		
		public B playerManager(PlayerManager<P> playerManager) {
			this.playerManager = playerManager;
			return (B)this;
		}
		
		public B defaultPlayerManager(int maxPlayer) {
			this.playerManager = new DefaultPlayerManager<>(maxPlayer);
			return (B)this;
		}
		
		@Override
		protected void preBuild() {
			if(playerManager == null)
				playerManager = new DefaultPlayerManager<>(999);
		}
		
		@Override
		public NormalRoom<P> build() {
			return (NormalRoom<P>) super.build();
		}
		
		@Override
		protected NormalRoom<P> newProduct() {
			return new NormalRoom<>(this);
		}
	}
}
