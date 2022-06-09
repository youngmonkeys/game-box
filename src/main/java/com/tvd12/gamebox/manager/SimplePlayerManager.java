package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
