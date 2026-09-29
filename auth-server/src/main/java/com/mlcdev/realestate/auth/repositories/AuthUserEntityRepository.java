package com.mlcdev.realestate.auth.repositories;

import com.mlcdev.realestate.auth.entities.AuthUserEntity;
import org.springframework.data.repository.Repository;


import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface AuthUserEntityRepository extends Repository<AuthUserEntity, UUID> {

    Optional<AuthUserEntity> findByUsername(String username);
}
