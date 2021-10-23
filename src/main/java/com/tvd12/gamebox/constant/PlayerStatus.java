package com.tvd12.gamebox.constant;

import lombok.Getter;

public enum PlayerStatus implements IPlayerStatus {

    NULL(0),
    VIEWING(1),
    PLAYING(2),
    SPEAKING(3);

    @Getter
    private int id;

    private PlayerStatus(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return toString();
    }

}
