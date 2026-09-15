package com.mlcdev.realestate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default
            "password must be at least 8 characters and contain uppercase, lowercase, number and special character (@$!%*?&)";


    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}