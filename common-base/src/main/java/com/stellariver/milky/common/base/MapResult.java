package com.stellariver.milky.common.base;

import java.util.*;
import java.util.stream.Collectors;

public class MapResult<K, T> {

    private final Map<K, Result<T>> resultMap = new HashMap<>();

    public void put(K key, Result<T> result) {

        resultMap.put(key, result);
    }

    public Result<T> getResult(K key) {
        return resultMap.get(key);
    }

    public T getValue(K key) {
        return Optional.ofNullable(resultMap.get(key)).map(Result::getData).orElse(null);
    }

    public Set<T> values() {
        return resultMap.values().stream().map(Result::getData).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Set<K> keys() {
        return resultMap.keySet();
    }

}
