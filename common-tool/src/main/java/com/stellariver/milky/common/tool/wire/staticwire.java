package com.stellariver.milky.common.tool.wire;

import java.lang.annotation.*;


/**
 * This annotation like @Autowire in Spring,
 * but the field should static, and field type should an interface
 * @author houchuang
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface staticwire {

    String name() default "";
    boolean required() default true;

}
