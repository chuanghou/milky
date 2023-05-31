package com.stellariver.milky.domain.support.dependency;

import java.lang.annotation.*;


/**
 * This annotation like @Autowired in Spring,
 * but the field should static, and field type should an interface
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
     * @return true means the dependency is required, or else exception
     */
    boolean required() default true;

}
