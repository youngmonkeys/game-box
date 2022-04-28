package com.tvd12.gamebox.constant;

import com.tvd12.ezyfox.util.EzyEnums;
import lombok.Getter;

import java.util.Map;

public enum PlayerStatus implements IPlayerStatus {

    NULL(0),
    VIEWING(1),
    PLAYING(2),
    SPEAKING(3);

    private static final Map<Integer, PlayerStatus> STATUS_BY_ID =
        EzyEnums.enumMapInt(PlayerStatus.class);
    @Getter
    private int id;

    private PlayerStatus(int id) {
        this.id = id;
    }

    public static PlayerStatus valueOf(int id) {
        return STATUS_BY_ID.getOrDefault(Integer.valueOf(id), NULL);
    }

    @Override
    public String getName() {
        return toString();
    }
}
