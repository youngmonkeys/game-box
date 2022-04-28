package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.exception.MaxRoomException;

import java.util.Arrays;

@SuppressWarnings("AbbreviationAsWordInName")
public class MMOVirtualWorld extends EzyLoggable {

    private final MMORoomGroup[] roomGroups;
    private final int roomGroupCount;
    private final int timeTickMillis;
    private final int maxRoomCount;

    protected MMOVirtualWorld(Builder builder) {
        this.roomGroupCount = builder.roomGroupCount;
        this.timeTickMillis = builder.timeTickMillis;
        this.maxRoomCount = builder.maxRoomCount;
        this.roomGroups = this.createRoomGroups();
    }

    public static Builder builder() {
        return new Builder();
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
        int roomGroupIndex = (int) (roomId % roomGroupCount);
        return roomGroups[roomGroupIndex];
    }

    public void addRoom(MMORoom room) {
        if (getRoomCount() >= maxRoomCount) {
            throw new MaxRoomException(room.toString(), getRoomCount(), maxRoomCount);
        }

        MMORoomGroup group = getRoomGroupByRoomId(room.getId());
        group.addRoom(room);
    }

    public void removeRoom(MMORoom room) {
        MMORoomGroup group = getRoomGroupByRoomId(room.getId());
        group.removeRoom(room);
    }

    public MMORoom getRoom(long roomId) {
        MMORoomGroup group = getRoomGroupByRoomId(roomId);
        return group.getRoom(roomId);
    }

    public int getRoomCount() {
        return Arrays.stream(roomGroups)
            .map(MMORoomGroup::getRoomCount)
            .reduce(0, Integer::sum);
    }

    public static class Builder implements EzyBuilder<MMOVirtualWorld> {
        private int timeTickMillis = 100;
        private int roomGroupCount = 2 * Runtime.getRuntime().availableProcessors();
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
            return new MMOVirtualWorld(this);
        }

    }
}
