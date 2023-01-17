package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author houchuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SetTyped<V> extends Typed<Set<V>> {

    private Class<?> vClazz;

    @Override
    @SuppressWarnings("unchecked")
    public Set<V> extractFrom(Map<Typed<?>, Object> map) {
        return (Set<V>) map.get(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<V> parseJson(String json) {
        return (Set<V>) Json.parseSet(json, vClazz);
    }

    @SuppressWarnings("unchecked")
    public Set<V> parseJsonNode(JsonNode jsonNode) {
        return (Set<V>) Json.parseSet(jsonNode, vClazz);
    }

}