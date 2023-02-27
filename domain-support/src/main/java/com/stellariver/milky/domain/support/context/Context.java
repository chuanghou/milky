package com.stellariver.milky.domain.support.context;


import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.slambda.BiSFunction;
import com.stellariver.milky.common.tool.slambda.SLambda;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.DaoAdapter;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.stellariver.milky.domain.support.ErrorEnums.REPEAT_DEPENDENCY_KEY;

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
    private final Map<Class<? extends Typed<?>>, Object> parameters = new HashMap<>();

    private final Map<Class<? extends Typed<?>>, Object> metaData = new HashMap<>();

    private final Map<Class<? extends Typed<?>>, Object> dependencies = new HashMap<>();

    private final List<Event> events = new ArrayList<>();

    private final List<Event> finalEvents = new ArrayList<>();

    private final List<Record> records = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <T> T getDependency(Class<? extends Typed<T>> key) {
        return (T) dependencies.get(key);
    }

    public Map<Class<? extends Typed<?>>, Object> getDependencies() {
        return new HashMap<>(dependencies);
    }

    public void clearDependencies() {
        dependencies.clear();
    }


    public void addMetaData(Class<? extends Typed<?>> key, Object value) {
        boolean contains = metaData.containsKey(key);
        SysException.trueThrow(contains, ErrorEnums.CONFIG_ERROR);
        metaData.put(key, value);
    }

    public void replaceMetaData(Class<? extends Typed<?>> key, Object value) {
        boolean contains = metaData.containsKey(key);
        SysException.falseThrow(contains, ErrorEnums.CONFIG_ERROR);
        metaData.put(key, value);
    }

    static private final Map<Pair<Object, Class<? extends Typed<?>>>, Object> proxies = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private  <T> T proxy(T t, Class<? extends Typed<?>> key) {
         return (T) Proxy.newProxyInstance(t.getClass().getClassLoader(), t.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        Object result = Reflect.invoke(method, t, args);
                        SysException.trueThrow(dependencies.containsKey(key), REPEAT_DEPENDENCY_KEY.message(key));
                        dependencies.put(key, result);
                        return result;
                    });

    }

    static private final Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T, U, R> R invoke(BiSFunction<T, U, R> function, U u, Class<? extends Typed<?>> key) {
        Class<?> fClass = function.getClass();
        T proxyInstance = (T) map.get(fClass);
        if (proxyInstance == null) {
            Class<?> instantiatedClass = SLambda.extract(function).getInstantiatedClass();
            T t = (T) BeanUtil.getBean(instantiatedClass);
            proxyInstance = (T) Proxy.newProxyInstance(t.getClass().getClassLoader(), t.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        Object result = Reflect.invoke(method, t, args);
                        SysException.trueThrow(dependencies.containsKey(key), REPEAT_DEPENDENCY_KEY.message(key));
                        dependencies.put(key, result);
                        return result;
                    });
            map.put(fClass, proxyInstance);
        }
        return function.apply(proxyInstance, u);
    }

    public void publish(@NonNull Event event) {
        events.add(0, event);
    }

    public void record(@NonNull Record record) {
        records.add(record);
    }

    public List<Record> getRecords() {
        return records;
    }

    public List<Event> popEvents() {
        finalEvents.addAll(events);
        List<Event> popEvents = new ArrayList<>(events);
        events.clear();
        return popEvents;
    }

    public List<Event> getFinalEvents() {
        return finalEvents;
    }

    public List<Event> peekEvents() {
        return new ArrayList<>(events);
    }

    public static Context build(Map<Class<? extends Typed<?>>, Object> parameters) {
        return build(parameters, null);
    }

    public static Context build(Map<Class<? extends Typed<?>>, Object> parameters, Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Context context = new Context();
        context.invocationId = BeanUtil.getBean(IdBuilder.class).get("default");
        context.parameters.putAll(parameters);
        context.metaData.putAll(parameters);
        Kit.op(aggregateIdMap).orElseGet(HashMap::new).forEach((aggregateClazz, aggregateIdSet) -> {
            DaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(aggregateClazz);
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
        DaoAdapter<? extends AggregateRoot> daoAdapter = CommandBus.getDaoAdapter(clazz);
        return (Map<String, Aggregate>) daoAdapter.batchGetByAggregateIds(aggregateIds, this);
    }

}




