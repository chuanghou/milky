package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.base.ExceptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author houchuang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Validate {

    ExceptionType type() default ExceptionType.SYS;

    Class<?>[] groups() default {};

    boolean failFast() default true;

}
