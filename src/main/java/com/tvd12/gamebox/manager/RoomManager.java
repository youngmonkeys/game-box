package com.tvd12.gamebox.manager;

import java.util.List;
import java.util.function.Predicate;

import com.tvd12.gamebox.entity.Room;
import com.tvd12.gamebox.util.ReadOnlyCollection;
import com.tvd12.gamebox.util.ReadOnlyList;

public interface RoomManager<R extends Room> {
	
	void addRoom(R room, boolean failIfAdded);
	
	void addRooms(R[] rooms, boolean failIfAdded);
	
	void addRooms(Iterable<R> rooms, boolean failIfAdded);
	
	R getRoom(long id);
	
	R getRoom(String name);
	
	R getRoom(Predicate<R> predicate);
	
	ReadOnlyCollection<R> getRoomList();
	
	void getRoomList(List<R> buffer);
	
	int getRoomCount();
	
	boolean containsRoom(long id);
	
	boolean containsRoom(String name);
	
	void removeRoom(R room);
	
	void removeRoom(long id);
	
	void removeRoom(String name);
	
	void removeRooms(Iterable<R> rooms);
	
	boolean available();
	
	default void addRoom(R room) {
		addRoom(room, true);
	}
	
	default void addRooms(R[] rooms) {
		addRooms(rooms, true);
	}
	
	default void addRooms(Iterable<R> rooms) {
		addRooms(rooms, true);
	}
	
	default boolean containsRoom(R room) {
		return containsRoom(room.getName());
	}
	
}
