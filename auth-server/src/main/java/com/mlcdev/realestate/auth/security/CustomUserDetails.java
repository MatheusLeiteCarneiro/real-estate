package com.mlcdev.realestate.auth.security;

import com.mlcdev.realestate.auth.entities.AuthUserEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private Boolean active;
    private Set<GrantedAuthority> authorities = new HashSet<>();

    public CustomUserDetails(AuthUserEntity authUserEntity) {
        id = authUserEntity.getId();
        username = authUserEntity.getUsername();
        password = authUserEntity.getPassword();
        active = authUserEntity.getActive();
        authorities = authUserEntity.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public UUID getId() {
        return id;
    }
}
