package com.stellariver.milky.spring.partner.limit;

import com.stellariver.milky.common.tool.stable.RlConfig;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRateLimit {

    double warningWaitTime() default 500.0; // FAIL_WAITING 模式 warning waiting time, unit mill second

}
