package org.example.core.dto.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Target( { FIELD} )
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = InIterableUUIDValidator.class)
public @interface NotNullUUIDInIterable {

    public String message() default "uuid is not specified";
    //represents group of constraints
    public Class<?>[] groups() default {};
    //represents additional information about annotation
    public Class<? extends Payload>[] payload() default {};

}
