package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import com.stellariver.milky.domain.support.dependency.DaoAdapter;
import com.stellariver.milky.domain.support.dependency.DataObjectInfo;

import java.util.Optional;

public class DomainTunnelImpl implements DomainTunnel{

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AggregateRoot> Optional<T> getByAggregateIdOptional(Class<T> aggregateClazz, Object aggregateId) {

        DaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(aggregateClazz);

        DataObjectInfo dataObjectInfo = daoAdapter.dataObjectInfo(aggregateId.toString());

        Class<? extends BaseDataObject<?>> dataObjectInfoClazz = dataObjectInfo.getClazz();

        DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = CommandBus.getDaoWrapper(dataObjectInfoClazz);

        Optional<Object> dataObjectOptional = daoWrapper.getByPrimaryIdOptionalWrapper(dataObjectInfo.getPrimaryId());

        T t = (T) dataObjectOptional.map(daoAdapter::toAggregate).orElse(null);


        return Optional.ofNullable(t);

    }
}
