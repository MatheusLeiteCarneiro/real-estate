package com.mlcdev.realestate.security;

import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

class OwnershipValidatorTest {

    @Test
    @DisplayName("Should allow broker owner")
    void propertyVerifyBrokerPermissionShouldAllowBrokerOwner() {
        UUID brokerId = UUID.randomUUID();
        Property property = buildPropertyWithBroker(brokerId);

        Assertions.assertDoesNotThrow(() ->
                OwnershipValidator.propertyVerifyBrokerPermission(property, brokerId, false)
        );
    }

    @Test
    @DisplayName("Should allow admin even when not owner")
    void propertyVerifyBrokerPermissionShouldAllowAdminEvenWhenNotOwner() {
        UUID ownerBrokerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Property property = buildPropertyWithBroker(ownerBrokerId);

        Assertions.assertDoesNotThrow(() ->
                OwnershipValidator.propertyVerifyBrokerPermission(property, adminId, true)
        );
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when broker is not owner")
    void propertyVerifyBrokerPermissionShouldThrowAccessDeniedExceptionWhenBrokerIsNotOwner() {
        UUID ownerBrokerId = UUID.randomUUID();
        UUID anotherBrokerId = UUID.randomUUID();
        Property property = buildPropertyWithBroker(ownerBrokerId);

        AccessDeniedException exception = Assertions.assertThrows(
                AccessDeniedException.class,
                () -> OwnershipValidator.propertyVerifyBrokerPermission(property, anotherBrokerId, false)
        );

        Assertions.assertEquals("User doesn't have the permission to modify the property", exception.getMessage());
    }

    private Property buildPropertyWithBroker(UUID brokerId) {
        User broker = User.builder()
                .id(brokerId)
                .username("broker")
                .build();

        return Property.builder()
                .id(UUID.randomUUID())
                .broker(broker)
                .build();
    }
}
