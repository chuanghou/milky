package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.domain.support.ErrorEnums;

import java.util.Optional;

public interface DomainTunnel {

    default <T extends AggregateRoot> T getByAggregateId(Class<T> clazz, Object aggregateId) {
        Optional<T> optional = getByAggregateIdOptional(clazz, aggregateId.toString());
        if (!optional.isPresent()) {
            throw new SysEx(ErrorEnums.SYS_EX.message(clazz.getSimpleName() + aggregateId));
        }
        return optional.get();
    }

    <T extends AggregateRoot> Optional<T> getByAggregateIdOptional(Class<T> clazz, Object aggregateId);

}
