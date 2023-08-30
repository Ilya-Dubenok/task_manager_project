package org.example.service.utils.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Target( { FIELD} )
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpecifiedScan {

    String[] fieldsToScan();

}
