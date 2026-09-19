package com.mlcdev.realestate.api.entities;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_BROKER;

    @Override
    public String getAuthority() {
        return name();
    }
}
