package com.stellariver.milky.domain.support.dependency;

import java.lang.annotation.*;


/**
 * This annotation like @Autowired in Spring,
 *
 * @author houchuang
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Milkywired {

    String name() default "";

    /**
     * Declares whether the annotated dependency is required.
     * <p>Defaults to {@code true}.
     */
    boolean required() default true;

}
