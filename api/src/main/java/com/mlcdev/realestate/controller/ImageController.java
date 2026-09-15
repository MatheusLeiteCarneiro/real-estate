package com.mlcdev.realestate.controller;

import com.mlcdev.realestate.dto.ImageDTO;
import com.mlcdev.realestate.security.JwtUtils;
import com.mlcdev.realestate.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Images", description = "Property image management endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/properties/{propertyId}/images")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "List all images for a property", description = "Public for available properties. BROKER or ADMIN authentication is required for unavailable properties.")
    @GetMapping
    public ResponseEntity<List<ImageDTO>> findAllImages(@PathVariable UUID propertyId, @AuthenticationPrincipal Jwt jwt) {
        List<ImageDTO> images = imageService.findAllImages(propertyId, JwtUtils.isBrokerOrAdmin(jwt));
        return ResponseEntity.ok(images);
    }

    @Operation(summary = "Find primary image for a property", description = "Public for available properties. BROKER or ADMIN authentication is required for unavailable properties.")
    @GetMapping(value = "/primary")
    public ResponseEntity<ImageDTO> findPrimaryImage(@PathVariable UUID propertyId,  @AuthenticationPrincipal Jwt jwt) {
        ImageDTO dto = imageService.findPrimaryImage(propertyId, JwtUtils.isBrokerOrAdmin(jwt));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Post images for a property", description = "Requires the property BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageDTO>> postImages(@PathVariable UUID propertyId,
                                                     @RequestPart("files")
                                                     List<MultipartFile> files,
                                                     @AuthenticationPrincipal Jwt jwt) {
        List<ImageDTO> imageDTOList = imageService.saveImages(propertyId, files, JwtUtils.getUserId(jwt), JwtUtils.isAdmin(jwt));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(propertyId).toUri();
        return ResponseEntity.created(uri).body(imageDTOList);
    }

    @Operation(summary = "Set primary image for a Property", description = "Requires the property BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @PatchMapping(value = "/{imageId}/primary")
    public ResponseEntity<ImageDTO> updatePrimaryImage(@PathVariable UUID propertyId, @PathVariable UUID imageId, @AuthenticationPrincipal Jwt jwt) {
        ImageDTO imageDTO = imageService.updateImageAsPrimary(propertyId, imageId, JwtUtils.getUserId(jwt), JwtUtils.isAdmin(jwt));
        return ResponseEntity.ok(imageDTO);
    }

    @Operation(summary = "Delete an image for a Property", description = "Requires the property BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @DeleteMapping(value = "/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID propertyId, @PathVariable UUID imageId, @AuthenticationPrincipal Jwt jwt) {
        imageService.deleteImage(propertyId, imageId, JwtUtils.getUserId(jwt), JwtUtils.isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

}
