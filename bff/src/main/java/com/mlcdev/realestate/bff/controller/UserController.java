package com.mlcdev.realestate.bff.controller;

import com.mlcdev.realestate.bff.proxy.ApiResponses;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/users")
public class UserController {

    private static final String USER_BASE_URL = "/v1/users";
    private static final String ID_PATH = USER_BASE_URL + "/{userId}";


    private final RestClient authRestClient;

    public UserController(@Qualifier("authRestClient") RestClient authRestClient) {
        this.authRestClient = authRestClient;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<byte[]> findById(@PathVariable UUID userId) {
        return ApiResponses.forward(authRestClient.get()
                .uri(ID_PATH, userId)
                .retrieve().toEntity(byte[].class)
        );
    }

    @GetMapping
    public ResponseEntity<byte[]> findAll(@RequestParam Map<String, String> params) {
        return ApiResponses.forward(authRestClient.get().uri(uriBuilder -> {
            params.forEach(uriBuilder::queryParam);
            return uriBuilder.path(USER_BASE_URL).build();
        }).retrieve().toEntity(byte[].class));
    }

    @PostMapping
    public ResponseEntity<byte[]> create(@RequestBody Map<String, Object> body) {
        return ApiResponses.forward(authRestClient.post()
                .uri(USER_BASE_URL).body(body)
                .retrieve().toEntity(byte[].class));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<byte[]> update(@PathVariable UUID userId, @RequestBody Map<String, Object> body) {
        return ApiResponses.forward(authRestClient.patch()
                .uri(ID_PATH , userId).body(body)
                .retrieve().toEntity(byte[].class));
    }

    @PatchMapping("/{userId}/toggle-active")
    public ResponseEntity<byte[]> toggleActive(@PathVariable UUID userId){
        return ApiResponses.forward(authRestClient.patch()
                .uri(ID_PATH + "/toggle-active", userId)
                .retrieve().toEntity(byte[].class));
    }

    @GetMapping("/me")
    public ResponseEntity<byte[]> me(){
        return ApiResponses.forward(authRestClient.get()
                .uri(USER_BASE_URL + "/me")
                .retrieve().toEntity(byte[].class));
    }

    @GetMapping("/{brokerId}/properties")
    public ResponseEntity<byte[]> findBrokerProperties(@PathVariable UUID brokerId, @RequestParam Map<String, String> params) {
        return ApiResponses.forward(authRestClient.get().uri(uriBuilder -> {
            params.forEach(uriBuilder::queryParam);
            return uriBuilder.path(USER_BASE_URL + "/{brokerId}/properties").build(brokerId);
        }).retrieve().toEntity(byte[].class));
    }
}