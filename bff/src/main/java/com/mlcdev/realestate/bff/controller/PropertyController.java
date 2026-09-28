package com.mlcdev.realestate.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final RestClient publicRestClient;
    private final RestClient authRestClient;

    public PropertyController(@Qualifier("publicRestClient") RestClient publicRestClient,
                              @Qualifier("authRestClient") RestClient authRestClient) {
        this.publicRestClient = publicRestClient;
        this.authRestClient = authRestClient;
    }

    @GetMapping
    public ResponseEntity<byte[]> findAvailableProperties(@RequestParam Map<String, String> params){
        return publicRestClient.get().uri(uriBuilder -> {
            params.forEach(uriBuilder::queryParam);
            return uriBuilder.path("/v1/properties").build();
        }).retrieve().toEntity(byte[].class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findPropertyById(@PathVariable UUID id, @AuthenticationPrincipal OidcUser user){
        RestClient restClient = user == null ? publicRestClient : authRestClient;
        return restClient.get().uri("/v1/properties/{id}", id).retrieve().toEntity(byte[].class);
    }

}
