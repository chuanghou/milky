package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapTyped<K, V> extends Typed<Map<K, V>> {

    private Class<?> kClazz;

    private Class<?> vClazz;

    public MapTyped(String name, Class<?> kClazz, Class<?> vClazz) {
        super(name, Map.class);
        this.kClazz = kClazz;
        this.vClazz = vClazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> parseJson(String json) {
        return (Map<K, V>) Json.parseMap(json, kClazz, vClazz);
    }

    @SuppressWarnings("unchecked")
    public Map<K, V> parseJsonNode(JsonNode jsonNode) {
        return (Map<K, V>) Json.parseMap(jsonNode, kClazz, vClazz);
    }

}
