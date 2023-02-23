package com.stellariver.milky.domain.support.dependency;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.stellariver.milky.common.tool.exception.SysException;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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
    MethodAccess methodAccess;
    int getterIndex;
    int setterIndex;
    @Nullable
    Object replacer;

    public Object get(Object bean) {
        return methodAccess.invoke(bean, getterIndex);
    }

    public void set(Object bean, Object value) {
        methodAccess.invoke(bean, setterIndex, value);
    }

    static Set<Class<?>> primitives = new HashSet<>(Arrays.asList(byte.class, short.class, int.class, long.class));
    static Map<Class<?>, List<FieldAccessor>> map = new ConcurrentHashMap<>();

    @SneakyThrows
    static public List<FieldAccessor> get(Class<?> clazz) {
        List<FieldAccessor> fieldAccessors = map.get(clazz);
        if (fieldAccessors != null) {
            return fieldAccessors;
        }
        List<Field> fields = Arrays.stream(clazz.getFields()).collect(Collectors.toList());
        fields.forEach(f -> SysException.trueThrow(primitives.contains(f.getType()), CONFIG_ERROR));
        MethodAccess methodAccess = MethodAccess.get(clazz);
        fieldAccessors = fields.stream().map(f -> {

            String name = f.getName();
            String getter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            String setter = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);

            int getterIndex, setterIndex;
            try {
                getterIndex = methodAccess.getIndex(getter);
                setterIndex = methodAccess.getIndex(setter, f.getType());
            } catch (IllegalArgumentException exception) {
                String message = String.format("could not find method %s, or %s", getter, setter);
                throw new SysException(CONFIG_ERROR.message(message));
            }

            Strategy strategy = null;
            Object replacer = null;
            NullReplacer annotation = f.getAnnotation(NullReplacer.class);
            if (annotation != null) {
                strategy = annotation.value();
                replacer = annotation.value().replacer;
                if (strategy == Strategy.CUSTOM) {
                    Supplier<Object> supplier;
                    try {
                        supplier = annotation.holderSupplier().newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    replacer = supplier.get();
                }
                boolean assignableFrom = f.getType().isAssignableFrom(replacer.getClass());
                SysException.falseThrow(assignableFrom, CONFIG_ERROR.message("the class of replacer is not appropriate!"));
            }

            return new FieldAccessor(name, clazz.getName(), strategy, methodAccess, getterIndex, setterIndex, replacer);
        }).collect(Collectors.toList());
        map.put(clazz, fieldAccessors);
        return fieldAccessors;
    }

}
