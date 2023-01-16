package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.AggregateDaoAdapter;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author houchuang
 */
public class Context{

    private Long invocationId;

    @Getter
    private final Map<Class<?>, Set<Object>> changedAggregateIds = new HashMap<>();

    @Getter
    private final Map<Class<?>, Set<Object>> createdAggregateIds = new HashMap<>();

    // it is used to store all DO cache
    @Getter
    private final Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = new HashMap<>();

    @Getter
    private final Map<Typed<?>, Object> parameters = new HashMap<>();

    @Getter
    private final Map<Typed<?>, Object> metaData = new HashMap<>();

    private final Map<Typed<?>, Object> dependencies = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    private final List<Event> finalRouteEvents = new ArrayList<>();

    private final List<MessageRecord> messageRecords = new ArrayList<>();

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getDependency(Typed<T> key) {
        return (T) dependencies.get(key);
    }

    public Map<Typed<?>, Object> getDependencies() {
        return dependencies;
    }

    public void putDependency(Typed<?> key, Object value) {
        dependencies.put(key, value);
    }

    public void clearDependencies() {
        dependencies.clear();
    }

    public void publish(@NonNull Event event) {
        events.add(0, event);
    }

    public void recordCommand(@NonNull CommandRecord commandRecord) {
        SysException.anyNullThrow(commandRecord);
        messageRecords.add(commandRecord);
    }

    public void recordEvent(@NonNull EventRecord eventRecord) {
        SysException.anyNullThrow(eventRecord);
        messageRecords.add(eventRecord);
    }

    public List<MessageRecord> getMessageRecords() {
        return messageRecords;
    }

    public List<Event> popEvents() {
        finalRouteEvents.addAll(events);
        List<Event> popEvents = new ArrayList<>(events);
        events.clear();
        return popEvents;
    }

    public List<Event> getFinalRouteEvents() {
        return finalRouteEvents;
    }

    public List<Event> peekEvents() {
        return new ArrayList<>(events);
    }

    public static Context build(Map<Typed<?>, Object> parameters) {
        return build(parameters, null);
    }

    public static Context build(Map<Typed<?>, Object> parameters, Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Context context = new Context();
        context.invocationId = BeanUtil.getBean(IdBuilder.class).build();
        context.parameters.putAll(parameters);
        context.metaData.putAll(parameters);
        Kit.op(aggregateIdMap).orElseGet(HashMap::new).forEach((aggregateClazz, aggregateIdSet) -> {
            AggregateDaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(aggregateClazz);
            daoAdapter.batchGetByAggregateIds(aggregateIdSet, context);
        });
        return context;
    }

    public Long getInvocationId() {
        return invocationId;
    }

    public <Aggregate extends AggregateRoot> Optional<Aggregate> getByAggregateIdOptional(Class<Aggregate> clazz, String aggregateId) {
        Aggregate aggregate = batchGetByAggregateIds(clazz, Collect.asSet(aggregateId)).get(aggregateId);
        return Kit.op(aggregate);
    }

    public <Aggregate extends AggregateRoot> Aggregate getByAggregateId(Class<Aggregate> clazz, String aggregateId) {
        Aggregate aggregate = batchGetByAggregateIds(clazz, Collect.asSet(aggregateId)).get(aggregateId);
        return Kit.op(aggregate).orElseThrow(() -> new SysException(ErrorEnums.AGGREGATE_NOT_EXISTED));
    }

    @SuppressWarnings("unchecked")
    public <Aggregate extends AggregateRoot> Map<String, Aggregate> batchGetByAggregateIds(Class<Aggregate> clazz, Set<String> aggregateIds) {
        AggregateDaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(clazz);
        return (Map<String, Aggregate>) daoAdapter.batchGetByAggregateIds(aggregateIds, this);
    }

}




