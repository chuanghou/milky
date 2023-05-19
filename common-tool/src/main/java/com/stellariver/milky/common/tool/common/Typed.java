package com.stellariver.milky.common.tool.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Typed<T> {

    private String name;

    private Class<?> clazz;

    @SuppressWarnings("unchecked")
    public T parseJson(String json) {
        return (T) Json.parse(json, clazz);
    }

    @SuppressWarnings("unchecked")
    public T parseJsonNode(JsonNode jsonNode) {
        return (T) Json.parse(jsonNode, clazz);
    }

    static private Map<Object, Typed<?>> map = new ConcurrentHashMap<>();
    static public Typed<?> transfer(Class<? extends Typed<?>> clazz) {
        Typed<?> typed = map.get(clazz);
        if (typed != null) {
            return typed;
        }
        Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
        Class<?> typedClass = clazz.getSuperclass();
        String simpleName = typedClass.getSimpleName();
        if (typedClass == Typed.class) {
            typed = new Typed<>(simpleName, (Class<?>) types[0]);
        } else if (typedClass == ListTyped.class) {
            typed = new ListTyped<>(simpleName, (Class<?>) types[0]);
        } else if (typedClass == SetTyped.class) {
            typed = new SetTyped<>(simpleName, (Class<?>) types[0]);
        } else if (typedClass == MapTyped.class) {
            typed = new MapTyped<>(simpleName, (Class<?>) types[0], (Class<?>) types[1]);
        } else {
            throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
        }
        map.put(clazz, typed);
        return typed;
    }


}
