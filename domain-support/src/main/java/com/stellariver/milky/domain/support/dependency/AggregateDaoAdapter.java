package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface AggregateDaoAdapter<Aggregate extends AggregateRoot> {


    /**
     * 实现数据库对象到聚合根转化
     * @param dataObject 数据库对象
     * @return 聚合根对象
     */
    Aggregate toAggregate(@NonNull Object dataObject);

    /**
     * 聚合根对象转换为数据库对象
     * @param aggregate 聚合根对象
     * @return 数据库对象
     */
    @SuppressWarnings("unchecked")
    default Object toDataObjectWrapper(Object aggregate) {
        Aggregate aggregateRoot = (Aggregate) aggregate;
        return toDataObject(aggregateRoot, dataObject(aggregateRoot.getAggregateId()));
    }

    Object toDataObject(Aggregate aggregate, DataObjectInfo dataObjectInfo);


    /**
     * 根据聚合根主键获取DO对象信息
     * @param aggregateId 聚合根主键id
     * @return 聚合根对象DO对象信息
     */
    DataObjectInfo dataObjectInfo(String aggregateId);


    default Optional<Aggregate> getByAggregateIdOptional(String aggregateId, Context context, DAOWrapper<?, ?> daoWrapper) {
        Map<Class<?>, Map<Object, Object>> doMap = context.getDoMap();
        DataObjectInfo dataObjectInfo = dataObjectInfo(aggregateId);
        Class<? extends BaseDataObject<?>> clazz = dataObjectInfo.getClazz();
        Object primaryId = dataObjectInfo.getPrimaryId();
        Optional<Aggregate> aggregateOptional = Kit.op(doMap.get(clazz))
                .map(map -> map.get(primaryId))
                .map(this::toAggregate);
        if (aggregateOptional.isPresent()) {
            return aggregateOptional;
        } else {
            Optional<?> dataObjectOpt = daoWrapper.getByPrimaryIdWrapper(primaryId);
            dataObjectOpt.ifPresent(dataObject -> doMap.computeIfAbsent(clazz, k -> new HashMap<>())
                    .put(primaryId, dataObject));
            return dataObjectOpt.map(this::toAggregate);
        }
    }

    default Optional<Aggregate> getByAggregateIdOptional(String aggregateId, Context context) {
        DataObjectInfo dataObjectInfo = dataObjectInfo(aggregateId);
        CommandBus.getDaoWrapper(dataObjectInfo.getClazz());
    }

    default Aggregate getByAggregateId(String aggregateId, Context context, DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper) {
        return getByAggregateIdOptional(aggregateId, context)
    }
}
