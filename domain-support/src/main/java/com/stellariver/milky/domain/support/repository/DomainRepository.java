package com.stellariver.milky.domain.support.repository;

import com.stellariver.milky.domain.support.context.Context;

public interface DomainRepository<T> {

    void save(T t, Context context);

    T getByAggregateId(String aggregateId);

    void updateByAggregateId(T t, Context context);


}
