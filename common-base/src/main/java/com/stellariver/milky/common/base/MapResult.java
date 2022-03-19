package com.stellariver.milky.common.base;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(builderMethodName = "mapResultBuilder")
public class MapResult<K, T> extends Result<T> {

    @Builder.Default
    private List<K> failureKeys = new ArrayList<>();

    @Builder.Default
    private Map<K, Result<T>> resultMap = new HashMap<>();

    public void put(K key, Result<T> result) {
        if (!result.getSuccess()) {
            failureKeys.add(key);
        }
        resultMap.put(key, result);
    }

    public Result<T> get(K key) {
        return resultMap.get(key);
    }

    public T getValue(K key) {
        return Optional.ofNullable(resultMap.get(key)).map(Result::getData).orElse(null);
    }

    public List<T> getDataList() {
        return resultMap.values().stream().filter(Result::getSuccess).map(Result::getData).collect(Collectors.toList());
    }

}
