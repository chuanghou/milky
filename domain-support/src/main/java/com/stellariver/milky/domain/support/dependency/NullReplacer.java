package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.exception.SysEx;

import java.lang.annotation.*;
import java.util.function.Supplier;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NullReplacer {

    Strategy value() default Strategy.CUSTOM;

    Class<? extends Supplier<Object>> holderSupplier() default DefaultCustomSupplier.class;

    class DefaultCustomSupplier implements Supplier<Object> {
        @Override
        public Object get() {
            throw new SysEx(CONFIG_ERROR.message("When you choose custom strategy, " +
                    "you need to implement a supplier by yourself"));
        }
    }

}
