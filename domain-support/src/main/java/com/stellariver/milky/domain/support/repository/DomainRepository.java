package com.stellariver.milky.domain.support.repository;

import com.stellariver.milky.domain.support.context.Context;

public interface DomainRepository<T> {

    T getByAggregateId(String aggregateId, Context context);

    T getByAggregateId(String aggregateId);

    void save(T t, Context context);
}
