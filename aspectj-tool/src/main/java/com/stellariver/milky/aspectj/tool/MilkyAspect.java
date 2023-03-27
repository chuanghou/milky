package com.stellariver.milky.aspectj.tool;

import com.stellariver.milky.aspectj.tool.limit.EnableRateLimit;
import com.stellariver.milky.aspectj.tool.tlc.EnableTLC;
import com.stellariver.milky.aspectj.tool.log.Log;
import com.stellariver.milky.aspectj.tool.validate.Validate;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MilkyAspect {

    Log log() default @Log;

    Validate validate() default @Validate;

    EnableTLC enableTLC() default @EnableTLC;

    EnableRateLimit enableRateLimit() default @EnableRateLimit;

}
