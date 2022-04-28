package com.tvd12.gamebox.constant;

import com.tvd12.ezyfox.util.EzyEnums;
import lombok.Getter;

import java.util.Map;

public enum RoomStatus implements IRoomStatus {

    WAITING(1),
    STARTABLE(2),
    PLAYING(3),
    FINISHING(4),
    FINISHED(5);

    private static final Map<Integer, RoomStatus> STATUS_BY_ID =
        EzyEnums.enumMapInt(RoomStatus.class);
    @Getter
    private int id;

    private RoomStatus(int id) {
        this.id = id;
    }

    public static RoomStatus valueOf(int id) {
        return STATUS_BY_ID.get(Integer.valueOf(id));
    }

    @Override
    public String getName() {
        return toString();
    }
}
