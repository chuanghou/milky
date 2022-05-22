package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;

import java.util.List;
import java.util.Map;

public class ListNameType<L, V> extends NameType<L> {

    private final Class<?> vClazz;

    public ListNameType(String name, Class<?> vClazz) {
        super(name, List.class);
        this.vClazz = vClazz;
    }

    @SuppressWarnings("unchecked")
    public L extractFrom(Map<NameType<?>, Object> map) {
        return (L) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public L parseJson(String json) {
        return (L) Json.parseList(json, vClazz);
    }

}
