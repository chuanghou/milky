package com.stellariver.milky.spring.partner.limit;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRateLimit {

    RateLimitSupport.Strategy strategy() default RateLimitSupport.Strategy.FAIL_FAST;

    double warningWaitTime() default 1.0; // FAIL_WAITING 模式 warning time, unit second

}
