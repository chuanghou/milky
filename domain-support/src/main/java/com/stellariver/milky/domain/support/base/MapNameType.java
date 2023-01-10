package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;

/**
 * @author houchuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MapNameType<K, V> extends NameType<Map<K, V>> {

    private Class<?> kClazz;

    private Class<?> vClazz;

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> extractFrom(Map<NameType<?>, Object> map) {
        return (Map<K, V>) map.get(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> parseJson(String json) {
        return (Map<K, V>) Json.parseMap(json, kClazz, vClazz);
    }

}
