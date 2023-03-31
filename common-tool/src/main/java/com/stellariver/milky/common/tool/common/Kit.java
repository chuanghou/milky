package com.stellariver.milky.common.tool.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.tool.slambda.SFunction;
import com.stellariver.milky.common.tool.slambda.SLambda;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author houchuang
 */
public class Kit {

    public static boolean eq(Object a, Object b) {
        return Objects.deepEquals(a, b);
    }

    public static boolean notEq(Object a, Object b) {
        return !Objects.deepEquals(a, b);
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

    public static <E extends Enum<E>> Optional<E> enumOf(Class<E> enumClass, @NonNull String enumName) {
        try {
            return Optional.of(Enum.valueOf(enumClass, enumName));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
    final static private Cache<Class<?>, Map<Object, Object>> enumValueMap = CacheBuilder.newBuilder().softValues().build();

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>, V> Optional<E> enumOf(SFunction<E, V> getter, V value) {
        Class<? extends SFunction<E, V>> getterC = (Class<? extends SFunction<E, V>>) getter.getClass();
        E o = (E) enumValueMap.get(getterC, () -> {
            E[] enumConstants = ((Class<E>)SLambda.extract(getter).getInstantiatedClass()).getEnumConstants();
            return new HashMap<>(Collect.toMapMightEx(enumConstants, getter));
        }).get(value);
        return Kit.op(o);
    }



    static public <T> T whenNull(T t, T defaultValue) {
        return t == null ? defaultValue : t;
    }

    static public String format(String format, Object... objects) {
        return String.format(format, objects);
    }

    @Nullable
    @SafeVarargs
    static public <T> T defaultChain(Supplier<T>... suppliers) {
        return defaultChain(null, suppliers);
    }

    @SafeVarargs
    static public <T> T defaultChain(T defaultValue, Supplier<T>... suppliers) {
        for (Supplier<T> supplier : suppliers) {
            T t = supplier.get();
            if (t != null) {
                return t;
            }
        }
        return defaultValue;
    }

}
