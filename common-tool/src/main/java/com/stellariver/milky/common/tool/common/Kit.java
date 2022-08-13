package com.stellariver.milky.common.tool.common;

import java.util.Objects;
import java.util.Optional;

public class Kit {


    public static boolean eq(Object a, Object b) {
        return Objects.equals(a, b);
    }

    public static <T> Optional<T> op(T value) {
        return Optional.ofNullable(value);
    }

    public static boolean isNull(Object value) {
        return value == null;
    }

    public static boolean notNull(Object value) {
        return value != null;
    }

}
