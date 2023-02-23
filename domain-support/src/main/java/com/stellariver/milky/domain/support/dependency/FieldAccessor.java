package com.stellariver.milky.domain.support.dependency;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Reflect;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FieldAccessor {

    String fieldName;
    String className;
    @Nullable
    Strategy strategy;
    Method getMethod;
    Method setMethod;
    @Nullable
    Object replacer;

    public Object get(Object bean) {
        return Reflect.invoke(getMethod, bean);
    }

    public void set(Object bean, Object value) {
        Reflect.invoke(setMethod, bean, value);
    }

    static Set<Class<?>> primitives = new HashSet<>(Arrays.asList(byte.class, short.class, int.class, long.class));
    static Map<Class<?>, List<FieldAccessor>> map = new ConcurrentHashMap<>();

    @SneakyThrows
    static public List<FieldAccessor> get(Class<?> clazz) {
        List<FieldAccessor> fieldAccessors = map.get(clazz);
        if (fieldAccessors != null) {
            return fieldAccessors;
        }
        fieldAccessors = new ArrayList<>();
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        fields.forEach(f -> SysException.trueThrow(primitives.contains(f.getType()), CONFIG_ERROR));
        for (Field f: fields) {
            String name = f.getName();
            String get = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            String set = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getMethod = clazz.getMethod(get);
            Method setMethod = clazz.getMethod(set, f.getType());

            Strategy strategy = null;
            Object replacer = null;
            NullReplacer annotation = f.getAnnotation(NullReplacer.class);
            if (annotation != null) {
                strategy = annotation.value();
                replacer = annotation.value().replacer;
                if (strategy == Strategy.CUSTOM) {
                    replacer = annotation.holderSupplier().newInstance().get();
                }
                boolean assignableFrom = f.getType().isAssignableFrom(replacer.getClass());
                SysException.falseThrow(assignableFrom, CONFIG_ERROR.message("the class of replacer is not appropriate!"));
            }

            FieldAccessor fieldAccessor = new FieldAccessor(name, clazz.getName(), strategy, getMethod, setMethod, replacer);
            fieldAccessors.add(fieldAccessor);
        }

        map.put(clazz, fieldAccessors);
        return fieldAccessors;
    }

}
