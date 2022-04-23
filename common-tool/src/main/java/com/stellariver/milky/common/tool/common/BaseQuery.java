package com.stellariver.milky.common.tool.common;


import java.util.*;

public interface BaseQuery<T, ID> {

    default Optional<T> queryById(ID id) {
        Map<ID, T> mapResult = queryMapByIds(new HashSet<>(Collections.singletonList(id)));
        return Optional.ofNullable(mapResult.get(id));
    }

    default List<T> queryByIds(List<ID> ids) {
        return new ArrayList<>(queryMapByIds(new HashSet<>(ids)).values());
    }

    Map<ID, T> queryMapByIds(Set<ID> ids);
}
