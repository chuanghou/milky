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
    String fieldName;
    String className;
    Method getMethod;
    Method setMethod;
    Object replaceValue;


    public Object get(Object bean) {
        return Reflect.invoke(getMethod, bean);
    }

    public void set(Object bean, Object value) {
        Reflect.invoke(setMethod, bean, value);
    }

    static Set<Class<?>> FORBIDDEN = new HashSet<>(Arrays.asList(
            Boolean.class, boolean.class, Character.class, byte.class, Short.class, short.class, int.class, long.class));

    static Map<Class<?>, List<NulliableReplacer>> map = new ConcurrentHashMap<>();

    @SneakyThrows
    static public List<NulliableReplacer> replacerOf(Class<?> clazz) {
        return map.computeIfAbsent(clazz, c -> Reflect.ancestorClasses(clazz)
                .stream().map(NulliableReplacer::resolve).flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @SneakyThrows
    static private List<NulliableReplacer> resolve(Class<?> clazz) {
        List<NulliableReplacer> nulliableReplacers = map.get(clazz);
        if (nulliableReplacers != null) {
            return nulliableReplacers;
        }
        nulliableReplacers = new ArrayList<>();
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        fields.forEach(f -> SysEx.trueThrow(FORBIDDEN.contains(f.getType()),
                CONFIG_ERROR.message(f.getType().getSimpleName() + " belongs to forbidden type!")));

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
                    .fieldName(name)
                    .className(f.getDeclaringClass().getSimpleName())
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
