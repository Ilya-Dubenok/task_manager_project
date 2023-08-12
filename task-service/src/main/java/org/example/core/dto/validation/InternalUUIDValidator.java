package org.example.core.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InternalUUIDValidator implements ConstraintValidator<NotNullInternalUUID, Uidable> {
    @Override
    public boolean isValid(Uidable value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        return value.getUuid() != null;
    }
}
