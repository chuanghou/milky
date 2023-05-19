package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author houchuang
 */
@Target( ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TLC {

    Class<? extends BaseQuery<?, ?>>[] disableBaseQueries() default {};

}