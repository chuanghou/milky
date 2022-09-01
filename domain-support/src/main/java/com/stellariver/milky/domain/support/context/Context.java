package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.AggregateDaoAdapter;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.Getter;
import lombok.NonNull;
import org.checkerframework.checker.nullness.Opt;

import java.util.*;

public class Context{

    private Long invocationId;

    @Getter
    private final Map<Class<?>, Set<Object>> changedAggregateIds = new HashMap<>();

    @Getter
    private final Map<Class<?>, Set<Object>> createdAggregateIds = new HashMap<>();

    @Getter
    private final Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = new HashMap<>();

    @Getter
    private final Map<NameType<?>, Object> parameters = new HashMap<>();

    @Getter
    private final Map<NameType<?>, Object> metaData = new HashMap<>();

    private final Map<NameType<?>, Object> dependencies = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    private final List<Event> finalRouteEvents = new ArrayList<>();

    private final List<MessageRecord> messageRecords = new ArrayList<>();

    public Object getDependency(NameType<?> key) {
        return dependencies.get(key);
    }

    public Map<NameType<?>, Object> getDependencies() {
        return dependencies;
    }

    public void putDependency(NameType<?> key, Object value) {
        dependencies.put(key, value);
    }

    public void clearDependencies() {
        dependencies.clear();
    }

    public void publish(@NonNull Event event) {
        SysException.anyNullThrow(event);
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

    public static Context build(Map<NameType<?>, Object> parameters) {
        Context context = new Context();
        context.invocationId = BeanUtil.getBean(IdBuilder.class).build();
        context.parameters.putAll(parameters);
        context.metaData.putAll(parameters);
        return context;
    }

    public Long getInvocationId() {
        return invocationId;
    }


    public Optional<? extends AggregateRoot> getByAggregateIdOptional(Class<? extends AggregateRoot> clazz, String aggregateId) {
        AggregateDaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(clazz);
        return daoAdapter.getByAggregateIdOptional(aggregateId, this);
    }

    public AggregateRoot getByAggregateId(Class<? extends AggregateRoot> clazz, String aggregateId) {
        AggregateDaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(clazz);
        return daoAdapter.getByAggregateId(aggregateId, this);
    }
}




