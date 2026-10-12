package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.gamebox.manager.SynchronizedRoomManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class MMORoomGroup extends EzyLoggable {

    private volatile boolean active;
    private volatile Thread thread;
    private final long timeTickMillis;
    private final RoomManager<MMORoom> roomManager;
    private final List<MMORoom> rooms
        = new CopyOnWriteArrayList<>();

    private static final AtomicInteger COUNTER
        = new AtomicInteger();

    protected MMORoomGroup(Builder builder) {
        this.timeTickMillis = builder.timeTickMillis;
        this.roomManager = new SynchronizedRoomManager<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    private void start() {
        this.active = true;
        Thread newThread = new Thread(this::loop);
        newThread.setName(
            "game-box-mmo-room-group-" +
                COUNTER.incrementAndGet()
        );
        newThread.setDaemon(true);
        this.thread = newThread;
        newThread.start();
    }

    private void loop() {
        long timeTickNanos = TimeUnit
            .MILLISECONDS
            .toNanos(timeTickMillis);
        long nextTickNanos = System.nanoTime();
        while (active) {
            long start = System.nanoTime();
            this.updateRooms();
            long now = System.nanoTime();
            long timeElapsed = now - start;
            if (timeElapsed > timeTickNanos) {
                logger.warn(
                    "Room group update took: {}ms, " +
                        "exceeded time tick: {}ms",
                    TimeUnit.NANOSECONDS.toMillis(timeElapsed),
                    timeTickMillis
                );
            }
            nextTickNanos = computeNextTickNanos(
                nextTickNanos,
                now,
                timeTickNanos
            );
            long remainingNanos = nextTickNanos - now;
            if (remainingNanos > 0) {
                try {
                    TimeUnit.NANOSECONDS.sleep(remainingNanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    static long computeNextTickNanos(
        long previousTickNanos,
        long nowNanos,
        long timeTickNanos
    ) {
        long nextTickNanos = previousTickNanos + timeTickNanos;
        if (nowNanos - nextTickNanos > timeTickNanos) {
            return nowNanos;
        }
        return nextTickNanos;
    }

    private void updateRooms() {
        for (MMORoom room : rooms) {
            try {
                room.update();
            } catch (Exception e) {
                logger.warn("Update room: {} error", room, e);
            }
        }
    }

    public void addRoom(MMORoom room) {
        synchronized (this) {
            this.roomManager.addRoom(room);
            this.rooms.add(room);
        }
    }

    public boolean removeRoom(MMORoom room) {
        synchronized (this) {
            MMORoom existingRoom = this.roomManager
                .getRoom(room.getName());
            if (existingRoom == null) {
                return false;
            }
            this.roomManager.removeRoom(existingRoom);
            this.rooms.removeIf(it -> it == existingRoom);
            return true;
        }
    }

    public MMORoom getRoom(long roomId) {
        return this.roomManager.getRoom(roomId);
    }

    public MMORoom getRoom(String roomName) {
        return this.roomManager.getRoom(roomName);
    }

    public int getRoomCount() {
        return this.roomManager.getRoomCount();
    }

    public void destroy() {
        this.active = false;
        Thread currentThread = this.thread;
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }

    static class Builder implements EzyBuilder<MMORoomGroup> {
        private long timeTickMillis;

        public Builder timeTickMillis(int timeTickMillis) {
            this.timeTickMillis = timeTickMillis;
            return this;
        }

        @Override
        public MMORoomGroup build() {
            if (timeTickMillis <= 0) {
                throw new IllegalArgumentException(
                    "timeTickMillis must be greater than 0"
                );
            }
            MMORoomGroup group = new MMORoomGroup(this);
            group.start();
            return group;
        }
    }
}
