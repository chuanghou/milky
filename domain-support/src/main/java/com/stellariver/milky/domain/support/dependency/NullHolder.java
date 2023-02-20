package com.stellariver.milky.domain.support.dependency;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Supplier;

@Retention(RetentionPolicy.RUNTIME)
public @interface NullHolder {
    NullStrategy value() default NullStrategy.CUSTOM;

    Class<? extends Supplier<Object>> holderSupplier() default DefaultSupplier.class;

    class DefaultSupplier implements Supplier<Object> {
        @Override
        public Object get() {
            throw new UnsupportedOperationException();
        }
    }
}
