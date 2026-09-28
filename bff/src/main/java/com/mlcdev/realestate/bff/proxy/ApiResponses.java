package com.mlcdev.realestate.bff.proxy;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static ResponseEntity<byte[]> forward(ResponseEntity<byte[]> upstream) {
        return forward(upstream, rewriteLocation(upstream.getHeaders().getLocation()));
    }

    public static ResponseEntity<byte[]> forward(ResponseEntity<byte[]> upstream, URI location) {
        MediaType contentType = upstream.getHeaders().getContentType();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(upstream.getStatusCode());

        if (contentType != null) {
            builder.contentType(contentType);
        }

        if (location != null) {
            builder.location(location);
        }

        return builder.body(upstream.getBody());
    }

    private static URI rewriteLocation(URI upstreamLocation) {
        if (upstreamLocation == null) {
            return null;
        }

        ServletUriComponentsBuilder currentRequest = ServletUriComponentsBuilder.fromCurrentRequest();
        String resourceId = lastPathSegment(upstreamLocation);

        if (isUuid(resourceId)) {
            return currentRequest.path("/{id}").buildAndExpand(resourceId).toUri();
        }

        return currentRequest.build().toUri();
    }

    private static String lastPathSegment(URI uri) {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}
