package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.Player;

public class DefaultPlayerManager<P extends Player> extends AbstractPlayerManager<P> {

    public DefaultPlayerManager() {
        this(999999999);
    }

    public DefaultPlayerManager(int maxPlayer) {
        super(maxPlayer);
    }

    protected DefaultPlayerManager(Builder<?, ?> builder) {
        super(builder);
    }

    public static Builder<?, ?> builder() {
        return new Builder<>();
    }

    public static class Builder<U extends Player, B extends Builder<U, B>>
        extends AbstractPlayerManager.Builder<U, B> {

        @Override
        protected PlayerManager<U> newProduct() {
            return new DefaultPlayerManager<>(this);
        }
    }

}
