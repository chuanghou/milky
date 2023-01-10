package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author houchuang
 */
public class Kit {

    public static boolean eq(Object a, Object b) {
        boolean notNull = notNull(a) && notNull(b);
        SysException.trueThrowGet(notNull && (a.getClass() != b.getClass()), () -> ErrorEnumsBase.NOT_SUPPORT_DIFFERENT_TYPE_COMPARE);
        return Objects.equals(a, b);
    }

    public static boolean notEq(Object a, Object b) {
        return !Kit.eq(a, b);
    }

    public static <T> Optional<T> op(T value) {
        return Optional.ofNullable(value);
    }

    public static <T> boolean isNull(T value) {
        return value == null;
    }

    public static <T> boolean notNull(T value) {
        return value != null;
    }

    public static <T> T isNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }

    public static boolean notBlank(String value) {
        return !StringUtils.isBlank(value);
    }

    public static String isBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

}
