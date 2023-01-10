package com.stellariver.milky.domain.support.interceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author houchuang
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Intercept {

    PosEnum pos();

    int order() default Integer.MAX_VALUE;
}
