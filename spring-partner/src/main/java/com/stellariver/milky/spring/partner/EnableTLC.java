package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target( {ElementType.METHOD, ElementType.TYPE})
public @interface EnableTLC {

    Class<? extends BaseQuery<?, ?>>[] disableBaseQueries() default {};

}