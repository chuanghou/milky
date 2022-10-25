package com.stellariver.milky.domain.support.context;

import com.stellariver.milky.common.tool.log.LogChoice;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface DependencyKey {

    String value();

    LogChoice logChoice() default LogChoice.EXCEPTION;

    String[] requiredKeys() default {};

}
