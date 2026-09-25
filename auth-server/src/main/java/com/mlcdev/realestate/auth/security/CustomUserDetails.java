package com.mlcdev.realestate.auth.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mlcdev.realestate.auth.entities.AuthUserEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final Boolean active;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(AuthUserEntity authUserEntity) {
        id = authUserEntity.getId();
        username = authUserEntity.getUsername();
        password = authUserEntity.getPassword();
        active = authUserEntity.getActive();
        authorities = authUserEntity.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @JsonCreator
    public CustomUserDetails(
            @JsonProperty("id") UUID id,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("authorities") Set<GrantedAuthority> authorities
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = enabled;
        this.authorities = authorities != null
                ? new HashSet<>(authorities)
                : new HashSet<>();
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
        return Boolean.TRUE.equals(active);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
