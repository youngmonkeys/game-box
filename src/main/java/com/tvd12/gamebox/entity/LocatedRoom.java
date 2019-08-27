package com.tvd12.gamebox.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.ezyfoxserver.wrapper.EzyUserManager;
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
	protected final LocatedPlayerManager locatedPlayerManager;
	
	public LocatedRoom(Builder<?> builder) {
		super(builder);
		this.locatedPlayerManager = builder.locatedPlayerManager;
	}
	
	protected LocatedRoom(
			long id, 
			String name, 
			EzyUserManager userManager,
			LocatedPlayerManager locatedPlayerManager) {
		super(id, name, userManager);
		this.locatedPlayerManager = locatedPlayerManager;
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
		locatedPlayerManager.addPlayer(player, localtion);
		return localtion;
	}
	
	public void removePlayer(LocatedPlayer player) {
		userManager.removeUser(player.getName());
		locatedPlayerManager.removePlayer(player.getLocation());
		slots.add(player.getLocation());
	}
	
	protected Queue<Integer> newSlots(int maxSlots) {
		Queue<Integer> slots = new LinkedList<>();
		for(int i = 0 ; i < maxSlots ; i++)
			slots.add(i);
		return slots;
	}
	
	protected Map<String, LocatedPlayer> newPlayerByNamesMap() {
		return new HashMap<>();
	}
	
	protected Map<Integer, LocatedPlayer> newPlayerByLocation() {
		return new HashMap<>();
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	@SuppressWarnings("unchecked")
	public static class Builder<B extends Builder<B>> extends Room.Builder<B> {
		
		protected LocatedPlayerManager locatedPlayerManager;
		
		public B locatedPlayerManager(LocatedPlayerManager locatedPlayerManager) {
			this.locatedPlayerManager = locatedPlayerManager;
			return (B)this;
		}
		
		@Override
		protected void preBuild() {
			if(locatedPlayerManager == null)
				locatedPlayerManager = new DefaultLocatedPlayerManager();
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
