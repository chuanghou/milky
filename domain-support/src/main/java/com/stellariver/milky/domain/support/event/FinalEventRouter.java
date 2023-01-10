package com.stellariver.milky.domain.support.event;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author houchuang
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface FinalEventRouter {

    boolean asyncable() default false;

    String executor() default "asyncExecutor";

    double order() default Double.MAX_VALUE;

}
