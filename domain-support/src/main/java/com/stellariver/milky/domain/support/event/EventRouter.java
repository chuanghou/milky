package com.stellariver.milky.domain.support.event;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface EventRouter {

    int order() default Integer.MAX_VALUE;

    TypeEnum type() default TypeEnum.SYNC;

    String executor() default "asyncExecutor";
}
