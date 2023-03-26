package com.stellariver.milky.validate.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.*;

/**
 * @author houchuang
 */
@Target( ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableTLC {

    Class<? extends BaseQuery<?, ?>>[] disableBaseQueries() default {};

}