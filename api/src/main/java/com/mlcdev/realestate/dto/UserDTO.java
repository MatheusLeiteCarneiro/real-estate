package com.mlcdev.realestate.dto;

import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private UUID id;

    private String username;

    @Builder.Default
    private Set<String> authorities = new HashSet<>();

    private Instant createdAt;

    private boolean active;
}
