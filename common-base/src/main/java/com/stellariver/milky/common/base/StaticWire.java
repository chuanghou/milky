package com.stellariver.milky.common.base;

import java.lang.annotation.*;


/**
 * This annotation like @Autowire in Spring,
 * but the field should static, and field type should an interface
 * @author houchuang
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticWire {

    String name() default "";
    boolean required() default true;

}
