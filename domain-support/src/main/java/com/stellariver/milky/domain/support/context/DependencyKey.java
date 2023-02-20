package com.stellariver.milky.domain.support.context;

import com.stellariver.milky.domain.support.base.Typed;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author houchuang
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface DependencyKey {

    Class<? extends Typed<?>> value();

    boolean alwaysLog() default false;

    Class<? extends Typed<?>>[] requiredKeys() default {};

}
