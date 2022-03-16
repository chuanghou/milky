package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(builderMethodName = "mapResultBuilder")
public class MapResult<K, T> extends Result<T> {

    private Map<K, Result<T>> resultMap = new HashMap<>();

    public void put(K key, Result<T> result) {
        resultMap.put(key, result);
    }

    public Result<T> get(K key) {
        return resultMap.get(key);
    }

    public T getValue(K key) {
        return Optional.ofNullable(resultMap.get(key)).map(Result::getData).orElse(null);
    }
}
