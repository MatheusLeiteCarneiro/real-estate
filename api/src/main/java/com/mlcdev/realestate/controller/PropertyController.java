package com.mlcdev.realestate.controller;


import com.mlcdev.realestate.dto.*;
import com.mlcdev.realestate.security.JwtUtils;
import com.mlcdev.realestate.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Properties", description = "Property management endpoints")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;


    @Operation(summary = "List all available properties", description = "Public endpoint - no authentication required")
    @GetMapping
    public ResponseEntity<Page<PropertySummaryDTO>> findAvailableProperties(
            @ParameterObject @ModelAttribute PropertyFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PropertySummaryDTO> propertyPage = propertyService.findAllAvailable(pageable, filter);
        return ResponseEntity.ok(propertyPage);
    }

    @Operation(summary = "Find property by ID", description = "Public for available properties. BROKER or ADMIN authentication is required for unavailable properties.")
    @GetMapping(value = "/{id}")
    public ResponseEntity<PropertyDetailDTO> findPropertyById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        PropertyDetailDTO propertyDTO = propertyService.findById(id, JwtUtils.isBrokerOrAdmin(jwt));
        return ResponseEntity.ok(propertyDTO);
    }

    @Operation(summary = "Create new Property", description = "Requires BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @PostMapping
    public ResponseEntity<PropertyDetailDTO> createProperty(@Valid @RequestBody PropertyCreateDTO dto, @AuthenticationPrincipal Jwt jwt) {
        PropertyDetailDTO createdDTO = propertyService.create(dto, JwtUtils.getUserId(jwt));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(createdDTO);
    }

    @Operation(summary = "Patch a Property", description = "Requires the property BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @PatchMapping(value = "/{id}")
    public ResponseEntity<PropertyDetailDTO> patchProperty(@Valid @RequestBody PropertyPatchDTO dto, @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        PropertyDetailDTO responseDto = propertyService.update(id, dto, JwtUtils.getUserId(jwt), JwtUtils.isAdmin(jwt));
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Toggle property available status", description = "Requires the property BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PropertyDetailDTO> toggleAvailable(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(propertyService.toggleAvailable(id, JwtUtils.getUserId(jwt), JwtUtils.isAdmin(jwt)));
    }

    @Operation(summary = "Delete a Property", description = "Requires the property BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        propertyService.delete(id, JwtUtils.getUserId(jwt), JwtUtils.isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "List all properties - including those that are unavailable", description = "Requires BROKER or ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('BROKER')")
    @GetMapping("/all")
    public ResponseEntity<Page<PropertySummaryDTO>> findAllProperties(
            @ParameterObject @ModelAttribute PropertyFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PropertySummaryDTO> propertyPage = propertyService.findAll(pageable, filter);
        return ResponseEntity.ok(propertyPage);
    }

}
