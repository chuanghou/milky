package com.stellariver.milky.aspectj.tool.validate;

import com.stellariver.milky.common.base.ExceptionType;

import jakarta.validation.groups.Default;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author houchuang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {

    ExceptionType type() default ExceptionType.BIZ;

    Class<?>[] groups() default Default.class;

    boolean failFast() default true;

}
