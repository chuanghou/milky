package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.util.Reflect;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Accessor {

    String fieldName;
    String className;
    Method getMethod;
    Method setMethod;

    public Object getValue(Object bean) {
        return Reflect.invoke(getMethod, bean);
    }

    public void setValue(Object bean, Object value) {
        Reflect.invoke(setMethod, bean, value);
    }

    static Map<Class<?>, List<Accessor>> map = new ConcurrentHashMap<>();

    @SneakyThrows
    static public List<Accessor> resolveAccessors(Class<?> clazz) {
        List<Accessor> accessors = map.get(clazz);
        if (accessors != null) {
            return accessors;
        }
        accessors = new ArrayList<>();
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        for (Field f: fields) {

            if (f.isSynthetic() || Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            String name = f.getName();
            String get = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            if (f.getType() == boolean.class) {
                get = "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            String set = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getMethod = clazz.getMethod(get);
            Method setMethod = clazz.getMethod(set, f.getType());
            Accessor accessor = Accessor.builder()
                    .fieldName(name)
                    .className(f.getDeclaringClass().getSimpleName())
                    .getMethod(getMethod)
                    .setMethod(setMethod)
                    .build();
            accessors.add(accessor);

        }
        return Collections.unmodifiableList(accessors);
    }

}
