package com.mlcdev.realestate.bff.proxy;

import com.mlcdev.realestate.bff.util.RedisIntegrationTest;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real proxy path (controller + security + ApiResponses/ApiErrorHandler)
 * against a plain JDK HttpServer standing in for the API, instead of the real thing.
 */
@RedisIntegrationTest
class PropertyProxyIT {

    private static final String UPSTREAM_BODY = "{\"id\":\"upstream-property\"}";
    private static final AtomicReference<String> capturedAuthorizationHeader = new AtomicReference<>();
    private static final HttpServer FAKE_API = startFakeApi();

    @Autowired
    private MockMvc mockMvc;

    private static HttpServer startFakeApi() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                capturedAuthorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));

                byte[] body = UPSTREAM_BODY.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                exchange.getResponseHeaders().add("X-Upstream-Internal", "should-not-leak");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void apiUrl(DynamicPropertyRegistry registry) {
        registry.add("real-estate.bff.api-url", () -> "http://localhost:" + FAKE_API.getAddress().getPort());
    }

    @BeforeEach
    void resetCapture() {
        capturedAuthorizationHeader.set(null);
    }

    @Test
    void anonymousRequestReachesUpstreamWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/properties/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(UPSTREAM_BODY));

        assertThat(capturedAuthorizationHeader.get()).isNull();
    }

    @Test
    void authenticatedRequestAttachesBearerToken() throws Exception {
        mockMvc.perform(get("/api/properties/" + UUID.randomUUID())
                        .with(oidcLogin())
                        .with(oauth2Client("real-estate-bff")))
                .andExpect(status().isOk())
                .andExpect(content().json(UPSTREAM_BODY));

        assertThat(capturedAuthorizationHeader.get()).startsWith("Bearer ");
    }

    @Test
    void upstreamTransportHeadersAreNotLeakedToTheClient() throws Exception {
        mockMvc.perform(get("/api/properties/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Upstream-Internal"));
    }

    @Test
    void anonymousRequestToAProtectedRouteIsRejected() throws Exception {
        mockMvc.perform(get("/api/properties/all"))
                .andExpect(status().isUnauthorized());
    }
}
