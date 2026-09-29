package com.mlcdev.realestate.api.exception;

public class ResourceMismatchException extends RuntimeException {
    public ResourceMismatchException(String message) {
        super(message);
    }

    public ResourceMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

}
