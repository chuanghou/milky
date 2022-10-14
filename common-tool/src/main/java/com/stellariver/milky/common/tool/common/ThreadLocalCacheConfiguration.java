package com.stellariver.milky.common.tool.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadLocalCacheConfiguration {

    long maximumSize() default 10L;

    long expireAfterWrite() default 3000L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
