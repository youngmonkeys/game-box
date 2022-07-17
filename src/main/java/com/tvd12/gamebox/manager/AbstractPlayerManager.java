package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.io.EzyLists;
import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.exception.MaxPlayerException;
import com.tvd12.gamebox.exception.PlayerExistsException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractPlayerManager<P extends Player>
    implements PlayerManager<P> {

    @Getter
    protected final int maxPlayer;
    protected final Map<String, P> playerByName = newPlayerByNameMap();

    public AbstractPlayerManager(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    protected AbstractPlayerManager(Builder<?, ?> builder) {
        this.maxPlayer = builder.maxPlayer;
    }

    @Override
    public P getPlayer(String username) {
        return playerByName.get(username);
    }

    @Override
    public P getPlayer(String username, Supplier<P> playerSupplier) {
        return playerByName.computeIfAbsent(username, k -> playerSupplier.get());
    }

    @Override
    public P getFirstPlayer() {
        return playerByName.isEmpty() ? null : playerByName.values().iterator().next();
    }

    @Override
    public List<P> getPlayerList() {
        return new ArrayList<>(playerByName.values());
    }

    @Override
    public void getPlayerList(List<P> buffer) {
        buffer.addAll(playerByName.values());
    }

    @Override
    public List<P> getPlayerList(Predicate<P> predicate) {
        return playerByName.values()
            .stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getPlayerNames() {
        return new ArrayList<>(playerByName.keySet());
    }

    @Override
    public boolean containsPlayer(String username) {
        return playerByName.containsKey(username);
    }

    @Override
    public void addPlayer(P player, boolean failIfAdded) {
        int count = playerByName.size();
        if (count >= maxPlayer) {
            throw new MaxPlayerException(player.getName(), count, maxPlayer);
        }
        String playerName = player.getName();
        if (playerByName.containsKey(playerName) && failIfAdded) {
            throw new PlayerExistsException(playerName);
        }
        playerByName.put(playerName, player);
    }

    @Override
    public void addPlayers(Collection<P> players, boolean failIfAdded) {
        int count = playerByName.size();
        int nextCount = count + players.size();
        if (nextCount > maxPlayer) {
            throw new MaxPlayerException(players.size(), count, maxPlayer);
        }
        for (P player : players) {
            if (playerByName.containsKey(player.getName()) && failIfAdded) {
                throw new PlayerExistsException(player.getName());
            }
        }
        for (P player : players) {
            playerByName.put(player.getName(), player);
        }
    }

    @Override
    public P removePlayer(P player) {
        if (player != null) {
            playerByName.remove(player.getName());
        }
        return player;
    }

    @Override
    public void removePlayers(Collection<P> players) {
        for (P player : players) {
            playerByName.remove(player.getName());
        }
    }

    @Override
    public int getPlayerCount() {
        return playerByName.size();
    }

    @Override
    public boolean available() {
        return playerByName.size() < maxPlayer;
    }

    @Override
    public boolean isEmpty() {
        return playerByName.isEmpty();
    }

    @Override
    public int countPlayers(Predicate<P> tester) {
        return (int) playerByName
            .values()
            .stream()
            .filter(tester)
            .count();
    }

    @Override
    public void forEach(Consumer<P> consumer) {
        playerByName.values().forEach(consumer);
    }

    @Override
    public List<P> filterPlayers(Predicate<P> tester) {
        return EzyLists.filter(playerByName.values(), tester);
    }

    @Override
    public void clear() {
        this.playerByName.clear();
    }

    protected Map<String, P> newPlayerByNameMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public String toString() {
        return "playerByName.size = " + playerByName.size();
    }

    public abstract static class Builder<U extends Player, B extends Builder<U, B>>
        implements EzyBuilder<PlayerManager<U>> {

        protected int maxPlayer = 999999;

        @SuppressWarnings("unchecked")
        public B maxPlayer(int maxPlayer) {
            this.maxPlayer = maxPlayer;
            return (B) this;
        }

        @Override
        public final PlayerManager<U> build() {
            preBuild();
            return newProduct();
        }

        protected void preBuild() {}

        protected abstract PlayerManager<U> newProduct();
    }
}
