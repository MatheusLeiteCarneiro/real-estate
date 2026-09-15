package com.mlcdev.realestate.security;

import com.mlcdev.realestate.entities.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

@Slf4j
public class OwnershipValidator {

    private OwnershipValidator() {
    }

    public static void propertyVerifyBrokerPermission(Property property, UUID brokerId, boolean isAdmin) {
        if (!isAdmin && !property.getBroker().getId().equals(brokerId)) {
            log.warn("User with ID: {} doesn't have the permission to modify the property with ID: {}", brokerId, property.getId());
            throw new AccessDeniedException("User doesn't have the permission to modify the property");
        }
    }


}
