package com.stellariver.milky.common.base;

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
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
public @interface TimeFormat {

    String format() default "yyyyMMdd";

    boolean checkNotEarlier() default false;

    String message() default "时间数据不满足要求";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
