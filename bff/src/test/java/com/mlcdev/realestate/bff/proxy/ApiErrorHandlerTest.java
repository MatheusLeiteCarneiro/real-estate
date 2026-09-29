package com.mlcdev.realestate.bff.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorHandlerTest {

    private final ApiErrorHandler handler = new ApiErrorHandler();

    @Test
    void mirrorsStatusContentTypeAndBodyFromApiError() {
        byte[] body = "{\"error\":\"Property not found\"}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", upstreamHeaders, body, StandardCharsets.UTF_8);

        ResponseEntity<byte[]> result = handler.handleApiError(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(body);
    }

    @Test
    void mirrorsServerErrorStatusAndBody() {
        byte[] body = "{\"error\":\"Unexpected failure\"}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpServerErrorException ex = HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", upstreamHeaders, body, StandardCharsets.UTF_8);

        ResponseEntity<byte[]> result = handler.handleApiError(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isEqualTo(body);
    }

    @Test
    void omitsContentTypeWhenApiErrorHasNone() {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], null);

        ResponseEntity<byte[]> result = handler.handleApiError(ex);

        assertThat(result.getHeaders().getContentType()).isNull();
    }

    @Test
    void returnsEmptyUnauthorizedWhenAuthorizationIsMissing() {
        ResponseEntity<Void> result = handler.handleMissingAuthorization();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody()).isNull();
    }
}
