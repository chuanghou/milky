package com.stellariver.milky.domain.support.base;

import javax.annotation.Nullable;

public interface DomainTunnel {

    @Nullable
    <T extends AggregateRoot> T getByAggregateId(Class<T> clazz, String aggregateId);

}
