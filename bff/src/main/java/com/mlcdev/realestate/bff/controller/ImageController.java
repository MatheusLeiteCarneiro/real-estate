package com.mlcdev.realestate.bff.controller;


import com.mlcdev.realestate.bff.proxy.ApiResponses;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties/{propertyId}/images")
public class ImageController {

    private static final String IMAGES_BASE_URL = "/v1/properties/{propertyId}/images";

    private final RestClient publicRestClient;
    private final RestClient authRestClient;

    public ImageController(@Qualifier("publicRestClient") RestClient publicRestClient,
                              @Qualifier("authRestClient") RestClient authRestClient) {
        this.publicRestClient = publicRestClient;
        this.authRestClient = authRestClient;
    }

    @GetMapping
    public ResponseEntity<byte[]> findAllImages(@PathVariable UUID propertyId,  @AuthenticationPrincipal OidcUser user){
        RestClient restClient = user == null ? publicRestClient : authRestClient;
        return ApiResponses.forward(restClient.get()
                .uri(IMAGES_BASE_URL, propertyId)
                .retrieve().toEntity(byte[].class));
    }

    @GetMapping("/primary")
    public ResponseEntity<byte[]> findPrimary(@PathVariable UUID propertyId,  @AuthenticationPrincipal OidcUser user){
        RestClient restClient = user == null ? publicRestClient : authRestClient;
        return ApiResponses.forward(restClient.get()
                .uri(IMAGES_BASE_URL + "/primary", propertyId)
                .retrieve().toEntity(byte[].class));
    }

    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<byte[]> updatePrimaryImage(@PathVariable UUID propertyId, @PathVariable UUID imageId){
        return ApiResponses.forward(authRestClient.patch()
                .uri(IMAGES_BASE_URL + "/{imageId}/primary", propertyId, imageId)
                .retrieve().toEntity(byte[].class));
    }

    @DeleteMapping(value = "/{imageId}")
    public ResponseEntity<byte[]> deleteImage(@PathVariable UUID propertyId, @PathVariable UUID imageId){
        return ApiResponses.forward(authRestClient.delete()
                .uri(IMAGES_BASE_URL + "/{imageId}", propertyId, imageId)
                .retrieve().toEntity(byte[].class));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> postImages(@PathVariable UUID propertyId,
                                             @RequestPart("files") List<MultipartFile> files){
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            HttpHeaders partHeaders = new HttpHeaders();
            String contentType = file.getContentType();

            if (contentType != null) {
                partHeaders.setContentType(MediaType.parseMediaType(contentType));
            }

            multipartBody.add("files", new HttpEntity<>(file.getResource(), partHeaders));
        }
        return ApiResponses.forward(authRestClient.post()
                .uri(IMAGES_BASE_URL, propertyId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipartBody)
                .retrieve()
                .toEntity(byte[].class)
        );
    }


}
