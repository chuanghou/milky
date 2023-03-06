package com.stellariver.milky.domain.support.dependency;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MilkyWired {

    String name() default "";

}
