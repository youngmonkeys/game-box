package com.tvd12.gamebox.manager;

import java.util.List;

import com.tvd12.gamebox.entity.Room;
import com.tvd12.gamebox.exception.RoomExistsException;

import lombok.Getter;

public class SynchronizedRoomManager<R extends Room> extends AbstractRoomManager<R> {

	@Getter
	protected final Object synchronizedLock = new Object();
	
	public SynchronizedRoomManager() {
		this(10000);
	}
	
	public SynchronizedRoomManager(int maxRoom) {
		super(maxRoom);
	}
	
	protected SynchronizedRoomManager(Builder<?, ?> builder) {
		super(builder);
	}
	
	@Override
	public void addRoom(R room, boolean failIfAdded) {
		boolean exists = false;
		synchronized(synchronizedLock) {
			exists = addRoom0(room, failIfAdded);
		}
		if(exists && failIfAdded)
			throw new RoomExistsException(room.getName());
		logger.info("{} add rooms: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), room, roomsByName.size(), roomsById.size());
	}

	@Override
	public void addRooms(Iterable<R> rooms, boolean failIfAdded) {
		synchronized (synchronizedLock) {
			addRooms0(rooms, failIfAdded);
		}
		logger.info("{} add rooms: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), rooms, roomsByName.size(), roomsById.size());
	}
	
	@Override
	public R getRoom(long id) {
		synchronized (synchronizedLock) {
			return super.getRoom(id);
		}
	}

	@Override
	public R getRoom(String name) {
		synchronized (synchronizedLock) {
			return super.getRoom(name);
		}
	}

	@Override
	public List<R> getRoomList() {
		synchronized (synchronizedLock) {
			return super.getRoomList();
		}
	}
	
	@Override
	public int getRoomCount() {
		synchronized (synchronizedLock) {
			return super.getRoomCount();
		}
	}

	@Override
	public void removeRoom(R room) {
		synchronized (synchronizedLock) {
			removeRoom0(room);
		}
		logger.info("{} remove room: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), room, roomsByName.size(), roomsById.size());
	}

	@Override
	public void removeRoom(long id) {
		synchronized (synchronizedLock) {
			super.removeRoom(id);
		}
	}

	@Override
	public void removeRoom(String name) {
		synchronized (synchronizedLock) {
			super.removeRoom(name);
		}
	}

	@Override
	public void removeRooms(Iterable<R> rooms) {
		synchronized (synchronizedLock) {
			removeRooms0(rooms);
		}
		logger.info("{} remove rooms: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), rooms, roomsByName.size(), roomsById.size());
	}
	
	@Override
	public boolean available() {
		synchronized (synchronizedLock) {
			return super.available();
		}
	}
	
	public static Builder<?, ?> builder() {
		return new Builder<>();
	}
	
	public static class Builder<R extends Room, B extends Builder<R, B>> 
			extends AbstractRoomManager.Builder<R, B> {

		@Override
		public RoomManager<R> build() {
			return new SynchronizedRoomManager<>(this);
		}
		
	}
	
}
