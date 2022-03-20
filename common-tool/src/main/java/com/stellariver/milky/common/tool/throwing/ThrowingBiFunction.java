package com.stellariver.milky.common.tool.throwing;

import com.stellariver.milky.common.tool.common.SysException;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Throwable> {
    R apply(T t, U u) throws E;
    static <T, U, R, E extends Throwable> BiFunction<T, U, R> unchecked(ThrowingBiFunction<T, U, R, E> f) {
        return (t, u) -> {
            try {
                return f.apply(t, u);
            } catch (Throwable e) {
                throw new SysException(e);
            }
        };
    }
}
