package com.tvd12.gamebox.entity;

import java.util.LinkedList;
import java.util.Queue;

import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.gamebox.exception.NoSlotException;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import com.tvd12.gamebox.manager.LocatedPlayerManager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LocatedRoom extends Room {

	protected int maxSlots;
	@Setter(AccessLevel.NONE)
	protected Queue<Integer> slots; 
	@Setter(AccessLevel.NONE)
	protected final LocatedPlayerManager playerManager;
	
	public LocatedRoom(Builder<?> builder) {
		super(builder);
		this.playerManager = builder.playerManager;
	}
	
	public void setMaxSlots(int maxSlots) {
		this.maxSlots = maxSlots;
		this.slots = newSlots(maxSlots);
	}
	
	public int addUser(EzyUser user, LocatedPlayer player) {
		if(slots.isEmpty())
			throw new NoSlotException("has no available slot");
		int localtion = slots.poll();
		player.setLocation(localtion);
		userManager.addUser(user);
		playerManager.addPlayer(player, localtion);
		return localtion;
	}
	
	public void removePlayer(LocatedPlayer player) {
		userManager.removeUser(player.getName());
		playerManager.removePlayer(player.getLocation());
		slots.add(player.getLocation());
	}
	
	protected Queue<Integer> newSlots(int maxSlots) {
		Queue<Integer> slots = new LinkedList<>();
		for(int i = 0 ; i < maxSlots ; i++)
			slots.add(i);
		return slots;
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	@SuppressWarnings("unchecked")
	public static class Builder<B extends Builder<B>> extends Room.Builder<B> {
		
		protected LocatedPlayerManager playerManager;
		
		public B playerManager(LocatedPlayerManager playerManager) {
			this.playerManager = playerManager;
			return (B)this;
		}
		
		@Override
		protected void preBuild() {
			if(playerManager == null)
				playerManager = new DefaultLocatedPlayerManager();
		}
		
		@Override
		public LocatedRoom build() {
			return (LocatedRoom) super.build();
		}
		
		@Override
		protected LocatedRoom newProduct() {
			return new LocatedRoom(this);
		}
	}
	
}
