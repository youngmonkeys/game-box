package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.Player;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SynchronizedPlayerManager<P extends Player> extends AbstractPlayerManager<P> {

    @Getter
    protected final Object synchronizedLock = new Object();

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
        synchronized (synchronizedLock) {
            return super.getPlayer(username);
        }
    }

    @Override
    public P getPlayer(String username, Supplier<P> playerSupplier) {
        synchronized (synchronizedLock) {
            return super.getPlayer(username, playerSupplier);
        }
    }

    @Override
    public P getFirstPlayer() {
        synchronized (synchronizedLock) {
            return super.getFirstPlayer();
        }
    }

    @Override
    public List<P> getPlayerList() {
        synchronized (synchronizedLock) {
            return super.getPlayerList();
        }
    }

    @Override
    public List<P> getPlayerList(Predicate<P> predicate) {
        synchronized (synchronizedLock) {
            return super.getPlayerList(predicate);
        }
    }

    @Override
    public void getPlayerList(List<P> buffer) {
        synchronized (synchronizedLock) {
            super.getPlayerList(buffer);
        }
    }

    @Override
    public List<String> getPlayerNames() {
        synchronized (synchronizedLock) {
            return super.getPlayerNames();
        }
    }

    @Override
    public boolean containsPlayer(String username) {
        synchronized (synchronizedLock) {
            return super.containsPlayer(username);
        }
    }

    @Override
    public void addPlayer(P player, boolean failIfAdded) {
        synchronized (synchronizedLock) {
            super.addPlayer(player, failIfAdded);
        }
        logger.info(
            "{} add player: {}, locks.size = {}, playersByName.size = {}",
            getMessagePrefix(),
            player,
            locks.size(),
            playersByName.size()
        );
    }

    @Override
    public void addPlayers(Collection<P> players, boolean failIfAdded) {
        synchronized (synchronizedLock) {
            super.addPlayers(players, failIfAdded);
        }
        logger.info(
            "{} add players: {}, locks.size = {}, playersByName.size = {}",
            getMessagePrefix(),
            players,
            locks.size(),
            playersByName.size()
        );
    }

    @Override
    public P removePlayer(P player) {
        synchronized (synchronizedLock) {
            super.removePlayer(player);
        }
        logger.info(
            "{} remove player: {}, locks.size = {}, playersByName.size = {}",
            getMessagePrefix(),
            player,
            locks.size(),
            playersByName.size()
        );
        return player;
    }

    @Override
    public void removePlayers(Collection<P> players) {
        synchronized (synchronizedLock) {
            super.removePlayers(players);
        }
        logger.info(
            "{} remove players: {}, locks.size = {}, playersByName.size = {}",
            getMessagePrefix(),
            players,
            locks.size(),
            playersByName.size()
        );
    }

    @Override
    public int getPlayerCount() {
        synchronized (synchronizedLock) {
            return super.getPlayerCount();
        }
    }

    @Override
    public boolean available() {
        synchronized (synchronizedLock) {
            return super.available();
        }
    }

    @Override
    public Lock getLock(String username) {
        synchronized (synchronizedLock) {
            return super.getLock(username);
        }
    }

    @Override
    public void removeLock(String username) {
        synchronized (synchronizedLock) {
            super.removeLock(username);
        }
    }

    @Override
    public int countPlayers(Predicate<P> tester) {
        synchronized (synchronizedLock) {
            return super.countPlayers(tester);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (synchronizedLock) {
            return super.isEmpty();
        }
    }

    @Override
    public void clear() {
        synchronized (synchronizedLock) {
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
