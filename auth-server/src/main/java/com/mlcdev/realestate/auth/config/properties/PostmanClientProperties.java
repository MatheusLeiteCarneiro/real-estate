package com.mlcdev.realestate.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "real-estate.oauth2.postman")
public record PostmanClientProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
