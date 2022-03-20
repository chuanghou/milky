package com.stellariver.milky.common.tool.throwing;

import com.stellariver.milky.common.tool.common.SysException;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
    static <T, E extends Throwable> Consumer<T> unchecked(ThrowingConsumer<T, E> consumer) {
        return (t) -> {
            try {
                consumer.accept(t);
            } catch (Throwable e) {
                throw new SysException(e);
            }
        };
    }
}
