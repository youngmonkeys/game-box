package com.tvd12.gamebox.manager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.Room;
import com.tvd12.gamebox.exception.MaxRoomException;
import com.tvd12.gamebox.exception.RoomExistsException;

import com.tvd12.gamebox.util.ReadOnlyCollection;
import lombok.Getter;

public abstract class AbstractRoomManager<R extends Room> 
		extends EzyLoggable
		implements RoomManager<R> {

	@Getter
	protected final int maxRoom;
	protected final Map<Long, R> roomsById = newRoomsByIdMap();
	protected final Map<String, R> roomsByName = newRoomsByNameMap();
	
	public AbstractRoomManager() {
		this(10000);
	}
	
	public AbstractRoomManager(int maxRoom) {
		this.maxRoom = maxRoom;
	}
	
	protected AbstractRoomManager(Builder<?, ?> builder) {
		this(
			builder.maxRoom
		);
	}
	
	@Override
	public void addRoom(R room, boolean failIfAdded) {
		boolean exists = addRoom0(room, failIfAdded);
		if(exists && failIfAdded)
			throw new RoomExistsException(room.getName());
		logger.info("{} add rooms: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), room, roomsByName.size(), roomsById.size());
	}
	
	protected boolean addRoom0(R room, boolean failIfAdded) {
		int roomCount = roomsByName.size();
		if(roomCount >= maxRoom)
			throw new MaxRoomException(room.toString(), roomCount, maxRoom);
		boolean exists = roomsById.containsKey(room.getId()) || 
				 roomsByName.containsKey(room.getName());
		if(!exists)
			addRoom0(room);
		return exists;
	}

	@Override
	public void addRooms(R[] rooms, boolean failIfAdded) {
		addRooms(Arrays.asList(rooms), failIfAdded);
	}

	@Override
	public void addRooms(Iterable<R> rooms, boolean failIfAdded) {
		addRooms0(rooms, failIfAdded);
		logger.info("{} add rooms: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), rooms, roomsByName.size(), roomsById.size());
	}
	
	protected void addRooms0(Iterable<R> rooms, boolean failIfAdded) {
		int count = 0;
		if(failIfAdded) {
			for(R room : rooms) {
				if(roomsById.containsKey(room.getId()) ||
						roomsByName.containsKey(room.getName()))
					throw new RoomExistsException(room.getName());
				++ count;
			}
		}
		int roomCount = roomsByName.size();
		int nextRoomCount = roomCount + count;
		if(nextRoomCount >= maxRoom)
			throw new MaxRoomException(count, roomCount, maxRoom);
		for(R room : rooms)
			addRoom0(room);
	}
	
	protected void addRoom0(R room) {
		roomsById.put(room.getId(), room);
		roomsByName.put(room.getName(), room);
	}
	
	@Override
	public boolean containsRoom(long id) {
		boolean contains = roomsById.containsKey(id);
		return contains;
	}
	
	@Override
	public boolean containsRoom(String name) {
		boolean contains = roomsByName.containsKey(name);
		return contains;
	}

	@Override
	public R getRoom(long id) {
		R room = roomsById.get(id);
		return room;
	}

	@Override
	public R getRoom(String name) {
		R room = roomsByName.get(name);
		return room;
	}
	
	@Override
	public R getRoom(Predicate<R> predicate) {
		for(R room : roomsByName.values()) {
			if(predicate.test(room))
				return room;
		}
		return null;
	}

	@Override
	public ReadOnlyCollection<R> getRoomList() {
		return new ReadOnlyCollection<R>(roomsByName.values());
	}
	
	@Override
	public void getRoomList(List<R> buffer) {
		buffer.addAll(roomsByName.values());
	}
	
	@Override
	public int getRoomCount() {
		int count = roomsByName.size();
		return count;
	}

	@Override
	public void removeRoom(R room) {
		removeRoom0(room);
		logger.info("{} remove room: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), room, roomsByName.size(), roomsById.size());
	}

	@Override
	public void removeRoom(long id) {
		removeRoom(roomsById.get(id));
	}

	@Override
	public void removeRoom(String name) {
		removeRoom(roomsByName.get(name));
	}

	@Override
	public void removeRooms(Iterable<R> rooms) {
		removeRooms0(rooms);
		logger.info("{} remove rooms: {}, roomsByName.size = {}, roomsById.size = {}", getMessagePrefix(), rooms, roomsByName.size(), roomsById.size());
	}
	
	protected void removeRooms0(Iterable<R> rooms) {
		for(R room : rooms)
			removeRoom0(room);
	}
	
	protected void removeRoom0(R room) {
		if(room != null) {
			removeRoom1(room);
		}
	}
	
	protected void removeRoom1(R room) {
		roomsById.remove(room.getId());
		roomsByName.remove(room.getName());
	}
	
	@Override
	public boolean available() {
		boolean answer = roomsById.size() < maxRoom;
		return answer;
	}
	
	protected String getMessagePrefix() {
		return "game room manager: ";
	}
	
	protected Map<Long, R> newRoomsByIdMap() {
		return new HashMap<>();
	}
	
	protected Map<String, R> newRoomsByNameMap() {
		return new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	public abstract static class Builder<R extends Room, B extends Builder<R, B>> 
			implements EzyBuilder<RoomManager<R>> {

		protected int maxRoom = 10000;
		
		public B maxRoom(int maxRoom) {
			this.maxRoom = maxRoom;
			return (B)this;
		}
		
	}
	
}
