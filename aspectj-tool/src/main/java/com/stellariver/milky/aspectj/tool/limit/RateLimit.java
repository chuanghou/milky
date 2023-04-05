package com.stellariver.milky.aspectj.tool.limit;


import java.lang.annotation.*;

/**
 * @author houchuang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

}
