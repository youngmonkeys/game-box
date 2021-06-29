package com.tvd12.gamebox.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.tvd12.gamebox.entity.Player;

public class SimplePlayerManager<P extends Player> extends AbstractPlayerManager<P> {
	
	public SimplePlayerManager() {
		this(999999999);
	}

    public SimplePlayerManager(int maxPlayer) {
        super(maxPlayer);
    }
    
    protected SimplePlayerManager(Builder<?, ?> builder) {
        super(builder);
    }
    
    @Override
    protected Map<String, Lock> newLockMap() {
    		return new HashMap<>();
    }
    
    @Override
    protected Map<String, P> newPlayersByNameMap() {
    		return new HashMap<>();
    }
    
    public static Builder<?, ?> builder() {
		return new Builder<>();
    }
    
    public static class Builder<U extends Player, B extends Builder<U, B>> 
            extends AbstractPlayerManager.Builder<U, B> {
        
    		@Override
        protected PlayerManager<U> newProduct() {
        		return new SimplePlayerManager<>(this);
        }
    }
	
}
