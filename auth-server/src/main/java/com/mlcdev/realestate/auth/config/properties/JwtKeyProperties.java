package com.mlcdev.realestate.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtKeyProperties(
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey
) {
}
