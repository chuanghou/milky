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
public class Getter {

    boolean config;
    boolean ignore;
    String fieldName;
    String className;
    Method getMethod;

    public Object getValue(Object bean) {
        return Reflect.invoke(getMethod, bean);
    }

    static Map<Class<?>, List<Getter>> map = new ConcurrentHashMap<>();

    @SneakyThrows
    static public List<Getter> getGetters(Class<?> clazz) {
        List<Getter> getters = map.get(clazz);
        if (getters != null) {
            return getters;
        }
        getters = new ArrayList<>();
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        for (Field f: fields) {

            if (f.isSynthetic() || Modifier.isStatic(f.getModifiers()) || f.getType() == boolean.class) {
                continue;
            }

            String name = f.getName();
            String get = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getMethod = clazz.getMethod(get);
            Getter getter = Getter.builder()
                    .fieldName(name)
                    .className(f.getDeclaringClass().getSimpleName())
                    .getMethod(getMethod)
                    .build();
            getters.add(getter);

        }
        return Collections.unmodifiableList(getters);
    }

}
