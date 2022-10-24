package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
        return toDataObject(aggregateRoot, dataObjectInfo(aggregateRoot.getAggregateId()));
    }

    Object toDataObject(Aggregate aggregate, DataObjectInfo dataObjectInfo);

    /**
     * 根据聚合根主键获取DO对象信息
     * @param aggregateId 聚合根主键id
     * @return 聚合根对应DO对象信息
     */
    DataObjectInfo dataObjectInfo(String aggregateId);

    default Optional<Aggregate> getByAggregateIdOptional(String aggregateId, Context context) {
        Aggregate aggregate = batchGetByAggregateIds(Collect.asSet(aggregateId), context).get(aggregateId);
        return Kit.op(aggregate);
    }

    default Aggregate getByAggregateId(String aggregateId, Context context) {
        return getByAggregateIdOptional(aggregateId, context)
                .orElseThrow(() -> new SysException(ErrorEnums.AGGREGATE_NOT_EXISTED.message(aggregateId)));
    }

    default Map<String, Aggregate> batchGetByAggregateIds(Set<String> aggregateIds, Context context) {
        List<DataObjectInfo> dataObjectInfos = Collect.transfer(aggregateIds, this::dataObjectInfo);
        Map<? extends Class<? extends BaseDataObject<?>>, Set<Object>> clazzPrimaryIdMap =
                Collect.groupSet(dataObjectInfos, DataObjectInfo::getClazz, DataObjectInfo::getPrimaryId);

        Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
        Set<Pair<? extends Class<? extends BaseDataObject<?>>, Set<Object>>> params = clazzPrimaryIdMap
                .entrySet().stream().map(entry -> {
                    Map<Object, Object> clazzDoMap = doMap.getOrDefault(entry.getKey(), new HashMap<>());
                    Set<Object> lostPrimaryIds = Collect.diff(entry.getValue(), clazzDoMap.keySet());
                    return Pair.of(entry.getKey(), lostPrimaryIds);
                }).filter(pair -> Collect.isNotEmpty(pair.getRight())).collect(Collectors.toSet());

        params.forEach(param -> {
            DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = CommandBus.getDaoWrapper(param.getKey());
            Map<Object, Object> clazzResultMap = daoWrapper.batchGetByPrimaryIdsWrapper(param.getRight());
            doMap.computeIfAbsent(param.getKey(), c -> new HashMap<>()).putAll(clazzResultMap);
        });
        Map<String, Aggregate> resultMap = new HashMap<>();
        aggregateIds.forEach(aggregateId -> {
            DataObjectInfo dataObjectInfo = dataObjectInfo(aggregateId);
            Object dataObject = doMap.getOrDefault(dataObjectInfo.getClazz(), new HashMap<>()).get(dataObjectInfo.getPrimaryId());
            if (dataObject != null) {
                resultMap.put(aggregateId, toAggregate(dataObject));
            }
        });
        return resultMap;
    }
}
