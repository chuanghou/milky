package com.stellariver.milky.common.tool.throwing;

import com.stellariver.milky.common.tool.common.SysException;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U, E extends Throwable> {
    void accept(T t, U u) throws E;
    static <T, U, E extends Throwable> BiConsumer<T, U> unchecked(ThrowingBiConsumer<T, U, E> consumer) {
        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Throwable e) {
                throw new SysException(e);
            }
        };
    }
}
