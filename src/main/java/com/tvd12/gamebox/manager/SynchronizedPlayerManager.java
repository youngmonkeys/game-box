package com.tvd12.gamebox.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.tvd12.ezyfox.io.EzyLists;
import com.tvd12.gamebox.entity.Player;

import lombok.Getter;

public class SynchronizedPlayerManager<P extends Player> extends AbstractPlayerManager<P> {

	@Getter
	protected final Object synchronizedLock = new Object();
	protected final List<P> playerList = new ArrayList<>();
    
    public SynchronizedPlayerManager(int maxPlayer) {
        super(maxPlayer);
    }
    
    protected SynchronizedPlayerManager(Builder<?, ?> builder) {
        super(builder);
    }
    
    @Override
    public P getPlayer(String username) {
    		synchronized (synchronizedLock) {
    			return super.getPlayer(username);
		}
        
    }
    
    @Override
    public List<P> getPlayerList() {
    		synchronized (synchronizedLock) {
    			return playerList;
    		}
    }
    
    @Override
    public List<String> getPlayerNames() {
    	synchronized (synchronizedLock) {
			return EzyLists.newArrayList(playerList, p -> p.getName());
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
    			addPlayer0(player, failIfAdded);
		}
    		logger.info("{} add player: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), player, locks.size(), playersByName.size());
    }
    
    @Override
    public void addPlayers(Collection<P> players, boolean failIfAdded) {
    		synchronized (synchronizedLock) {
    			addPlayers0(players, failIfAdded);
		}
    		logger.info("{} add players: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), players, locks.size(), playersByName.size());
    }
    
    @Override
    protected void addPlayer0(P player) {
    		super.addPlayer0(player);
    		playerList.add(player);
    }
    
    @Override
    public P removePlayer(P player) {
    		synchronized (synchronizedLock) {
	        removePlayer0(player);
    		}
        logger.info("{} remove player: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), player, locks.size(), playersByName.size());
        return player;
    }
    
    @Override
    public void removePlayers(Collection<P> players) {
    		synchronized (synchronizedLock) {
    			removePlayers0(players);
		}
		logger.info("{} remove players: {}, locks.size = {}, playersByName.size = {}", getMessagePrefix(), players, locks.size(), playersByName.size());
    }
    
    @Override
    protected void removePlayer1(P player) {
    		super.removePlayer1(player);
    		playerList.remove(player);
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
    
    public static Builder<?, ?> builder() {
    		return new Builder<>();
    }
    
    public static class Builder<U extends Player, B extends Builder<U, B>> 
            extends AbstractPlayerManager.Builder<U, B> {
        
    		@Override
        protected PlayerManager<U> newProduct() {
        		return new SynchronizedPlayerManager<>(this);
        }
    }
	
}
