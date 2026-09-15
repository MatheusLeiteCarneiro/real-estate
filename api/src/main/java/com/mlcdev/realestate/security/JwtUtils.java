package com.mlcdev.realestate.security;

import com.mlcdev.realestate.entities.Role;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

public class JwtUtils {

    private JwtUtils() {
    }

    public static UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public static boolean isAdmin(Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null && authorities.contains(Role.ROLE_ADMIN.name());
    }

    public static boolean isBroker(Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null && authorities.contains(Role.ROLE_BROKER.name());
    }

    public static boolean isBrokerOrAdmin(Jwt jwt){
        return jwt != null && (JwtUtils.isAdmin(jwt) || JwtUtils.isBroker(jwt));
    }
}
