package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Reflect;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NulliableReplacer {

    boolean config;
    boolean ignore;
    Object replaceValue;
    String fieldName;
    String className;
    Method getMethod;
    Method setMethod;

    public Object get(Object bean) {
        return Reflect.invoke(getMethod, bean);
    }

    public void set(Object bean, Object value) {
        Reflect.invoke(setMethod, bean, value);
    }

    static Set<Class<?>> primitives = new HashSet<>(Arrays.asList(boolean.class, byte.class, short.class, int.class, long.class));
    static Map<Class<?>, List<NulliableReplacer>> map = new ConcurrentHashMap<>();

    @SneakyThrows
    static public List<NulliableReplacer> resolveReplacer(Class<?> clazz) {
        return map.computeIfAbsent(clazz, NulliableReplacer::build);
    }

    @SneakyThrows
    static private List<NulliableReplacer> build(Class<?> clazz) {
        List<NulliableReplacer> nulliableReplacers = new ArrayList<>();
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        fields.forEach(f -> SysEx.trueThrow(primitives.contains(f.getType()),
                CONFIG_ERROR.message("primitive could not be a field of an aggregate")));
        for (Field f: fields) {
            if (f.isSynthetic() || Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            String name = f.getName();
            String get = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            String set = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getMethod = clazz.getMethod(get);
            Method setMethod = clazz.getMethod(set, f.getType());
            NulliableReplacerBuilder builder = NulliableReplacer.builder()
                    .className(f.getDeclaringClass().getSimpleName())
                    .fieldName(name)
                    .getMethod(getMethod)
                    .setMethod(setMethod);
            Object replaceValue;

            Nulliable nulliable = f.getAnnotation(Nulliable.class);
            if (nulliable == null) {
                NulliableReplacer nulliableReplacer = builder.config(false).build();
                nulliableReplacers.add(nulliableReplacer);
                continue;
            } else {
                builder.config(true);
            }

            if (nulliable.ignore()) {
                NulliableReplacer nulliableReplacer = builder.ignore(true).build();
                nulliableReplacers.add(nulliableReplacer);
                continue;
            } else {
                builder.ignore(false);
            }

            if (nulliable.replacerSupplier() != Nulliable.PlaceHolder.class) {
                replaceValue = nulliable.replacerSupplier().newInstance().get();
            } else {
                Object o = Nulliable.defaultReplacer.get(f.getType());
                if (o != null) {
                    replaceValue = o;
                } else {
                    throw new SysEx(CONFIG_ERROR.message("need a replacer supplier class!"));
                }
            }
            NulliableReplacer nulliableReplacer = builder.replaceValue(replaceValue).build();
            nulliableReplacers.add(nulliableReplacer);
        }
        return Collections.unmodifiableList(nulliableReplacers);
    }

}
