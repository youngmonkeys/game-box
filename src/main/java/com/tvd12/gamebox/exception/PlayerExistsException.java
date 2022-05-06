package com.tvd12.gamebox.exception;

public class PlayerExistsException extends IllegalArgumentException {
    private static final long serialVersionUID = -3890769973167703303L;

    public PlayerExistsException(String username) {
        super("player: " + username + " has added");
    }
}
