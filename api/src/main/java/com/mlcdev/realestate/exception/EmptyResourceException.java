package com.mlcdev.realestate.exception;

public class EmptyResourceException extends RuntimeException {
    public EmptyResourceException(String message) {
        super(message);
    }

    public EmptyResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
