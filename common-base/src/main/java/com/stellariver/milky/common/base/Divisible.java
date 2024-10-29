package com.stellariver.milky.common.base;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * a number could be exact divide
 * null is valid
 * support integer long big decimal
 * @author houchuang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
public @interface Divisible {

    /**
     * divisor
     */
    int value();

    /**
     * delta default 1e-8
     */
    double delta() default 1e-8;

    String message() default "${dividend}不能被${divisor}整除";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
