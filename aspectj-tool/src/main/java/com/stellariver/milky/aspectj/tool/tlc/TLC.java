package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.*;

/**
 * @author houchuang
 */
@Target( ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TLC {

    Class<? extends BaseQuery<?, ?>>[] disableBaseQueries() default {};

}