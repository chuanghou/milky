package com.stellariver.milky.spring.partner.limit;

import com.stellariver.milky.common.tool.stable.RlConfig;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRateLimit {

}
