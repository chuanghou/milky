package com.stellariver.milky.common.tool.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

public class Kit {

    public static boolean eq(Object a, Object b) {
        boolean notNull = notNull(a) && notNull(b);
        SysException.trueThrowGet(notNull && (a.getClass() != b.getClass()),
                () -> ErrorEnumBase.NOT_SUPPORT_DIFFERENT_TYPE_COMPARE);
        return Objects.equals(a, b);
    }

    public static boolean notEq(Object a, Object b) {
        return !Kit.eq(a, b);
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

    public static boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }

    public static boolean notBlank(String value) {
        return !StringUtils.isBlank(value);
    }

}
