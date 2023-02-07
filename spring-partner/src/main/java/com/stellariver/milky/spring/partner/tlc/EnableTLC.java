package com.stellariver.milky.spring.partner.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.*;

/**
 * @author houchuang
 */
@Target( ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableTLC {

    Class<? extends BaseQuery<?, ?>>[] enableBaseQueries() default {};

}