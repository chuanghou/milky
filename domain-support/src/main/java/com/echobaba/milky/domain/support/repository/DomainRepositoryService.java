package com.echobaba.milky.domain.support.repository;

public interface DomainRepositoryService<T> {

    T getByAggregateId(String agg);

    void save(T t);
}
