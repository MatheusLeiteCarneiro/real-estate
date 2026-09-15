package com.mlcdev.realestate.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2AuthorizationCleanupService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void invalidateUserTokens(String username){
        jdbcTemplate.update(
                "DELETE FROM oauth2_authorization WHERE principal_name = ?", username
        );
    }
}
