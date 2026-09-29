package com.mlcdev.realestate.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "real-estate.oauth2.swagger")
public record SwaggerClientProperties(
        String clientId,
        String redirectUri
) {
}
