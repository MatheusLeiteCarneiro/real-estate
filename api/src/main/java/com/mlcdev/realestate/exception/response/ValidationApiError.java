package com.mlcdev.realestate.exception.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationApiError extends ApiError {

    private final List<FieldError> errors = new ArrayList<>();

    public ValidationApiError(Integer status, String error, String path) {
        super(status, error, path);
    }

    public void addError(String fieldName, String message) {
        errors.add(new FieldError(fieldName, message));
    }

    public record FieldError(String fieldName, String message) {
    }
}
