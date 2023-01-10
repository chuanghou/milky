package com.stellariver.milky.spring.partner.limit;


import java.lang.annotation.*;

/**
 * @author houchuang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRateLimit {

}
