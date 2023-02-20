package com.stellariver.milky.domain.support.dependency;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.stellariver.milky.common.tool.exception.SysException;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    NullStrategy nullStrategy;
    MethodAccess methodAccess;
    int getterIndex;
    int setterIndex;
    Object defaultValue;

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
            int getterIndex = methodAccess.getIndex(getter);
            int setterIndex = methodAccess.getIndex(setter, f.getType());
            NullHolder annotation = f.getAnnotation(NullHolder.class);
            NullStrategy nullStrategy = annotation.value();
            Object defaultValue = annotation.value().holderValue;
            if (nullStrategy == NullStrategy.CUSTOM) {
                Supplier<Object> supplier;
                try {
                    supplier = annotation.holderSupplier().newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                defaultValue = supplier.get();
            }
            return new FieldAccessor(nullStrategy, methodAccess, getterIndex, setterIndex, defaultValue);
        }).collect(Collectors.toList());
        map.put(clazz, fieldAccessors);
        return fieldAccessors;
    }


    static public Object replaceNullFieldByStrategy(Object obj) {
        List<FieldAccessor> fieldAccessors = get(obj.getClass());
        for (FieldAccessor fA : fieldAccessors) {
            Object originalValue = fA.methodAccess.invoke(obj, fA.getterIndex);
            if (originalValue != null) {
                continue;
            }
            if (fA.nullStrategy == null) {
                throw new SysException(CONFIG_ERROR.message("TODO"));
            }
            fA.methodAccess.invoke(obj, fA.setterIndex, fA.nullStrategy.holderValue);
        }
        return obj;
    }

    static public Object recoverNullFieldByStrategy(Object obj) {
        List<FieldAccessor> fieldAccessors = get(obj.getClass());
        for (FieldAccessor fA : fieldAccessors) {
            Object originalValue = fA.methodAccess.invoke(obj, fA.getterIndex);
            if (originalValue == null) {
                throw new SysException(CONFIG_ERROR.message("TODO"));
            }
            if (fA.nullStrategy == null) {
                continue;
            }
            if (Objects.equals(fA.defaultValue, fA.nullStrategy.holderValue)) {
                fA.methodAccess.invoke(obj, fA.setterIndex, (Object) null);
            }
        }
        return obj;
    }

}
