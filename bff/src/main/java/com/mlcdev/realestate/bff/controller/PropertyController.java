package com.mlcdev.realestate.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private static final String PROPERTIES_BASE_URL = "/v1/properties";
    private static final String ID_PATH = PROPERTIES_BASE_URL + "/{id}";

    private final RestClient publicRestClient;
    private final RestClient authRestClient;

    public PropertyController(@Qualifier("publicRestClient") RestClient publicRestClient,
                              @Qualifier("authRestClient") RestClient authRestClient) {
        this.publicRestClient = publicRestClient;
        this.authRestClient = authRestClient;
    }

    @GetMapping
    public ResponseEntity<byte[]> findAvailableProperties(@RequestParam Map<String, String> params) {
        return forward(publicRestClient.get().uri(uriBuilder -> {
            params.forEach(uriBuilder::queryParam);
            return uriBuilder.path(PROPERTIES_BASE_URL).build();
        }).retrieve().toEntity(byte[].class));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findPropertyById(@PathVariable UUID id, @AuthenticationPrincipal OidcUser user) {
        RestClient restClient = user == null ? publicRestClient : authRestClient;
        return forward(restClient.get().uri(ID_PATH, id).retrieve().toEntity(byte[].class));
    }

    @GetMapping("/all")
    public ResponseEntity<byte[]> findAllProperties(@RequestParam Map<String, String> params) {
        return forward(authRestClient.get().uri(uriBuilder -> {
            params.forEach(uriBuilder::queryParam);
            return uriBuilder.path(PROPERTIES_BASE_URL + "/all").build();
        }).retrieve().toEntity(byte[].class));
    }

    @PostMapping
    public ResponseEntity<byte[]> createProperty(@RequestBody Map<String, Object> body) {
        return forward(authRestClient.post().uri(PROPERTIES_BASE_URL).body(body).retrieve().toEntity(byte[].class));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<byte[]> updateProperty(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return forward(authRestClient.patch().uri(ID_PATH, id).body(body).retrieve().toEntity(byte[].class));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<byte[]> toggleAvailable(@PathVariable UUID id) {
        return forward(authRestClient.patch().uri(ID_PATH + "/toggle-active", id).retrieve().toEntity(byte[].class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<byte[]> deleteProperty(@PathVariable UUID id) {
        return forward(authRestClient.delete().uri(ID_PATH, id).retrieve().toEntity(byte[].class));
    }

    private ResponseEntity<byte[]> forward(ResponseEntity<byte[]> upstream) {
        MediaType contentType = upstream.getHeaders().getContentType();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(upstream.getStatusCode());

        if (contentType != null) {
            builder.contentType(contentType);
        }

        return builder.body(upstream.getBody());
    }
}
