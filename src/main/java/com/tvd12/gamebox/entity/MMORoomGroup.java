package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.gamebox.manager.SynchronizedRoomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MMORoomGroup extends EzyLoggable {
	
	private volatile boolean active;
	private final int timeTickMillis;
	private final List<MMORoom> roomsBuffer;
	private final RoomManager<MMORoom> roomManager;
	private final static AtomicInteger COUNTER = new AtomicInteger();
	
	protected MMORoomGroup(Builder builder) {
		this.timeTickMillis = builder.timeTickMillies;
		this.roomsBuffer = new ArrayList<>();
		this.roomManager = new SynchronizedRoomManager<>();
		this.start();
	}
	
	private void start() {
		Thread newThread = new Thread(() -> loop());
		newThread.setName("game-box-mmo-room-group-" + COUNTER.incrementAndGet());
		newThread.start();
	}
	
	private void loop() {
		this.active = true;
		while (active) {
			try {
				this.updateRooms();
				Thread.sleep(timeTickMillis);
			} catch (Exception e) {
				logger.error("Room group loop error: ", e);
			}
		}
	}
	
	private void updateRooms() {
		this.roomsBuffer.clear();
		this.roomManager.getRoomList(roomsBuffer);
		for (MMORoom room : roomsBuffer) {
			try {
				room.update();
			} catch (Exception e) {
				logger.warn("Update room: {} error", room, e);
			}
		}
	}
	
	public void addRoom(MMORoom room) {
		this.roomManager.addRoom(room);
	}
	
	public void removeRoom(MMORoom room) {
		this.roomManager.removeRoom(room);
	}
	
	public void destroy() {
		this.active = false;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<MMORoomGroup> {
		private int timeTickMillies;
		
		public Builder timeTickMillies(int timeTickMillies) {
			this.timeTickMillies = timeTickMillies;
			return this;
		}
		
		@Override
		public MMORoomGroup build() {
			return new MMORoomGroup(this);
		}
	}
}
