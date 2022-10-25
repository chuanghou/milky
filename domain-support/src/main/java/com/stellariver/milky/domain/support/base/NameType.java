package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Kit;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;

@Data
public class NameType<T> {

    private String name;

    private Class<?> clazz;

    @SuppressWarnings("unchecked")
    public T extractFrom(Map<NameType<?>, Object> map) {
        return (T) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public T extractFrom(Map<NameType<?>, Object> map, T defaultValue) {
        return Kit.op((T) map.get(this)).orElse(defaultValue);
    }

    @SuppressWarnings("unchecked")
    public T parseJson(String json) {
        return (T) Json.parse(json, clazz);
    }

}
