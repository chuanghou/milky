package com.stellariver.milky.domain.support.dependency;

import java.lang.annotation.*;


/**
 * This Milk
 *
 * @author houchuang
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Milkywired {

    String name() default "";

}
