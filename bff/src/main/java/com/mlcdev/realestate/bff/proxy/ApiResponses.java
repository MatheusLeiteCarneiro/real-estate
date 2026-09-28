package com.mlcdev.realestate.bff.proxy;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static ResponseEntity<byte[]> forward(ResponseEntity<byte[]> upstream) {
        MediaType contentType = upstream.getHeaders().getContentType();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(upstream.getStatusCode());

        if (contentType != null) {
            builder.contentType(contentType);
        }

        return builder.body(upstream.getBody());
    }
}
