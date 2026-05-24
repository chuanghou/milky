package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在方法执行期间，为指定的 {@link BaseQuery} 开启线程级缓存（{@link BaseQuery#enableThreadLocal()}）。
 *
 * @author houchuang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TLC {

    /**
     * 需要在本线程内启用 TLC 的 BaseQuery 实现类。
     */
    Class<? extends BaseQuery<?, ?>>[] threadLocalBaseQueries() default {};

}
