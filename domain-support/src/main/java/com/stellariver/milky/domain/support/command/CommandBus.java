package com.stellariver.milky.domain.support.command;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.common.tool.executor.EnhancedExecutor;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Random;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.interceptor.Intercept;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import lombok.CustomLog;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONCURRENCY_VIOLATION;
import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;
import static com.stellariver.milky.domain.support.command.HandlerType.CONSTRUCTOR_HANDLER;
import static com.stellariver.milky.domain.support.command.HandlerType.INSTANCE_HANDLER;

/**
 * @author houchuang
 */
@CustomLog
public class CommandBus {

    private static final Predicate<Method> COMMAND_HANDLER_FORMAT =
            method -> Modifier.isPublic(method.getModifiers())
                    && (!Modifier.isStatic(method.getModifiers()))
                    && method.getParameterTypes().length == 2
                    && Command.class.isAssignableFrom(method.getParameterTypes()[0])
                    && method.getParameterTypes()[1] == Context.class;

    private static final Predicate<Method> CONSTRUCTOR_HANDLER_FORMAT =
            method -> Modifier.isPublic(method.getModifiers())
                    && Modifier.isStatic(method.getModifiers())
                    && method.getParameterTypes().length == 2
                    && Command.class.isAssignableFrom(method.getParameterTypes()[0])
                    && method.getParameterTypes()[1] == Context.class;

    private static final Predicate<Method> COMMAND_INTERCEPTOR_FORMAT =
            method -> Modifier.isPublic(method.getModifiers())
                    && method.getParameterTypes().length == 3
                    && Command.class.isAssignableFrom(method.getParameterTypes()[0])
                    && AggregateRoot.class.isAssignableFrom(method.getParameterTypes()[1])
                    && method.getParameterTypes()[2] == Context.class;


    volatile private static CommandBus instance;

    private static final ThreadLocal<Context> THREAD_LOCAL_CONTEXT = new ThreadLocal<>();

    private final Map<Class<? extends Command>, Map<Class<? extends AggregateRoot>, Handler>> commandHandlers = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, DaoAdapter<?>> daoAdapterMap = new HashMap<>();

    private final Map<Class<? extends BaseDataObject<?>>, DAOWrapper<? extends BaseDataObject<?>, ?>> daoWrappersMap = new HashMap<>();

    private final ListMultimap<Class<? extends Command>, Interceptor> beforeCommandInterceptors
            = MultimapBuilder.hashKeys().arrayListValues().build();

    private final ListMultimap<Class<? extends Command>, Interceptor> afterCommandInterceptors
            = MultimapBuilder.hashKeys().arrayListValues().build();

    private final ConcurrentOperate concurrentOperate;

    private final EventBus eventBus;

    private final MilkyTraceRepository milkyTraceRepository;

    private final EnhancedExecutor enhancedExecutor;

    private final Reflections reflections;

    private final TransactionSupport transactionSupport;

    private final BeanLoader beanLoader;

    private final ThreadLocal<Boolean> memoryTxTL = ThreadLocal.withInitial(() -> false);

    private final Set<Class<? extends AggregateRoot>> aggregateClasses;

    @SuppressWarnings("unused")
    public CommandBus(MilkySupport milkySupport, EventBus eventBus) {

        this.concurrentOperate = milkySupport.getConcurrentOperate();
        this.milkyTraceRepository = milkySupport.getMilkyTraceRepository();
        this.transactionSupport = milkySupport.getTransactionSupport();
        this.enhancedExecutor = milkySupport.getEnhancedExecutor();
        this.eventBus = eventBus;
        this.reflections = milkySupport.getReflections();
        this.beanLoader = milkySupport.getBeanLoader();
        this.aggregateClasses = this.reflections.getSubTypesOf(AggregateRoot.class);
        prepareCommandHandlers();
        prepareRepositories(milkySupport);
        prepareDAOWrappers(milkySupport);
        prepareCommandInterceptors(milkySupport);

        if (null == instance) {
            synchronized (CommandBus.class) {
                if (null == instance) {
                    instance = this;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void prepareCommandInterceptors(MilkySupport milkySupport) {

        ListMultimap<Class<? extends Command>, Interceptor> tempInterceptorsMap =  MultimapBuilder.hashKeys().arrayListValues().build();

        ListMultimap<Class<? extends Command>, Interceptor> finalInterceptorsMap =  MultimapBuilder.hashKeys().arrayListValues().build();

        // collect all command interceptors into tempInterceptorsMap group by commandClass
        milkySupport.getInterceptors().stream()
                .map(Object::getClass).map(Class::getDeclaredMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(Intercept.class))
                .filter(m -> Command.class.isAssignableFrom(m.getParameterTypes()[0]))
                .peek(m -> SysEx.falseThrow(COMMAND_INTERCEPTOR_FORMAT.test(m),
                        CONFIG_ERROR.message(m.toGenericString() + " signature not valid!")))
                .forEach(method -> {
                    Intercept annotation = method.getAnnotation(Intercept.class);
                    Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
                    Object bean = beanLoader.getBean(method.getDeclaringClass());
                    Interceptor interceptor = new Interceptor(bean, method, annotation.pos(), annotation.order());
                    tempInterceptorsMap.get(commandClass).add(interceptor);
                });

        reflections.getSubTypesOf(Command.class).forEach(commandClass -> {
            List<Class<? extends Command>> ancestorClasses = Reflect.ancestorClasses(commandClass)
                    .stream().filter(Command.class::isAssignableFrom).collect(Collectors.toList());
            ancestorClasses.forEach(ancestor -> {
                List<Interceptor> ancestorInterceptors = tempInterceptorsMap.get(ancestor);
                finalInterceptorsMap.get(commandClass).addAll(ancestorInterceptors);
            });
        });

        // divided into before and after
        finalInterceptorsMap.keySet().forEach(commandClass -> {
            List<Interceptor> interceptors = finalInterceptorsMap.get(commandClass);
            List<Interceptor> beforeInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.BEFORE))
                    .sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList());
            beforeCommandInterceptors.putAll(commandClass, beforeInterceptors);
            List<Interceptor> afterInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.AFTER))
                    .sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList());
            afterCommandInterceptors.putAll(commandClass, afterInterceptors);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareRepositories(MilkySupport milkySupport) {
        milkySupport.getDaoAdapters().forEach(bean -> {
            Type[] types = Arrays.stream(bean.getClass().getGenericInterfaces())
                    .map(i -> (ParameterizedType) i)
                    .filter(t -> Objects.equals(t.getRawType(), DaoAdapter.class))
                    .map(ParameterizedType::getActualTypeArguments).findFirst()
                    .orElseThrow(() -> new SysEx(CONFIG_ERROR));
            daoAdapterMap.put((Class<? extends AggregateRoot>) types[0], bean);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareDAOWrappers(MilkySupport milkySupport) {
        milkySupport.getDaoWrappers().forEach(bean -> {
            Type[] types = Arrays.stream(bean.getClass().getGenericInterfaces())
                    .map(i -> (ParameterizedType) i)
                    .filter(t -> Objects.equals(t.getRawType(), DAOWrapper.class))
                    .map(ParameterizedType::getActualTypeArguments).findFirst()
                    .orElseThrow(() -> new SysEx(CONFIG_ERROR));
            daoWrappersMap.put((Class<? extends BaseDataObject<?>>) types[0], bean);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareCommandHandlers() {
        aggregateClasses.forEach(clazz -> {
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(MethodHandler.class))
                    .peek(m -> SysEx.falseThrow(COMMAND_HANDLER_FORMAT.test(m),
                            CONFIG_ERROR.message(m.toGenericString() + " signature not valid!")))
                    .forEach(method -> {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Handler handler = new Handler(clazz, method, INSTANCE_HANDLER);
                        Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
                        Map<Class<? extends AggregateRoot>, Handler> handlerMap = commandHandlers.computeIfAbsent(commandType, c -> new HashMap<>());
                        SysEx.trueThrow(handlerMap.containsKey(clazz),
                                CONFIG_ERROR.message(() -> commandType.getName() + " has two command handlers in the same class ") + clazz.getName());
                        handlerMap.put(clazz, handler);
                    });

            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(ConstructorHandler.class))
                    .peek(m -> SysEx.falseThrow(CONSTRUCTOR_HANDLER_FORMAT.test(m),
                            CONFIG_ERROR.message(m.toGenericString() + " signature not valid!"))).collect(Collectors.toList())
                    .forEach(method -> {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Handler handler = new Handler(clazz, method, CONSTRUCTOR_HANDLER);
                        Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
                        Map<Class<? extends AggregateRoot>, Handler> handlerMap = commandHandlers.computeIfAbsent(commandType, c -> new HashMap<>());
                        SysEx.trueThrow(handlerMap.containsKey(clazz),
                                CONFIG_ERROR.message(() -> commandType.getName() + " has two command handlers in the same class ") + clazz.getName());
                        handlerMap.put(clazz, handler);
                    });

        });
    }

    static public <T extends Command> Object acceptMemoryTransactional(List<T> commands, Map<Class<? extends Typed<?>>, Object> parameters,
                                                                       Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Object result;
        instance.memoryTxTL.set(true);
        SysEx.nullThrow(instance.transactionSupport,
                "transactionSupport is null, so you can't use memory transactional feature, change to CommandBus.accept(command, parameters)!");
        try {
            result = instance.doSend(commands, parameters, aggregateIdMap);
        } finally {
            instance.memoryTxTL.set(false);
        }
        return result;
    }

    static public <T extends Command> Object acceptMemoryTransactional(List<T> commands, Map<Class<? extends Typed<?>>, Object> parameters) {
        return acceptMemoryTransactional(commands, parameters, null);
    }

    static public <T extends Command> Object acceptMemoryTransactional(T command, Map<Class<? extends Typed<?>>, Object> parameters) {
        return acceptMemoryTransactional(Collections.singletonList(command), parameters, null);
    }

    static public <T extends Command> Object accept(T command, Map<Class<? extends Typed<?>>, Object> parameters,
                                                    Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        return instance.doSend(Collections.singletonList(command), parameters, aggregateIdMap);
    }

    static public <T extends Command> Object accept(T command, Map<Class<? extends Typed<?>>, Object> parameters) {
        return instance.doSend(Collections.singletonList(command), parameters, null);
    }

    static public DaoAdapter<? extends AggregateRoot> getDaoAdapter(Class<? extends AggregateRoot> clazz) {
        return instance.daoAdapterMap.get(clazz);
    }

    static public DAOWrapper<? extends BaseDataObject<?>, ?> getDaoWrapper(Class<? extends BaseDataObject<?>> clazz) {
        return instance.daoWrappersMap.get(clazz);
    }

    /**
     * 针对应用层调用的命令总线接口
     *
     * @param commands 外部命令列表
     * @param <T>     命令泛型
     * @return 总结结果
     */
    @SneakyThrows
    private <T extends Command> Object doSend(List<T> commands, Map<Class<? extends Typed<?>>, Object> parameters,
                                              @Nullable Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        List<Object> results;
        Context shouldNull = THREAD_LOCAL_CONTEXT.get();
        SysEx.trueThrow(shouldNull != null, CONFIG_ERROR.message("Inside a event router, should use CommandBus.driveByEvent"));
        Context context = Context.build(parameters, aggregateIdMap);
        THREAD_LOCAL_CONTEXT.set(context);
        Long invocationId = context.getInvocationId();
        InvokeTrace invokeTrace = new InvokeTrace(invocationId, invocationId);
        commands.forEach(c -> c.setInvokeTrace(invokeTrace));
        Boolean memoryTx = Kit.op(memoryTxTL.get()).orElse(false);
        Throwable backup = null;
        try {
            results = commands.stream().map(this::route).collect(Collectors.toList());
            eventBus.preFinalRoute(context.getFinalEvents(), context);
            if (memoryTx) {
                transactionSupport.begin();
            }
            Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
            if (memoryTx) {
                doMap.forEach((dataObjectClazz, map) -> {
                    DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = daoWrappersMap.get(dataObjectClazz);

                    // all three group of primary ids
                    Set<Object> doPrimaryIds = map.keySet();
                    Set<Object> created = context.getCreatedAggregateIds().get(dataObjectClazz);
                    Set<Object> changed = context.getChangedAggregateIds().get(dataObjectClazz);

                    // created and updated primary ids
                    Set<Object> createdPrimaryIds = Collect.inter(doPrimaryIds, created);
                    Set<Object> changedPrimaryIds = Collect.subtract(Collect.inter(doPrimaryIds, changed), createdPrimaryIds);

                    // created and updated data object
                    List<Object> createdDataObjects = createdPrimaryIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
                    List<Object> changedDataObjects = changedPrimaryIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());

                    // persistent layer
                    if (Collect.isNotEmpty(createdDataObjects)) {
                        daoWrapper.batchSaveWrapper(createdDataObjects);
                    }
                    if (Collect.isNotEmpty(changedDataObjects)) {
                        daoWrapper.batchUpdateWrapper(changedDataObjects);
                    }
                });
            }
            eventBus.postFinalRoute(context.getFinalEvents(), context);
            enhancedExecutor.submit(() -> milkyTraceRepository.record(context, true), UUID.randomUUID().toString());
        } catch (Throwable throwable) {
            enhancedExecutor.submit(() -> milkyTraceRepository.record(context, false),  UUID.randomUUID().toString());
            if (memoryTx) {
                transactionSupport.rollback();
            }
            backup = excavate(throwable);
            throw backup;
        } finally {
            try {
                THREAD_LOCAL_CONTEXT.remove();
                Pair<Boolean, Map<String, Result<Void>>> unLockedAll = concurrentOperate.unLockAll();
                if (!unLockedAll.getLeft()) {
                    log.arg0(unLockedAll.getRight()).error("UNLOCK_ALL_FAILURE");
                }
            } catch (Throwable throwable) {
                log.position("THROW_IN_FINALLY").error(throwable.getMessage(), throwable);
                if (backup != null) {
                    throw backup;
                }
            }
        }
        if (memoryTx) {
            transactionSupport.commit();
        }
        return commands.size() == 1 ? results.get(0) : results;
    }

    static private Throwable excavate(Throwable throwable) {
        while (true) {
            Throwable one = doExcavate(throwable);
            if (one == throwable) {
                return one;
            }
        }
    }


    static private Throwable doExcavate(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            return ((InvocationTargetException) throwable).getTargetException();
        } else if (throwable instanceof ExecutionException || throwable instanceof UncheckedExecutionException) {
            return throwable.getCause();
        } else {
            return throwable;
        }
    }

    static public <T extends Command> void driveByEvent(T command, Event sourceEvent) {
        command.setInvokeTrace(InvokeTrace.build(sourceEvent));
        instance.route(command);
    }

    private <T extends Command> Object route(@NonNull T command) {

        Map<Class<? extends AggregateRoot>, Handler> handlerMap = commandHandlers.get(command.getClass());
        SysEx.nullThrow(handlerMap, command.getClass().getSimpleName() + "could not found its handler!");
        boolean eq = Kit.eq(handlerMap.size(), 1);
        SysEx.falseThrow(eq, CONFIG_ERROR.message(
                    command.getClass().getName() + " has at least 2 handlers implementations, please assign aggregate class"));
        Handler commandHandler = handlerMap.values().stream().findFirst().orElseThrow(() -> new SysEx(ErrorEnums.UNREACHABLE_CODE));
        Context context = THREAD_LOCAL_CONTEXT.get();
        // command bus lock and it will be release finally
        UK nameSpace = UK.build(commandHandler.getAggregateClazz());
        String lockKey = command.getAggregateId();
        boolean locked = concurrentOperate.tryReentrantLock(nameSpace, lockKey, command.lockExpireMils());
        if (!locked) {
            long sleepTimeMs = Random.randomRange(command.violationRandomSleepRange());
            RetryParameter retryParameter = RetryParameter.builder()
                    .nameSpace(nameSpace)
                    .lockKey(lockKey)
                    .milsToExpire(command.lockExpireMils())
                    .times(command.retryTimes())
                    .sleepTimeMils(sleepTimeMs)
                    .build();
            locked = concurrentOperate.tryRetryLock(retryParameter);
            BizEx.falseThrow(locked, CONCURRENCY_VIOLATION);
        }
        Object result = doRoute(command, context, commandHandler);
        context.popEvents().forEach(event -> {
            event.setInvokeTrace(InvokeTrace.build(command));
            eventBus.route(event, context);
        });
        return result;
    }

    private <T extends Command> Object doRoute(T command, Context context, Handler commandHandler) {
        DaoAdapter<?> daoAdapter = daoAdapterMap.get(commandHandler.getAggregateClazz());
        SysEx.nullThrow(daoAdapter, commandHandler.getAggregateClazz() + "hasn't corresponding command handler");

        // build command record and record it
        String aggregateId = command.getAggregateId();

        // real command handle procedure
        Object result;
        AggregateRoot aggregate;
        AggregateStatus aggregateStatus = AggregateStatus.KEEP;
        BaseDataObject<?> dataObjectOld = null;
        if (commandHandler.handlerType == CONSTRUCTOR_HANDLER) {
            // before interceptors run, it is corresponding to a create command
            beforeCommandInterceptors.get(command.getClass()).forEach(interceptor -> {
                interceptor.invoke(command, null, context);
                Trail trail = Trail.builder().beanName(interceptor.getClass().getSimpleName()).messages(Collect.asList(command)).build();
                context.record(trail);
            });

            // // run command handlers
            aggregate = (AggregateRoot) commandHandler.invoke(null, command, context);

            // update aggregate status to CREATE
            aggregateStatus = AggregateStatus.CREATE;
            result = aggregate;

        } else if (commandHandler.handlerType == INSTANCE_HANDLER) {

            // from db or context get aggregate
            aggregate = daoAdapter.getByAggregateId(aggregateId, context);
            dataObjectOld = (BaseDataObject<?>) daoAdapter.toDataObjectWrapper(aggregate);

            // run command before interceptors, it is corresponding to a common command, an instance method
            beforeCommandInterceptors.get(command.getClass()).forEach(interceptor -> {
                interceptor.invoke(command, aggregate, context);
                Trail trail = Trail.builder().beanName(interceptor.getClass().getSimpleName()).messages(Collect.asList(command)).build();
                context.record(trail);
            });

            // run command handlers
            result = commandHandler.invoke(aggregate, command, context);
            boolean present = context.peekEvents().stream().anyMatch(Event::aggregateChanged);

            if (present) {
                aggregateStatus = AggregateStatus.UPDATE;
            }
        } else {
            throw new SysEx("unreached part!");
        }

        Trail trail = Trail.builder().beanName(aggregate.getClass().getSimpleName()).messages(Collect.asList(command)).result(result).build();
        context.record(trail);

        // process context cache for aggregate
        DataObjectInfo dataObjectInfo = daoAdapter.dataObjectInfo(aggregateId);
        Class<? extends BaseDataObject<?>> dataObjectClazz = dataObjectInfo.getClazz();
        Object primaryId = dataObjectInfo.getPrimaryId();
        // if aggregateStatus is not KEEP, it means the aggregate has been created or updated
        if (aggregateStatus != AggregateStatus.KEEP) {

            // context DO cache
            Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
            // according primaryId, find corresponding data object
            Object original = Kit.op(doMap.get(dataObjectClazz)).map(map -> map.get(primaryId)).orElse(null);
            // aggregate to data object
            BaseDataObject<?> dataObjectNew = (BaseDataObject<?>) daoAdapter.toDataObjectWrapper(aggregate);

            if (original != null && dataObjectOld != null) {
                for (Accessor accessor : Accessor.resolveAccessors(dataObjectClazz)) {
                    Object oldValue = accessor.getValue(dataObjectOld);
                    Object newValue = accessor.getValue(dataObjectNew);
                    if (Kit.eq(oldValue, newValue)) {
                        accessor.setValue(dataObjectNew, newValue);
                    }
                }
            }
            DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = CommandBus.getDaoWrapper(dataObjectClazz);

            Boolean memoryTx = Kit.op(memoryTxTL.get()).orElse(false);
            if (memoryTx) {
                doMap.computeIfAbsent(dataObjectClazz, k -> new HashMap<>(16)).put(primaryId, dataObjectNew);
            } else {
                Kit.op(doMap.get(dataObjectClazz)).ifPresent(map -> map.remove(primaryId));
            }
            // if memoryTx is true, the created or updated aggregate DO will be saved in cache
            // or else these DO wil save in DB immediately
            if (aggregateStatus == AggregateStatus.CREATE) {
                if (memoryTx) {
                    context.getCreatedAggregateIds().get(dataObjectClazz).add(primaryId);
                } else {
                    daoWrapper.batchSaveWrapper(Collect.asList(dataObjectNew));
                }
            } else {
                if (memoryTx) {
                    context.getChangedAggregateIds().get(dataObjectClazz).add(primaryId);
                } else {
                    daoWrapper.batchUpdateWrapper(Collect.asList(dataObjectNew));
                }
            }
        }

        // after interceptors
        List<Interceptor> interceptors = afterCommandInterceptors.get(command.getClass());
        for (Interceptor interceptor : interceptors) {
            interceptor.invoke(command, aggregate, context);
            trail = Trail.builder().beanName(interceptor.getClass().getSimpleName()).messages(Collections.singletonList(command)).build();
            context.record(trail);
        }

        return result;
    }

    @Data
    static private class Handler {

        public Handler(Class<? extends AggregateRoot> aggregateClazz, Method method, HandlerType handlerType) {
            this.aggregateClazz = aggregateClazz;
            this.method = method;
            this.handlerType = handlerType;
        }

        private Class<? extends AggregateRoot> aggregateClazz;
        private Method method;
        private HandlerType handlerType;

        public Object invoke(AggregateRoot aggregate, Object... params) {
            return Reflect.invoke(method, aggregate, params);
        }

    }


    /**
     * When use @DirtyContext to clear, the instance need to be clear when the application context was close
     */
    @SuppressWarnings("unused")
    public void close() {
        instance = null;
    }

}
