package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.gamebox.constant.IPlayerRole;
import com.tvd12.gamebox.constant.IPlayerStatus;
import com.tvd12.gamebox.constant.PlayerRole;
import com.tvd12.gamebox.constant.PlayerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {

    protected final String name;
    @Setter
    protected IPlayerRole role = PlayerRole.NULL;
    @Setter
    protected IPlayerStatus status = PlayerStatus.NULL;
    @Setter
    protected long currentRoomId;

    public Player(String name) {
        this.name = name;
    }

    protected Player(Builder<?> builder) {
        this(builder.name);
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return name.equals(((Player) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public static class Builder<B extends Builder<B>> implements EzyBuilder<Player> {

        protected String name;

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        @Override
        public Player build() {
            preBuild();
            return newProduct();
        }

        protected void preBuild() {
        }

        protected Player newProduct() {
            return new Player(this);
        }

    }

}
