package com.stellariver.milky.domain.support.context;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.If;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import com.stellariver.milky.domain.support.base.NotExistedMessage;
import com.stellariver.milky.domain.support.base.Trail;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.DaoAdapter;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.event.PlaceHolderEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

import static com.stellariver.milky.domain.support.ErrorEnums.AGGREGATE_NOT_EXISTED;

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

    @Getter
    private final SetMultimap<Class<?>, Object> deletedAggregateIds = MultimapBuilder.hashKeys().hashSetValues().build();

    // it is used to store all DO cache
    @Getter
    private final Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = new HashMap<>();

    @Getter
    private final Map<Class<? extends Typed<?>>, Object> parameters = new HashMap<>();

    @Getter
    private final Map<Class<? extends Typed<?>>, Object> metaData = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    @Getter
    private final List<Event> finalEvents = new ArrayList<>();

    private final List<Trail> trails = new ArrayList<>();

    @Getter @Setter
    private List<Trail> treeTrails;

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

    public void publishPlaceHolderEvent(@NonNull String aggregateId) {
        publish(new PlaceHolderEvent(aggregateId));
    }

    public void record(@NonNull Trail trail) {
        trails.add(trail);
    }

    public void organizeTrails() {
        Map<Long, List<Trail>> groupTrails =
                trails.stream().collect(Collectors.groupingBy(t -> t.getMessage().getInvokeTrace().getTriggerId()));
        this.treeTrails = groupTrails.get(invocationId);
        this.treeTrails.forEach(trail -> fill(trail, groupTrails));
    }

    public void fill(Trail trail, Map<Long, List<Trail>> groupTrails) {
        List<Trail> trails = groupTrails.get(trail.getMessage().getId());
        if (trails != null) {
            trail.setSubTrails(trails);
            trails.forEach(t -> fill(t, groupTrails));
        }
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

        return Kit.op(aggregate).orElseThrow(() -> {
            NotExistedMessage annotation = clazz.getAnnotation(NotExistedMessage.class);
            if (annotation == null) {
                return new BizEx(AGGREGATE_NOT_EXISTED);
            } else {
                return new BizEx(AGGREGATE_NOT_EXISTED.message(annotation.value()));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <Aggregate extends AggregateRoot> Map<String, Aggregate> batchGetByAggregateIds(Class<Aggregate> clazz, Set<String> aggregateIds) {
        DaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(clazz);
        return (Map<String, Aggregate>) daoAdapter.batchGetByAggregateIds(aggregateIds, this);
    }

}




