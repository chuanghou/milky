package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;
import lombok.Getter;

import java.util.Map;

public class NameType<T> {

    @Getter
    private final String name;

    private final Class<?> clazz;

    public NameType(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public T extractFrom(Map<NameType<?>, Object> map) {
        return (T) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public T parseJson(String json) {
        return (T) Json.parse(json, clazz);
    }

}
