package com.mlcdev.realestate.entities;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_BROKER;

    @Override
    public String getAuthority() {
        return name();
    }
}
