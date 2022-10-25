package com.stellariver.milky.spring.partner.tlc;

import com.stellariver.milky.common.tool.BaseQuery;

import java.lang.annotation.*;

@Target( ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableTLC {

    Class<? extends BaseQuery<?, ?>>[] disableBaseQueries() default {};

}