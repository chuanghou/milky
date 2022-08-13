package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;

import java.util.Map;

public class MapNameType<K, V> extends NameType<Map<K, V>> {

    private final Class<?> kClazz;

    private final Class<?> vClazz;

    public MapNameType(String name, Class<?> kClazz, Class<?> vClazz) {
        super(name, Map.class);
        this.kClazz = kClazz;
        this.vClazz = vClazz;
    }

    @SuppressWarnings("unchecked")
    public Map<K, V> extractFrom(Map<NameType<?>, Object> map) {
        return (Map<K, V>) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public Map<K, V> parseJson(String json) {
        return (Map<K, V>) Json.parseMap(json, kClazz, vClazz);
    }

}
