package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Collect;

import java.lang.annotation.*;
import java.util.Map;
import java.util.function.Supplier;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nulliable {

    boolean ignore() default false;

    /**
     * If ho
     * {@link Integer#MIN_VALUE} + 1 as null replacer when the field is an Integer
     * {@link Long#MIN_VALUE} + 1 as null replacer when the field is a Long
     * NULL_REPLACER as null replacer when the field is a String
     *
     */
    Class<? extends Supplier<Object>> replacerSupplier() default PlaceHolder.class;

    class PlaceHolder implements Supplier<Object> {
        @Override
        public Object get() {
            throw new SysEx(CONFIG_ERROR.message("not work"));
        }
    }

    Map<Class<?>, Object> defaultReplacer = Collect.asMap(
            String.class, "NULL_REPLACER",
            Integer.class, Integer.MIN_VALUE + 1,
            Long.class, Long.MIN_VALUE + 1
    );

}
