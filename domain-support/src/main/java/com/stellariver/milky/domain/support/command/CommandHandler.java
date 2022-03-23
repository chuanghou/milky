package com.stellariver.milky.domain.support.command;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD, CONSTRUCTOR})
public @interface CommandHandler {

    String[] dependencyKeys() default {};

}
