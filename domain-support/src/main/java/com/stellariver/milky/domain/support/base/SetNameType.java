package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetNameType<V> extends NameType<Set<V>> {

    private Class<?> vClazz;

    @SuppressWarnings("unchecked")
    public Set<V> extractFrom(Map<NameType<?>, Object> map) {
        return (Set<V>) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public Set<V> parseJson(String json) {
        return (Set<V>) Json.parseSet(json, vClazz);
    }

}
