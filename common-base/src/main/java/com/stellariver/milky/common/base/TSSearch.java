package com.stellariver.milky.common.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TSSearch {

    String index() default "";

    /**
     * 当对应字段是字符串，在多元索引中以模糊索引存在，可以使用like注解，类似关系型数据库的LIKE查询
     * @return 是否启动模糊搜索
     */
    boolean like() default false;

}