package com.stellariver.milky.domain.support.base;



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
}


