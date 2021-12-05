package com.tvd12.gamebox.exception;

public class MaxRoomException extends RuntimeException {
    private static final long serialVersionUID = 556562233157419286L;

    public MaxRoomException(String msg) {
        super(msg);
    }

    public MaxRoomException(String room, int currentRoomCount, int maxRoomCount) {
        this(new StringBuilder()
                     .append("can not add new room: ").append(room)
                     .append(", current room count is: ").append(currentRoomCount)
                     .append(" when max room count is: ").append(maxRoomCount)
                     .toString());
    }

    public MaxRoomException(int numberOfRoom, int currentRoomCount, int maxRoomCount) {
        this(new StringBuilder()
                     .append("can not add ").append(numberOfRoom).append(" new rooms")
                     .append(", current room count is: ").append(currentRoomCount)
                     .append(" when max room count is: ").append(maxRoomCount)
                     .toString());
    }

}
