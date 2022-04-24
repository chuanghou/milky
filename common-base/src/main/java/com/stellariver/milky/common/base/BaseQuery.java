package com.stellariver.milky.common.base;


import java.util.*;

public interface BaseQuery<T, ID> {

    Map<ID, T> queryMapByIds(Set<ID> ids);

    default T queryById(ID id) {
        Map<ID, T> mapResult = queryMapByIds(new HashSet<>(Collections.singletonList(id)));
        return Optional.of(mapResult.get(id))
                .orElseThrow(() -> new RuntimeException(String.format("entity of %s not exists", id)));
    }

    default Optional<T> queryByIdOptional(ID id) {
        Map<ID, T> mapResult = queryMapByIds(new HashSet<>(Collections.singletonList(id)));
        return Optional.ofNullable(mapResult.get(id));
    }

    default Set<T> queryByIds(Set<ID> ids) {
        return new HashSet<>(queryMapByIds(ids).values());
    }

}
