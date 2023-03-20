package com.stellariver.milky.financial.base;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author houchuang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Constraint(validatedBy = ShareValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Share{

    long value() default 100L;

    String message() default "${validatedValue}不能被{value}整除";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
