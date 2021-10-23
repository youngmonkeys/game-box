package com.tvd12.gamebox.exception;

public class MaxPlayerException extends RuntimeException {
    private static final long serialVersionUID = 556562233157419286L;

    public MaxPlayerException(String msg) {
        super(msg);
    }

    public MaxPlayerException(String player, int currentPlayerCount, int maxPlayerCount) {
        this(new StringBuilder()
                     .append("can not add new player: ").append(player)
                     .append(", current player count is: ").append(currentPlayerCount)
                     .append(" when max player count is: ").append(maxPlayerCount)
                     .toString());
    }

    public MaxPlayerException(int numberOfPlayer, int currentPlayerCount, int maxPlayerCount) {
        this(new StringBuilder()
                     .append("can not add ").append(numberOfPlayer).append(" new players")
                     .append(", current player count is: ").append(currentPlayerCount)
                     .append(" when max player count is: ").append(maxPlayerCount)
                     .toString());
    }

}
