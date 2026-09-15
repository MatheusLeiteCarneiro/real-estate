package com.mlcdev.realestate.security;

import com.mlcdev.realestate.entities.Role;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final boolean active;
    @Builder.Default
    private final Set<Role> authorities = new HashSet<>();

    @Override
    public boolean isEnabled() {
        return active;
    }
}
