package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.Player;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SynchronizedPlayerManager<P extends Player>
    extends AbstractPlayerManager<P> {

    public SynchronizedPlayerManager() {
        this(999999999);
    }

    public SynchronizedPlayerManager(int maxPlayer) {
        super(maxPlayer);
    }

    protected SynchronizedPlayerManager(Builder<?, ?> builder) {
        super(builder);
    }

    @SuppressWarnings("rawtypes")
    public static Builder builder() {
        return new Builder<>();
    }

    @Override
    public P getPlayer(String username) {
        synchronized (this) {
            return super.getPlayer(username);
        }
    }

    @Override
    public P getPlayer(String username, Supplier<P> playerSupplier) {
        synchronized (this) {
            return super.getPlayer(username, playerSupplier);
        }
    }

    @Override
    public P getFirstPlayer() {
        synchronized (this) {
            return super.getFirstPlayer();
        }
    }

    @Override
    public List<P> getPlayerList() {
        synchronized (this) {
            return super.getPlayerList();
        }
    }

    @Override
    public List<P> getPlayerList(Predicate<P> predicate) {
        synchronized (this) {
            return super.getPlayerList(predicate);
        }
    }

    @Override
    public void getPlayerList(List<P> buffer) {
        synchronized (this) {
            super.getPlayerList(buffer);
        }
    }

    @Override
    public List<String> getPlayerNames() {
        synchronized (this) {
            return super.getPlayerNames();
        }
    }

    @Override
    public boolean containsPlayer(String username) {
        synchronized (this) {
            return super.containsPlayer(username);
        }
    }

    @Override
    public void addPlayer(P player, boolean failIfAdded) {
        synchronized (this) {
            super.addPlayer(player, failIfAdded);
        }
    }

    @Override
    public void addPlayers(Collection<P> players, boolean failIfAdded) {
        synchronized (this) {
            super.addPlayers(players, failIfAdded);
        }
    }

    @Override
    public P removePlayer(P player) {
        synchronized (this) {
            return super.removePlayer(player);
        }
    }

    @Override
    public void removePlayers(Collection<P> players) {
        synchronized (this) {
            super.removePlayers(players);
        }
    }

    @Override
    public int getPlayerCount() {
        synchronized (this) {
            return super.getPlayerCount();
        }
    }

    @Override
    public boolean available() {
        synchronized (this) {
            return super.available();
        }
    }

    @Override
    public int countPlayers(Predicate<P> tester) {
        synchronized (this) {
            return super.countPlayers(tester);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return super.isEmpty();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            super.clear();
        }
    }

    public static class Builder<U extends Player, B extends Builder<U, B>>
        extends AbstractPlayerManager.Builder<U, B> {

        @Override
        protected PlayerManager<U> newProduct() {
            return new SynchronizedPlayerManager<>(this);
        }
    }
}
