package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;

/**
 * @author houchuang
 */
public interface DaoAdapter<Aggregate extends AggregateRoot> {

    default Aggregate toAggregateWrapper(Object dataObject) {
        Aggregate aggregate = toAggregate(dataObject);
        List<FieldAccessor> fieldAccessors = FieldAccessor.resolveAccessors(aggregate.getClass());
        for (FieldAccessor fA: fieldAccessors) {
            Object value = fA.get(aggregate);
            if (fA.getStrategy() != null) {
                if (Kit.eq(fA.getReplacer(), value)) {
                    fA.set(aggregate, null);
                }
            }
        }
        return aggregate;
    }
    /**
     * 实现数据库对象到聚合根转化
     * @param dataObject 数据库对象
     * @return 聚合根对象
     */
    Aggregate toAggregate(@NonNull Object dataObject);


    @SuppressWarnings("unchecked")
    default Object toDataObjectWrapper(Object aggregate) {
        Aggregate aggregateRoot = (Aggregate) aggregate;
        List<FieldAccessor> fieldAccessors = FieldAccessor.resolveAccessors(aggregateRoot.getClass());
        for (FieldAccessor fA : fieldAccessors) {
            Object value = fA.get(aggregate);
            if (value != null) {
                if (fA.getStrategy() != null && Kit.eq(fA.getReplacer(), value)) {
                    String message = String.format("%s in %s equal null holder value", fA.getFieldName(), fA.getClassName());
                    throw new SysEx(CONFIG_ERROR.message(message));
                }
            } else {
                if (fA.getStrategy() == null) {
                    String message = String.format("%s in %s is null of class %s, " +
                            "consider an annotation NullReplacer at corresponding field to assign a non null value stand null in database ",
                            fA.getFieldName(), fA.getClassName(), aggregate.getClass());
                    throw new SysEx(CONFIG_ERROR.message(message));
                } else {
                    fA.set(aggregate, fA.getReplacer());
                }
            }
        }
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

    default Map<String, Aggregate> batchGetByAggregateIds(Set<String> aggregateIds, Context context) {
        List<DataObjectInfo> dataObjectInfos = Collect.transfer(aggregateIds, this::dataObjectInfo);
        Map<? extends Class<? extends BaseDataObject<?>>, Set<Object>> clazzPrimaryIdMap =
                Collect.groupSet(dataObjectInfos, DataObjectInfo::getClazz, DataObjectInfo::getPrimaryId);

        Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
        Set<Pair<? extends Class<? extends BaseDataObject<?>>, Set<Object>>> params = clazzPrimaryIdMap
                .entrySet().stream().map(entry -> {
                    Map<Object, Object> clazzDoMap = doMap.getOrDefault(entry.getKey(), new HashMap<>(16));
                    Set<Object> lostPrimaryIds = Collect.diff(entry.getValue(), clazzDoMap.keySet());
                    return Pair.of(entry.getKey(), lostPrimaryIds);
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
                resultMap.put(aggregateId, toAggregateWrapper(dataObject));
            }
        });
        return resultMap;
    }
}
