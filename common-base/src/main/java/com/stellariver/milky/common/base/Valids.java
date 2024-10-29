package com.stellariver.milky.common.base;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
public @interface Valids {

    String message() default "${internalMessage}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}