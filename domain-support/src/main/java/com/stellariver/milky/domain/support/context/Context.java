package com.stellariver.milky.domain.support.context;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.If;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import com.stellariver.milky.domain.support.base.Record;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.DaoAdapter;
import com.stellariver.milky.domain.support.dependency.Trace;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.domain.support.event.Event;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

/**
 * @author houchuang
 */
public class Context{

    @Getter
    private Long invocationId;

    @Getter
    private final SetMultimap<Class<?>, Object> changedAggregateIds = MultimapBuilder.hashKeys().hashSetValues().build();

    @Getter
    private final SetMultimap<Class<?>, Object> createdAggregateIds = MultimapBuilder.hashKeys().hashSetValues().build();

    // it is used to store all DO cache
    @Getter
    private final Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = new HashMap<>();

    @Getter
    private final Map<Class<? extends Typed<?>>, Object> parameters = new HashMap<>();

    private final Map<Class<? extends Typed<?>>, Object> metaData = new HashMap<>();
    @Getter
    private final List<Trace> traces = new ArrayList<>();

    private final List<Event> events = new ArrayList<>();

    @Getter
    private final List<Event> finalEvents = new ArrayList<>();

    @Getter
    private final List<Record> records = new ArrayList<>();

    public void clearTraces() {
        traces.clear();
    }

    public <T> void addMetaData(Class<? extends Typed<T>> key, T value) {
        boolean contains = metaData.containsKey(key);
        SysEx.trueThrow(contains, ErrorEnums.CONFIG_ERROR.message(key));
        metaData.put(key, value);
    }

    public <T> void replaceMetaData(Class<? extends Typed<T>> key, T value) {
        boolean contains = metaData.containsKey(key);
        SysEx.falseThrow(contains, ErrorEnums.CONFIG_ERROR.message(key));
        metaData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetaData(Class<? extends Typed<T>> key) {
        return (T) metaData.get(key);
    }


    public void publish(@NonNull Event event) {
        events.add(0, event);
    }

    public void record(@NonNull Record record) {
        records.add(record);
    }

    public List<Event> popEvents() {
        finalEvents.addAll(events);
        List<Event> popEvents = new ArrayList<>(events);
        events.clear();
        return popEvents;
    }

    public List<Event> peekEvents() {
        return new ArrayList<>(events);
    }

    public static Context build(Map<Class<? extends Typed<?>>, Object> parameters) {
        return build(parameters, null);
    }

    @SuppressWarnings("all")
    public static Context build(Map<Class<? extends Typed<?>>, Object> parameters, Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Context context = new Context();
        context.invocationId = BeanUtil.getBean(UniqueIdGetter.class).get();
        If.isTrue(parameters != null, () -> context.parameters.putAll(parameters));
        If.isTrue(parameters != null, () -> context.metaData.putAll(parameters));
        Kit.op(aggregateIdMap).orElseGet(HashMap::new).forEach((aggregateClazz, aggregateIdSet) -> {
            DaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(aggregateClazz);
            daoAdapter.batchGetByAggregateIds(aggregateIdSet, context);
        });
        return context;
    }

    public <Aggregate extends AggregateRoot> Optional<Aggregate> getByAggregateIdOptional(Class<Aggregate> clazz, String aggregateId) {
        Aggregate aggregate = batchGetByAggregateIds(clazz, Collect.asSet(aggregateId)).get(aggregateId);
        return Kit.op(aggregate);
    }

    public <Aggregate extends AggregateRoot> Aggregate getByAggregateId(Class<Aggregate> clazz, String aggregateId) {
        Aggregate aggregate = batchGetByAggregateIds(clazz, Collect.asSet(aggregateId)).get(aggregateId);
        return Kit.op(aggregate).orElseThrow(() -> new SysEx(ErrorEnums.AGGREGATE_NOT_EXISTED));
    }

    @SuppressWarnings("unchecked")
    public <Aggregate extends AggregateRoot> Map<String, Aggregate> batchGetByAggregateIds(Class<Aggregate> clazz, Set<String> aggregateIds) {
        DaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(clazz);
        return (Map<String, Aggregate>) daoAdapter.batchGetByAggregateIds(aggregateIds, this);
    }

}




