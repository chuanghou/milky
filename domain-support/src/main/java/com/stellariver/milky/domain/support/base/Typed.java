package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;

/**
 * @author houchuang
 */
@Data
public class Typed<T> {

    private String name;

    private Class<?> clazz;

    @SuppressWarnings("unchecked")
    public T extractFrom(Map<Typed<?>, Object> map) {
        return (T) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public T extractFrom(Map<Typed<?>, Object> map, T defaultValue) {
        return Kit.op((T) map.get(this)).orElse(defaultValue);
    }

    @SuppressWarnings("unchecked")
    public T parseJson(String json) {
        return (T) Json.parse(json, clazz);
    }

    @SuppressWarnings("unchecked")
    public T parseJsonNode(JsonNode jsonNode) {
        return (T) Json.parse(jsonNode, clazz);
    }

}
