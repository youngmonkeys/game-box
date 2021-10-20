package com.tvd12.gamebox.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.function.EzyFunctions;
import com.tvd12.ezyfox.io.EzyLists;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.exception.MaxPlayerException;
import com.tvd12.gamebox.exception.PlayerExistsException;

import com.tvd12.gamebox.util.ReadOnlyList;
import com.tvd12.gamebox.util.ReadOnlySet;
import lombok.Getter;

public abstract class AbstractPlayerManager<P extends Player> 
		extends EzyLoggable 
		implements PlayerManager<P> {

	@Getter
	protected final int maxPlayer;
    protected final Map<String, Lock> locks = newLockMap();
    protected final List<P> playerList = new LinkedList<>();
    protected final Map<String, P> playersByName = newPlayersByNameMap();
    
    public AbstractPlayerManager(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }
    
    protected AbstractPlayerManager(Builder<?, ?> builder) {
        this.maxPlayer = builder.maxPlayer;
    }
    
    @Override
    public P getPlayer(String username) {
        P player = playersByName.get(username);
        return player;
    }
    
    @Override
    public P getPlayer(String username, Supplier<P> playerSupplier) {
    	return playersByName.computeIfAbsent(username, k -> playerSupplier.get());
    }
    
    @Override
    public P getPlayerByIndex(int index) {
    	if(playerList.size() > index)
    		return playerList.get(index);
    	return null;
    }
    
    @Override
    public ReadOnlyList<P> getPlayerList() {
        return new ReadOnlyList<P>(playerList);
    }
	
	@Override
	public void getPlayerList(List<P> buffer) {
		buffer.addAll(playerList);
	}
	
	@Override
    public List<P> getPlayerList(Predicate<P> predicate) {
		List<P> list = new ArrayList<>();
		for(P player : playerList) {
			boolean test = predicate.test(player);
			if(test)
				list.add(player);
		}
		return list;
    }
    
    @Override
    public List<String> getPlayerNames() {
		return new ArrayList<>(playersByName.keySet());
    }

    @Override
    public boolean containsPlayer(String username) {
        boolean contains = playersByName.containsKey(username);
        return contains;
    }
    
    @Override
    public void addPlayer(P player, boolean failIfAdded) {
		addPlayer0(player, failIfAdded);
		logger.info("{} add player: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), player, locks.size(), playersByName.size());
    }
    
    protected void addPlayer0(P player, boolean failIfAdded) {
		int count = playersByName.size();
		if(count >= maxPlayer)
			throw new MaxPlayerException(player.getName(), count, maxPlayer);
		if(playersByName.containsKey(player.getName()) && failIfAdded)
			throw new PlayerExistsException(player.getName());
		addPlayer0(player);
    }
    
    @Override
    public void addPlayers(Collection<P> players, boolean failIfAdded) {
		addPlayers0(players, failIfAdded);
		logger.info("{} add players: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), players, locks.size(), playersByName.size());
    }
    
    protected void addPlayers0(Collection<P> players, boolean failIfAdded) {
		int count = playersByName.size();
		int nextCount = count + players.size();
		if(nextCount > maxPlayer)
			throw new MaxPlayerException(players.size(), count, maxPlayer);
		for(P player : players) {
			if(playersByName.containsKey(player.getName()) && failIfAdded)
    			throw new PlayerExistsException(player.getName());
		}
		players.forEach(this::addPlayer0);
    }
    
    protected void addPlayer0(P player) {
    	playerList.add(player);
		playersByName.put(player.getName(), player);
    }

    @Override
    public P removePlayer(P player) {
		removePlayer0(player);
        logger.info("{} remove player: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), player, locks.size(), playersByName.size());
        return player;
    }
    
    protected void removePlayer0(P player) {
		if(player != null) {
			removePlayer1(player);
        }
    }
    
    protected void removePlayer1(P player) {
        locks.remove(player.getName());
        playerList.remove(player);
        playersByName.remove(player.getName());
    }
    
    @Override
    public void removePlayers(Collection<P> players) {
		removePlayers0(players);
		logger.info("{} remove players: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), players, locks.size(), playersByName.size());
    }
    
    protected void removePlayers0(Collection<P> players) {
    	for(P player : players)
    		removePlayer0(player);
    }
    
    @Override
    public int getPlayerCount() {
        int count = playersByName.size();
        return count;
    }
    
    @Override
    public boolean available() {
        boolean available = playersByName.size() < maxPlayer;
        return available;
    }
    
    @Override
    public Lock getLock(String username) {
        return locks.computeIfAbsent(username, EzyFunctions.NEW_REENTRANT_LOCK_FUNC);
    }
    
    @Override
    public void removeLock(String username) {
        locks.remove(username);
    }
    
    @Override
    public boolean isEmpty() {
    	return playersByName.isEmpty();
    }
    
    @Override
    public int countPlayers(Predicate<P> tester) {
		int count = (int) playersByName.values().stream().filter(tester).count();
		return count;
    }
    
    @Override
    public List<P> filterPlayers(Predicate<P> tester) {
		return EzyLists.filter(playersByName.values(), tester);
    }
    
    @Override
    public void clear() {
        this.unlockAll();
        this.locks.clear();
        this.playersByName.clear();
    }
    
    protected void unlockAll() {
        for(Lock lock : locks.values())
            lock.unlock();
    }
    
    protected String getMessagePrefix() {
        return "user manager:";
    }
    
    protected Map<String, Lock> newLockMap() {
		return new HashMap<>();
	}
	
	protected Map<String, P> newPlayersByNameMap() {
		return new HashMap<>();
	}
    
    public static abstract class Builder<U extends Player, B extends Builder<U, B>> 
            implements EzyBuilder<PlayerManager<U>> {
        
        protected int maxPlayer = 999999;
        
        @SuppressWarnings("unchecked")
        public B maxPlayer(int maxPlayer) {
            this.maxPlayer = maxPlayer;
            return (B)this;
        }
        
        @Override
        public final PlayerManager<U> build() {
    		preBuild();
    		return newProduct();
        }
        
        protected void preBuild() {
        }
        
        protected abstract PlayerManager<U> newProduct();
    }
	
}
