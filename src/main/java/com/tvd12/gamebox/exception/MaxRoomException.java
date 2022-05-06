package com.tvd12.gamebox.exception;

public class MaxRoomException extends RuntimeException {
    private static final long serialVersionUID = 556562233157419286L;

    public MaxRoomException(String msg) {
        super(msg);
    }

    public MaxRoomException(
        String room,
        int currentRoomCount,
        int maxRoomCount
    ) {
        this(
            "can not add new room: " + room +
            ", current room count is: " + currentRoomCount +
            " when max room count is: " + maxRoomCount
        );
    }

    public MaxRoomException(
        int numberOfRoom,
        int currentRoomCount,
        int maxRoomCount
    ) {
        this(
            "can not add " + numberOfRoom + " new rooms" +
            ", current room count is: " + currentRoomCount +
            " when max room count is: " + maxRoomCount
        );
    }
}
