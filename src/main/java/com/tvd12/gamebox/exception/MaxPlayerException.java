package com.tvd12.gamebox.exception;

public class MaxPlayerException extends RuntimeException {
    private static final long serialVersionUID = 556562233157419286L;

    public MaxPlayerException(String msg) {
        super(msg);
    }

    public MaxPlayerException(
        String player,
        int currentPlayerCount,
        int maxPlayerCount
    ) {
        this(
            "can not add new player: " + player +
            ", current player count is: " + currentPlayerCount +
            " when max player count is: " + maxPlayerCount
        );
    }

    public MaxPlayerException(
        int numberOfPlayer,
        int currentPlayerCount,
        int maxPlayerCount
    ) {
        this(
            "can not add " + numberOfPlayer + " new players" +
            ", current player count is: " + currentPlayerCount +
            " when max player count is: " + maxPlayerCount
        );
    }
}
