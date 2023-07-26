package com.stellariver.milky.domain.support.dependency;

import com.google.common.collect.SetMultimap;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
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

/**
 * @author houchuang
 */
public interface DaoAdapter<Aggregate extends AggregateRoot> {

    /**
     * 实现数据库对象到聚合根转化
     * @param dataObject 数据库对象
     * @return 聚合根对象
     */
    Aggregate toAggregate(@NonNull Object dataObject);

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
                .orElseThrow(() -> new SysEx(ErrorEnums.AGGREGATE_NOT_EXISTED.message(aggregateId)));
    }

    Map<Object, Object> map = new HashMap<>();

    default Map<String, Aggregate> batchGetByAggregateIds(Set<String> aggregateIds, Context context) {
        List<DataObjectInfo> dataObjectInfos = Collect.transfer(aggregateIds, this::dataObjectInfo);
        SetMultimap<Class<? extends BaseDataObject<?>>, Object> clazzPrimaryIdMap =
                dataObjectInfos.stream().collect(Collect.setMultiMap(DataObjectInfo::getClazz, DataObjectInfo::getPrimaryId));

        Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
        Set<Pair<? extends Class<? extends BaseDataObject<?>>, Set<Object>>> params = clazzPrimaryIdMap.keySet().stream().map(c -> {
            Map<Object, Object> clazzDoMap = doMap.getOrDefault(c, map);
            Set<Object> lostPrimaryIds = Collect.subtract(clazzPrimaryIdMap.get(c), clazzDoMap.keySet());
            return Pair.of(c, lostPrimaryIds);
        }).filter(pair -> Collect.isNotEmpty(pair.getRight())).collect(Collectors.toSet());
        params.forEach(param -> {
            DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = CommandBus.getDaoWrapper(param.getKey());
            Map<Object, Object> clazzResultMap = daoWrapper.batchGetByPrimaryIdsWrapper(param.getRight());
            doMap.computeIfAbsent(param.getKey(), c -> new HashMap<>(16)).putAll(clazzResultMap);
        });
        Map<String, Aggregate> resultMap = new HashMap<>(16);
        aggregateIds.forEach(aggregateId -> {
            DataObjectInfo dataObjectInfo = dataObjectInfo(aggregateId);
            Object dataObject = doMap.getOrDefault(dataObjectInfo.getClazz(), new HashMap<>(10)).get(dataObjectInfo.getPrimaryId());
            if (dataObject != null) {
                resultMap.put(aggregateId, toAggregate(dataObject));
            }
        });
        return resultMap;
    }
}
