package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.slambda.SFunction;
import com.stellariver.milky.common.tool.slambda.SLambda;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author houchuang
 */
public class Kit {

    public static boolean eq(Object a, Object b) {
//        boolean notNull = notNull(a) && notNull(b);
//        SysException.trueThrowGet(notNull && (a.getClass() != b.getClass()), () -> ErrorEnumsBase.NOT_SUPPORT_DIFFERENT_TYPE_COMPARE);
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

    public static <E extends Enum<E>> Optional<E> enumOf(@NonNull Class<E> enumClass, @NonNull String enumName) {
        try {
            return Optional.of(Enum.valueOf(enumClass, enumName));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    final static private Map<Class<?>, Class<?>> enumMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>, V> Optional<E> enumOf(@NonNull SFunction<E, V> getter, @NonNull V value) {
        Class<E> enumClass = (Class<E>) enumMap.computeIfAbsent(
                getter.getClass(), c -> SLambda.extract(getter).getInstantiatedClass());
        E[] enumConstants = enumClass.getEnumConstants();
        return Arrays.stream(enumConstants).filter(e -> value.equals(getter.apply(e))).findFirst();
    }


    static public boolean eq(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) == 0;
    }

    static public boolean greater(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) > 0;
    }

    static public boolean greaterOrEq(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) >= 0;
    }

    static public boolean less(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) < 0;
    }

    static public boolean lessOrEq(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) <= 0;
    }

}
