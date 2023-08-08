package org.example.core.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class InIterableUUIDValidator implements ConstraintValidator<NotNullUUIDInIterable, Iterable<? extends Uidable>> {

    @Override
    public boolean isValid(Iterable<? extends Uidable> value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }

        for (Uidable uidable : value) {
            if (null == uidable.getUuid()) {

                return false;

            }
        }

        return true;
    }
}
