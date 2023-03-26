package com.stellariver.milky.validate.tool.validate;

import com.stellariver.milky.common.base.ExceptionType;

import javax.validation.groups.Default;
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

    ExceptionType type() default ExceptionType.BIZ;

    boolean log() default false;

    Class<?>[] groups() default Default.class;

    boolean failFast() default true;

}
