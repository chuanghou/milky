package com.stellariver.milky.domain.support.command;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author houchuang
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ConstructorHandler {

}
