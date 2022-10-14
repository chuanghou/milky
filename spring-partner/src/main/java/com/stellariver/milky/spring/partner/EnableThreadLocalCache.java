package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.*;

@Target( ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableThreadLocalCache {

    Class<? extends BaseQuery<?, ?>>[] enableBaseQueries();

}