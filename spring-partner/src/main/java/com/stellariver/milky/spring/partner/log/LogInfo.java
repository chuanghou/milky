package com.stellariver.milky.spring.partner.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogInfo {

    // 当代理检测到logTag为default会自动切换为method签名
    String logTag() default "default";

}
