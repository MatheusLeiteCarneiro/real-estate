package com.mlcdev.realestate.controller;

import com.mlcdev.realestate.dto.*;
import com.mlcdev.realestate.security.JwtUtils;
import com.mlcdev.realestate.service.PropertyService;
import com.mlcdev.realestate.service.UserService;
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

@Tag(name = "Users", description = "User management endpoints (Admin only)")
@SecurityRequirement(name = "oauth2")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users")
public class UserController {

    private final PropertyService propertyService;
    private final UserService userService;

    @Operation(summary = "Find user by ID", description = "Requires ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{userId}")
    public ResponseEntity<UserDTO> findById(@PathVariable UUID userId) {
        UserDTO dto = userService.findById(userId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "List all users", description = "Requires ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(
            @ParameterObject @ModelAttribute UserFilter filter,
            @ParameterObject @PageableDefault(sort = {"active", "createdAt"}, direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        Page<UserDTO> dtoPage = userService.findAll(pageable, filter);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Create new user", description = "Requires ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody @Valid UserCreateDTO createDTO) {
        UserDTO dto = userService.create(createDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @Operation(summary = "Patch user", description = "Requires ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDTO> update(@RequestBody @Valid UserPatchDTO patchDTO, @PathVariable UUID userId) {
        UserDTO dto = userService.update(patchDTO, userId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Toggle property active status", description = "Requires ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/toggle-active")
    public ResponseEntity<UserDTO> toggleActive(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.toggleActive(userId));
    }


    @Operation(summary = "Find all properties by broker", description = "Requires ADMIN role")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{brokerId}/properties")
    public ResponseEntity<Page<PropertySummaryDTO>> findBrokerProperties(
            @ParameterObject @ModelAttribute PropertyFilter filter,
            @ParameterObject @PageableDefault(sort = {"available", "createdAt"}, direction = Sort.Direction.DESC, size = 20) Pageable pageable,
            @PathVariable UUID brokerId) {
        Page<PropertySummaryDTO> propertyPage = propertyService.findBrokerProperties(pageable, filter,brokerId);
        return ResponseEntity.ok(propertyPage);
    }

    @Operation(summary = "Find the authenticated user data", description = "Requires to be logged")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/me")
    public ResponseEntity<UserDTO> findMe(@AuthenticationPrincipal Jwt jwt) {
        UserDTO dto = userService.findById(JwtUtils.getUserId(jwt));
        return ResponseEntity.ok(dto);
    }
}
