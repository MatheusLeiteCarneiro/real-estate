package com.mlcdev.realestate.bff.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponsesTest {

    @BeforeEach
    void setUpRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/properties");
        request.setServerName("localhost");
        request.setServerPort(8081);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void preservesStatusContentTypeAndBody() {
        byte[] body = "{\"id\":1}".getBytes();
        ResponseEntity<byte[]> upstream = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getBody()).isEqualTo(body);
    }

    @Test
    void omitsContentTypeWhenUpstreamHasNone() {
        ResponseEntity<byte[]> upstream = ResponseEntity.noContent().build();

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream);

        assertThat(result.getHeaders().getContentType()).isNull();
    }

    @Test
    void dropsTransportHeadersNotExplicitlyForwarded() {
        ResponseEntity<byte[]> upstream = ResponseEntity.ok()
                .header("Transfer-Encoding", "chunked")
                .header("X-Upstream-Internal", "secret")
                .body(new byte[0]);

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream);

        assertThat(result.getHeaders().get("Transfer-Encoding")).isNull();
        assertThat(result.getHeaders().get("X-Upstream-Internal")).isNull();
    }

    @Test
    void keepsNoLocationWhenUpstreamHasNone() {
        ResponseEntity<byte[]> upstream = ResponseEntity.ok().body(new byte[0]);

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream);

        assertThat(result.getHeaders().getLocation()).isNull();
    }

    @Test
    void rewritesLocationToBffUrlWhenUpstreamResourceIdIsUuid() {
        UUID id = UUID.randomUUID();
        ResponseEntity<byte[]> upstream = ResponseEntity.created(URI.create("http://api:8080/v1/properties/" + id))
                .body(new byte[0]);

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream);

        assertThat(result.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost:8081/api/properties/" + id));
    }

    @Test
    void rewritesLocationToCurrentBffUrlWhenUpstreamResourceIsNotAnId() {
        UUID propertyId = UUID.randomUUID();
        MockHttpServletRequest imageUploadRequest =
                new MockHttpServletRequest("POST", "/api/properties/" + propertyId + "/images");
        imageUploadRequest.setServerName("localhost");
        imageUploadRequest.setServerPort(8081);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(imageUploadRequest));

        ResponseEntity<byte[]> upstream = ResponseEntity
                .created(URI.create("http://api:8080/v1/properties/" + propertyId + "/images"))
                .body(new byte[0]);

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream);

        assertThat(result.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost:8081/api/properties/" + propertyId + "/images"));
    }

    @Test
    void twoArgOverloadUsesGivenLocationInsteadOfRewriting() {
        ResponseEntity<byte[]> upstream = ResponseEntity.created(URI.create("http://api:8080/v1/properties/whatever"))
                .body(new byte[0]);
        URI explicitLocation = URI.create("/api/other-resource");

        ResponseEntity<byte[]> result = ApiResponses.forward(upstream, explicitLocation);

        assertThat(result.getHeaders().getLocation()).isEqualTo(explicitLocation);
    }
}
