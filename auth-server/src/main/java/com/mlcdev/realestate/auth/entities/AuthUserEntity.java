package com.mlcdev.realestate.auth.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_user")
public class AuthUserEntity {

    @Id
    private UUID id;
    private String username;
    private String password;
    private Boolean active;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    private Set<String> authorities = new HashSet<>();

    public AuthUserEntity() {
    }

    public AuthUserEntity(UUID id, String username, String password, Boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = active;
    }

    public AuthUserEntity(UUID id, String username, String password, Boolean active, Set<String> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = active;
        this.authorities = authorities;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}
