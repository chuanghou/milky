package com.stellariver.milky.financial.base;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * {@code null} elements is considered valid.
 * @author houchuang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Constraint(validatedBy = ExactDivisionValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExactDivision {

    String value() default "100";

    String message() default "${validatedValue}不能被{value}整除";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
