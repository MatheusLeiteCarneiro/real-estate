package com.mlcdev.realestate.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@Entity
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "authority")
    @Builder.Default
    @BatchSize(size = 50)
    private Set<Role> authorities = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public void changeUsername(String newUsername) {
        this.username = newUsername;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public void addRole(Role role) {
        this.authorities.add(role);
    }
}
