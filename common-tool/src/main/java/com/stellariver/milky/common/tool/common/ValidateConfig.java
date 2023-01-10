package com.stellariver.milky.common.tool.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author houchuang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValidateConfig {

    Class<?>[] groups() default {};

    boolean failFast() default true;

}
