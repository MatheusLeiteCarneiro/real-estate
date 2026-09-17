package com.mlcdev.realestate.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "real-estate.oauth2.bff")
public record BffClientProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String postLogoutRedirectUri
) {
}
