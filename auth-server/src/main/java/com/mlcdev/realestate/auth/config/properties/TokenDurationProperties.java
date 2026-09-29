package com.mlcdev.realestate.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt.token-duration")
public record TokenDurationProperties(
        Integer refresh,
        Integer access
) {
}
