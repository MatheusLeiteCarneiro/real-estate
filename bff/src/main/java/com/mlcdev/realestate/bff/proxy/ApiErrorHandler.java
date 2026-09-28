package com.mlcdev.realestate.bff.proxy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<byte[]> handleApiError(RestClientResponseException ex) {
        HttpHeaders responseHeaders = ex.getResponseHeaders();
        MediaType contentType = responseHeaders != null ? responseHeaders.getContentType() : null;
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(ex.getStatusCode());

        if (contentType != null) {
            builder.contentType(contentType);
        }

        return builder.body(ex.getResponseBodyAsByteArray());
    }

    @ExceptionHandler(ClientAuthorizationRequiredException.class)
    public ResponseEntity<Void> handleMissingAuthorization(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
