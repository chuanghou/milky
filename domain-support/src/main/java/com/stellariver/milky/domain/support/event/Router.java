package com.stellariver.milky.domain.support.event;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Router {

    int order() default Integer.MAX_VALUE;

    HandlerTypeEnum type() default HandlerTypeEnum.SYNC;

    String executor() default "asyncExecutor";
}
