package com.tvd12.gamebox.exception;

public class PlayerNotExistsException extends IllegalArgumentException {
    private static final long serialVersionUID = -3890769973167703303L;

    public PlayerNotExistsException(String message) {
        super(message);
    }
}
