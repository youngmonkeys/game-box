package com.tvd12.gamebox.constant;

import java.util.Map;

import com.tvd12.ezyfox.util.EzyEnums;

import lombok.Getter;

public enum PlayerStatus implements IPlayerStatus {

    NULL(0),
    VIEWING(1),
    PLAYING(2),
    SPEAKING(3);

    @Getter
    private int id;
    
    private static final Map<Integer, PlayerStatus> STATUS_BY_ID =
        EzyEnums.enumMapInt(PlayerStatus.class);

    private PlayerStatus(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return toString();
    }
    
    public static PlayerStatus valueOf(int id) {
        return STATUS_BY_ID.getOrDefault(Integer.valueOf(id), NULL);
    }
}
