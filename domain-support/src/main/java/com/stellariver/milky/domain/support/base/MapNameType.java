package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapNameType<K, V> extends NameType<Map<K, V>> {

    private Class<?> kClazz;

    private Class<?> vClazz;

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
