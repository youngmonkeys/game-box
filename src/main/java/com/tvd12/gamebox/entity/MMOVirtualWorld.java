package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.exception.MaxRoomException;

import java.util.concurrent.atomic.AtomicInteger;

public class MMOVirtualWorld extends EzyLoggable {

    private final MMORoomGroup[] roomGroups;
    private final int roomGroupCount;
    private final int timeTickMillis;
    private final int maxRoomCount;
    private final AtomicInteger roomCount = new AtomicInteger();

    protected MMOVirtualWorld(Builder builder) {
        this.roomGroupCount = builder.roomGroupCount;
        this.timeTickMillis = builder.timeTickMillis;
        this.maxRoomCount = builder.maxRoomCount;
        this.roomGroups = this.createRoomGroups();
    }

    private MMORoomGroup[] createRoomGroups() {
        MMORoomGroup[] groups = new MMORoomGroup[this.roomGroupCount];
        for (int i = 0; i < roomGroupCount; i++) {
            groups[i] = MMORoomGroup.builder()
                .timeTickMillis(timeTickMillis)
                .build();
        }
        return groups;
    }

    private MMORoomGroup getRoomGroupByRoomId(long roomId) {
        int roomGroupIndex = (int) Math.floorMod(
            roomId,
            roomGroupCount
        );
        return roomGroups[roomGroupIndex];
    }

    public void addRoom(MMORoom room) {
        MMORoomGroup group = getRoomGroupByRoomId(room.getId());
        int newRoomCount = roomCount.incrementAndGet();
        if (newRoomCount > maxRoomCount) {
            roomCount.decrementAndGet();
            throw new MaxRoomException(
                room.toString(),
                newRoomCount - 1,
                maxRoomCount
            );
        }
        try {
            group.addRoom(room);
        } catch (RuntimeException e) {
            roomCount.decrementAndGet();
            throw e;
        }
    }

    public void removeRoom(MMORoom room) {
        MMORoomGroup group = getRoomGroupByRoomId(room.getId());
        if (group.removeRoom(room)) {
            roomCount.decrementAndGet();
        }
    }

    public MMORoom getRoom(long roomId) {
        MMORoomGroup group = getRoomGroupByRoomId(roomId);
        return group.getRoom(roomId);
    }

    public MMORoom getRoom(String roomName) {
        for (MMORoomGroup roomGroup : roomGroups) {
            MMORoom room = roomGroup.getRoom(roomName);
            if (room != null) {
                return room;
            }
        }
        return null;
    }

    public int getRoomCount() {
        return roomCount.get();
    }

    public void destroy() {
        for (MMORoomGroup roomGroup : roomGroups) {
            roomGroup.destroy();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements EzyBuilder<MMOVirtualWorld> {
        private int timeTickMillis = 100;
        private int roomGroupCount = 2 * Runtime
            .getRuntime()
            .availableProcessors();
        private int maxRoomCount = 10000;

        public Builder maxRoomCount(int maxRoomCount) {
            this.maxRoomCount = maxRoomCount;
            return this;
        }

        public Builder roomGroupCount(int roomGroupCount) {
            this.roomGroupCount = roomGroupCount;
            return this;
        }

        public Builder timeTickMillis(int timeTickMillis) {
            this.timeTickMillis = timeTickMillis;
            return this;
        }

        @Override
        public MMOVirtualWorld build() {
            if (roomGroupCount <= 0) {
                throw new IllegalArgumentException(
                    "roomGroupCount must be greater than 0"
                );
            }
            if (timeTickMillis <= 0) {
                throw new IllegalArgumentException(
                    "timeTickMillis must be greater than 0"
                );
            }
            if (maxRoomCount <= 0) {
                throw new IllegalArgumentException(
                    "maxRoomCount must be greater than 0"
                );
            }
            return new MMOVirtualWorld(this);
        }
    }
}
