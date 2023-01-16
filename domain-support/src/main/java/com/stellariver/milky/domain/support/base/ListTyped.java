package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListTyped<V> extends Typed<List<V>> {

    private Class<?> vClazz;

    @Override
    @SuppressWarnings("unchecked")
    public List<V> extractFrom(Map<Typed<?>, Object> map) {
        return (List<V>) map.get(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<V> parseJson(String json) {
        return (List<V>) Json.parseList(json, vClazz);
    }

    @SuppressWarnings("unchecked")
    public List<V> parseJsonNode(JsonNode jsonNode) {
        return (List<V>) Json.parseList(jsonNode, vClazz);
    }

}
