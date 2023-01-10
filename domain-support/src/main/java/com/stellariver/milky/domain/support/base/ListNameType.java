package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListNameType<V> extends NameType<List<V>> {

    private Class<?> vClazz;

    @Override
    @SuppressWarnings("unchecked")
    public List<V> extractFrom(Map<NameType<?>, Object> map) {
        return (List<V>) map.get(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<V> parseJson(String json) {
        return (List<V>) Json.parseList(json, vClazz);
    }

}
