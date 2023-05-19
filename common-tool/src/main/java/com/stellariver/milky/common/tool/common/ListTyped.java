package com.stellariver.milky.common.tool.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.stellariver.milky.common.tool.util.Json;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListTyped<V> extends Typed<List<V>> {

    private Class<?> vClazz;

    public ListTyped(String name, Class<?> vClazz) {
        super(name, List.class);
        this.vClazz = vClazz;
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
