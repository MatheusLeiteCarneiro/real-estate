package com.mlcdev.realestate.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    @DisplayName("Should accept null or blank password")
    void shouldAcceptNullOrBlankPassword() {
        Assertions.assertTrue(validator.isValid(null, null));
        Assertions.assertTrue(validator.isValid("", null));
        Assertions.assertTrue(validator.isValid("   ", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password1!", "Str0ng@Pass", "Valid$123"})
    @DisplayName("Should accept strong password")
    void shouldAcceptStrongPassword(String password) {
        Assertions.assertTrue(validator.isValid(password, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Short1!",
            "password1!",
            "PASSWORD1!",
            "Password!",
            "Password1",
            "Password1#"
    })
    @DisplayName("Should reject weak password")
    void shouldRejectWeakPassword(String password) {
        Assertions.assertFalse(validator.isValid(password, null));
    }
}
