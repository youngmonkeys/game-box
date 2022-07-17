package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.Room;

public class SimpleRoomManager<R extends Room> extends AbstractRoomManager<R> {

    public SimpleRoomManager() {
        this(10000);
    }

    public SimpleRoomManager(int maxRoom) {
        super(maxRoom);
    }

    protected SimpleRoomManager(Builder<?, ?> builder) {
        super(builder);
    }

    public static Builder<?, ?> builder() {
        return new Builder<>();
    }

    public static class Builder<R extends Room, B extends Builder<R, B>>
        extends AbstractRoomManager.Builder<R, B> {

        @Override
        public SimpleRoomManager<R> build() {
            return new SimpleRoomManager<>(this);
        }
    }
}
