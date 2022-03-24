package com.stellariver.milky.domain.support.repository;

import com.stellariver.milky.domain.support.context.Context;

import java.util.Optional;

public interface DomainRepository<T> {

    void save(T t, Context context);

    Optional<T> getByAggregateId(String aggregateId);

    void updateByAggregateId(T t, Context context);


}
