package com.stellariver.milky.common.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author houchuang
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {

    String value() default "";

    String prefix() default "";

}
