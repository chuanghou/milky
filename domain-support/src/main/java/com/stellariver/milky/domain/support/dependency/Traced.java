package com.stellariver.milky.domain.support.dependency;

import java.lang.annotation.*;

/**
 *
 *
 * @author houchuang
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Traced {

}
