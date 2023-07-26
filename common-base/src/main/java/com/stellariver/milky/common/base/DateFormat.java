package com.stellariver.milky.common.base;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * @author houchuang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
public @interface DateFormat {

    String format() default "yyyyMMdd";

    Compare compare() default Compare.NOT_CHECK;

    long delay() default 0L;

    ChronoUnit unit() default ChronoUnit.DAYS;

    String message() default "时间数据不满足要求";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
