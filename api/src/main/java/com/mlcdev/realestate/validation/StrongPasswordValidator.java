package com.mlcdev.realestate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final String UPPERCASE = ".*[A-Z].*";
    private static final String LOWERCASE = ".*[a-z].*";
    private static final String DIGIT = ".*\\d.*";
    private static final String SPECIAL = ".*[@$!%*?&].*";


    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.length() >= MIN_LENGTH &&
                value.matches(UPPERCASE) &&
                value.matches(LOWERCASE) &&
                value.matches(DIGIT) &&
                value.matches(SPECIAL);

    }
}
