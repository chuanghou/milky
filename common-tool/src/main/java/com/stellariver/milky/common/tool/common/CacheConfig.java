package com.stellariver.milky.common.tool.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

/**
 * @author houchuang
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheConfig {

    long tlcMaximumSize() default 10L;

    long tlcExpireAfterWrite() default 3000L;

    long barrierCacheMaximumSize() default 0L;

    long barrierCacheExpireAfterWrite() default 0L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
