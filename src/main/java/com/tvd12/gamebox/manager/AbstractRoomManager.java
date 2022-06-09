package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.gamebox.entity.Room;
import com.tvd12.gamebox.exception.MaxRoomException;
import com.tvd12.gamebox.exception.RoomExistsException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractRoomManager<R extends Room>
    implements RoomManager<R> {

    @Getter
    protected final int maxRoom;
    protected final Map<Long, R> roomById = newRoomByIdMap();
    protected final Map<String, R> roomByName = newRoomByNameMap();

    public AbstractRoomManager() {
        this(10000);
    }

    public AbstractRoomManager(int maxRoom) {
        this.maxRoom = maxRoom;
    }

    protected AbstractRoomManager(Builder<?, ?> builder) {
        this(builder.maxRoom);
    }

    @Override
    public void addRoom(R room, boolean failIfAdded) {
        int roomCount = roomByName.size();
        if (roomCount >= maxRoom) {
            throw new MaxRoomException(room.toString(), roomCount, maxRoom);
        }
        String roomName = room.getName();
        boolean exists = roomByName.containsKey(roomName);
        if (exists && failIfAdded) {
            throw new RoomExistsException(roomName);
        }
        roomById.put(room.getId(), room);
        roomByName.put(roomName, room);
    }

    @Override
    public void addRooms(R[] rooms, boolean failIfAdded) {
        addRooms(Arrays.asList(rooms), failIfAdded);
    }

    @Override
    public void addRooms(Iterable<R> rooms, boolean failIfAdded) {
        int count = 0;
        if (failIfAdded) {
            for (R room : rooms) {
                if (roomByName.containsKey(room.getName())) {
                    throw new RoomExistsException(room.getName());
                }
                ++count;
            }
        }
        int roomCount = roomByName.size();
        int nextRoomCount = roomCount + count;
        if (nextRoomCount > maxRoom) {
            throw new MaxRoomException(count, roomCount, maxRoom);
        }
        for (R room : rooms) {
            roomById.put(room.getId(), room);
            roomByName.put(room.getName(), room);
        }
    }

    @Override
    public boolean containsRoom(long id) {
        return roomById.containsKey(id);
    }

    @Override
    public boolean containsRoom(String name) {
        return roomByName.containsKey(name);
    }

    @Override
    public R getRoom(long id) {
        return roomById.get(id);
    }

    @Override
    public R getRoom(String name) {
        return roomByName.get(name);
    }

    @Override
    public R getRoom(Predicate<R> predicate) {
        for (R room : roomByName.values()) {
            if (predicate.test(room)) {
                return room;
            }
        }
        return null;
    }

    @Override
    public List<R> getRoomList() {
        return new ArrayList<>(roomByName.values());
    }

    @Override
    public List<R> getRoomList(Predicate<R> predicate) {
        return roomByName
            .values()
            .stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    @Override
    public void getRoomList(List<R> buffer) {
        buffer.addAll(roomByName.values());
    }

    @Override
    public int getRoomCount() {
        return roomByName.size();
    }

    @Override
    public void removeRoom(R room) {
        doRemoveRoom(room);
    }

    @Override
    public void removeRoom(long id) {
        doRemoveRoom(roomById.get(id));
    }

    @Override
    public void removeRoom(String name) {
        doRemoveRoom(roomByName.get(name));
    }

    @Override
    public void removeRooms(Iterable<R> rooms) {
        for (R room : rooms) {
            doRemoveRoom(room);
        }
    }

    protected void doRemoveRoom(R room) {
        if (room != null) {
            roomById.remove(room.getId());
            roomByName.remove(room.getName());
        }
    }

    @Override
    public boolean available() {
        return roomById.size() < maxRoom;
    }

    public void clear() {
        roomById.clear();
        roomByName.clear();
    }

    protected Map<Long, R> newRoomByIdMap() {
        return new ConcurrentHashMap<>();
    }

    protected Map<String, R> newRoomByNameMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public String toString() {
        return "roomByName.size = " + roomByName.size() +
            ", roomById.size = " + roomById.size() ;
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<R extends Room, B extends Builder<R, B>>
        implements EzyBuilder<RoomManager<R>> {

        protected int maxRoom = 10000;

        public B maxRoom(int maxRoom) {
            this.maxRoom = maxRoom;
            return (B) this;
        }
    }
}
