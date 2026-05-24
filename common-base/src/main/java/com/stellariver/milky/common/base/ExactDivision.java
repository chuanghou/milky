package com.stellariver.milky.common.base;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code null} elements is considered valid.
 *
 * @author houchuang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ExactDivision {

    String value() default "100";

    String message() default "${validatedValue}不能被{value}整除";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
