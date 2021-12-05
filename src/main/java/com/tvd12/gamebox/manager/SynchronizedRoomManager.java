package com.tvd12.gamebox.manager;

import java.util.List;
import java.util.function.Predicate;

import com.tvd12.gamebox.entity.Room;

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
        synchronized (synchronizedLock) {
            super.addRoom(room, failIfAdded);
        }
        logger.info(
                "{} add rooms: {}, roomsByName.size = {}, roomsById.size = {}",
                getMessagePrefix(),
                room,
                roomsByName.size(),
                roomsById.size()
        );
    }
    
    @Override
    public void addRooms(R[] rooms, boolean failIfAdded) {
        synchronized (synchronizedLock) {
            super.addRooms(rooms, failIfAdded);
        }
    }

    @Override
    public void addRooms(Iterable<R> rooms, boolean failIfAdded) {
        synchronized (synchronizedLock) {
            super.addRooms(rooms, failIfAdded);
        }
        logger.info(
                "{} add rooms: {}, roomsByName.size = {}, roomsById.size = {}",
                getMessagePrefix(),
                rooms,
                roomsByName.size(),
                roomsById.size()
        );
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
    public R getRoom(Predicate<R> predicate) {
        synchronized (synchronizedLock) {
            return super.getRoom(predicate);
        }
    }

    @Override
    public List<R> getRoomList() {
        synchronized (synchronizedLock) {
            return super.getRoomList();
        }
    }

    @Override
    public void getRoomList(List<R> buffer) {
        synchronized (synchronizedLock) {
            super.getRoomList(buffer);
        }
    }
    
    @Override
    public List<R> getRoomList(Predicate<R> predicate) {
        synchronized (synchronizedLock) {
            return super.getRoomList(predicate);
        }
    }
    
    @Override
    public int getRoomCount() {
        synchronized (synchronizedLock) {
            return super.getRoomCount();
        }
    }

    @Override
    public boolean containsRoom(long id) {
        synchronized (synchronizedLock) {
            return super.containsRoom(id);   
        }
    }
    
    @Override
    public boolean containsRoom(String name) {
        synchronized (synchronizedLock) {
            return super.containsRoom(name);
        }
    }
    
    @Override
    public void removeRoom(R room) {
        synchronized (synchronizedLock) {
            super.removeRoom(room);
        }
        logger.info(
                "{} remove room: {}, roomsByName.size = {}, roomsById.size = {}",
                getMessagePrefix(),
                room,
                roomsByName.size(),
                roomsById.size()
        );
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
            super.removeRooms(rooms);
        }
        logger.info(
                "{} remove rooms: {}, roomsByName.size = {}, roomsById.size = {}",
                getMessagePrefix(),
                rooms,
                roomsByName.size(),
                roomsById.size()
        );
    }

    @Override
    public boolean available() {
        synchronized (synchronizedLock) {
            return super.available();
        }
    }
    
    @Override
    public void clear() {
        synchronized (synchronizedLock) {
            super.clear();   
        }
    }

    @SuppressWarnings("rawtypes")
    public static Builder builder() {
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
