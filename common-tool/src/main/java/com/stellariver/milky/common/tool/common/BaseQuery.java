package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.util.Collect;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface BaseQuery<T, ID> {

    default Optional<T> queryById(ID id) {
        List<T> ts = queryByIds(Collections.singletonList(id));
        if (Collect.isEmpty(ts)) {
            return Optional.empty();
        }
        return Optional.of(ts.get(0));
    }

    List<T> queryByIds(List<ID> ids);
}
