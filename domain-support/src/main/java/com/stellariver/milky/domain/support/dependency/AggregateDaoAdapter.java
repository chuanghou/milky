package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.AggregateRoot;
import lombok.NonNull;

public interface AggregateDaoAdapter<Aggregate extends AggregateRoot> {


    Aggregate toAggregate(@NonNull Object dataObject);

    default Object toDataObjectWrapper(Object aggregate) {
        Aggregate aggregateRoot = (Aggregate) aggregate;
        return toDataObject(aggregateRoot, dataObject(aggregateRoot.getAggregateId()));
    }

    Object toDataObject(Aggregate aggregate, DataObjectInfo dataObjectInfo);



}
