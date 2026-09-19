package com.mlcdev.realestate.api.exception;

public class EmptyResourceException extends RuntimeException {
    public EmptyResourceException(String message) {
        super(message);
    }

    public EmptyResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
