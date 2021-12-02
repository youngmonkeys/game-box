package com.tvd12.gamebox.manager;

import java.util.List;
import java.util.function.Predicate;

import com.tvd12.gamebox.entity.Room;

public interface RoomManager<R extends Room> {

    void addRoom(R room, boolean failIfAdded);

    default void addRoom(R room) {
        addRoom(room, true);
    }

    void addRooms(R[] rooms, boolean failIfAdded);

    default void addRooms(R[] rooms) {
        addRooms(rooms, true);
    }

    void addRooms(Iterable<R> rooms, boolean failIfAdded);

    default void addRooms(Iterable<R> rooms) {
        addRooms(rooms, true);
    }

    R getRoom(long id);

    R getRoom(String name);

    R getRoom(Predicate<R> predicate);

    List<R> getRoomList();

    void getRoomList(List<R> buffer);

    int getRoomCount();

    boolean containsRoom(long id);

    boolean containsRoom(String name);

    default boolean containsRoom(R room) {
        return containsRoom(room.getName());
    }

    void removeRoom(R room);

    void removeRoom(long id);

    void removeRoom(String name);

    void removeRooms(Iterable<R> rooms);

    boolean available();
}
