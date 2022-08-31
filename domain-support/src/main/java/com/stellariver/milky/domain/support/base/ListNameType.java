package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.util.Json;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListNameType<V> extends NameType<List<V>> {

    private Class<?> vClazz;

    public ListNameType(String name, Class<?> vClazz) {
        super(name, List.class);
        this.vClazz = vClazz;
    }

    @SuppressWarnings("unchecked")
    public List<V> extractFrom(Map<NameType<?>, Object> map) {
        return (List<V>) map.get(this);
    }

    @SuppressWarnings("unchecked")
    public List<V> parseJson(String json) {
        return (List<V>) Json.parseList(json, vClazz);
    }

}
