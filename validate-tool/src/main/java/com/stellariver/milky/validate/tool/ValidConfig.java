package com.stellariver.milky.validate.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author houchuang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValidConfig {

    ValidateUtil.ExceptionType type() default ValidateUtil.ExceptionType.SYS;

    Class<?>[] groups() default {};

    boolean failFast() default true;

}
