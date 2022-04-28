package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.exception.NoSlotException;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import com.tvd12.gamebox.manager.LocatedPlayerManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
public class LocatedRoom extends Room {

    protected final int maxSlot;
    @Setter(AccessLevel.NONE)
    protected final LocatedPlayerManager playerManager;
    @Setter(AccessLevel.NONE)
    protected Queue<Integer> slots;

    public LocatedRoom(Builder<?> builder) {
        super(builder);
        this.maxSlot = builder.maxSlot;
        this.playerManager = builder.playerManager;
        this.slots = newSlots(builder.maxSlot);
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    public int addPlayer(LocatedPlayer player) {
        if (slots.isEmpty()) {
            throw new NoSlotException("has no available slot");
        }
        int location = slots.poll();
        playerManager.addPlayer(player, location);
        return location;
    }

    public void removePlayer(LocatedPlayer player) {
        if (playerManager.containsPlayer(player.getName())) {
            playerManager.removePlayer(player.getLocation());
            slots.add(player.getLocation());
        }

    }

    protected Queue<Integer> newSlots(int maxSlots) {
        Queue<Integer> slots = new LinkedList<>();
        for (int i = 0; i < maxSlots; i++) {
            slots.add(i);
        }
        return slots;
    }

    @SuppressWarnings("unchecked")
    public static class Builder<B extends Builder<B>> extends Room.Builder<B> {

        protected int maxSlot = 999;
        protected LocatedPlayerManager playerManager;

        public B maxSlot(int maxSlot) {
            this.maxSlot = maxSlot;
            return (B) this;
        }

        public B playerManager(LocatedPlayerManager playerManager) {
            this.playerManager = playerManager;
            return (B) this;
        }

        @Override
        protected void preBuild() {
            if (playerManager == null) {
                playerManager = new DefaultLocatedPlayerManager();
            }
        }

        @Override
        public LocatedRoom build() {
            return (LocatedRoom) super.build();
        }

        @Override
        protected LocatedRoom newProduct() {
            return new LocatedRoom(this);
        }
    }

}
