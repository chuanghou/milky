package com.stellariver.milky.common.base;


import java.util.*;

public interface BaseQuery<T, ID> {

    Map<ID, T> queryMapByIds(Set<ID> ids);

    default Set<T> querySetByIds(Set<ID> ids) {
        return new HashSet<>(queryMapByIds(ids).values());
    }

    default List<T> queryListByIds(Set<ID> ids) {
        return new ArrayList<>(queryMapByIds(ids).values());
    }

    default Optional<T> queryByIdOptional(ID id) {
        Set<ID> ids = new HashSet<>(Collections.singletonList(id));
        Map<ID, T> tMap = queryMapByIds(ids);
        return Optional.ofNullable(tMap).map(m -> m.get(id));
    }

    default T queryById(ID id) {
        Set<ID> ids = new HashSet<>(Collections.singletonList(id));
        Map<ID, T> tMap = queryMapByIds(ids);
        return Optional.ofNullable(tMap).map(m -> m.get(id)).orElseThrow(
                () -> new RuntimeException(String.format("could not find %s", id)));
    }

}
