package org.example.core.dto.user;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomEmailValidator implements ConstraintValidator<StrongEmailValidation, String> {



    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.endsWith("@gmail.com");

    }
}
