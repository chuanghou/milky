package com.stellariver.milky.aspectj.tool.rate.limit;


import java.lang.annotation.*;

/**
 * @author houchuang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

}
