package com.mlcdev.realestate.exception.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiError {


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private final Instant timestamp;
    private final Integer status;
    private final String error;
    private final String path;


    public ApiError(Integer status, String error, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.path = path;
    }
}
