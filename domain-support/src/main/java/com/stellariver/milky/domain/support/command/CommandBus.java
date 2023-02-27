package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Random;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.Typed;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.util.ThreadLocalTransferableExecutor;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.interceptor.Intercept;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import com.stellariver.milky.domain.support.dependency.DaoAdapter;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import lombok.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONCURRENCY_VIOLATION;
import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;
import static com.stellariver.milky.domain.support.command.HandlerType.*;

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

    private final Map<Class<? extends Command>, List<Interceptor>> beforeCommandInterceptors = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> afterCommandInterceptors = new HashMap<>();

    private final ConcurrentOperate concurrentOperate;

    private final EventBus eventBus;

    private final TraceRepository traceRepository;

    private final ThreadLocalTransferableExecutor threadLocalTransferableExecutor;

    private final Reflections reflections;

    private final TransactionSupport transactionSupport;

    private final BeanLoader beanLoader;

    private final ThreadLocal<Boolean> memoryTxTL = ThreadLocal.withInitial(() -> false);

    @SuppressWarnings("unused")
    public CommandBus(MilkySupport milkySupport, EventBus eventBus, MilkyConfiguration milkyConfiguration) {

        this.concurrentOperate = milkySupport.getConcurrentOperate();
        this.traceRepository = milkySupport.getTraceRepository();
        this.transactionSupport = milkySupport.getTransactionSupport();
        this.threadLocalTransferableExecutor = milkySupport.getThreadLocalTransferableExecutor();
        this.eventBus = eventBus;
        this.reflections = milkySupport.getReflections();
        this.beanLoader = milkySupport.getBeanLoader();
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

        HashMap<Class<? extends Command>, List<Interceptor>> tempInterceptorsMap = new HashMap<>(16);

        HashMap<Class<? extends Command>, List<Interceptor>> finalInterceptorsMap = new HashMap<>(16);

        // collect all command interceptors into tempInterceptorsMap group by commandClass
        milkySupport.getInterceptors().stream()
                .map(Object::getClass).map(Class::getDeclaredMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(Intercept.class))
                .filter(m -> Command.class.isAssignableFrom(m.getParameterTypes()[0]))
                .filter(m -> {
                    SysException.falseThrow(COMMAND_INTERCEPTOR_FORMAT.test(m),
                            CONFIG_ERROR.message(m.toGenericString() + " signature not valid!"));
                    return true;
                }).collect(Collectors.toList())
                .forEach(method -> {
                    Intercept annotation = method.getAnnotation(Intercept.class);
                    Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
                    Object bean = beanLoader.getBean(method.getDeclaringClass());
                    Interceptor interceptor = new Interceptor(bean, method, annotation.pos(), annotation.order());
                    tempInterceptorsMap.computeIfAbsent(commandClass, cC -> new ArrayList<>()).add(interceptor);
                });

        Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);

        commandClasses.forEach(commandClass -> {
            List<Class<? extends Command>> ancestorClasses = Reflect.ancestorClasses(commandClass)
                    .stream().filter(Command.class::isAssignableFrom).collect(Collectors.toList());
            ancestorClasses.forEach(ancestor -> {
                List<Interceptor> ancestorInterceptors = Optional.ofNullable(tempInterceptorsMap.get(ancestor)).orElseGet(ArrayList::new);
                finalInterceptorsMap.computeIfAbsent(commandClass, c -> new ArrayList<>()).addAll(ancestorInterceptors);
            });
        });

        // divided into before and after
        finalInterceptorsMap.forEach((commandClass, interceptors) -> {
            List<Interceptor> beforeInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.BEFORE))
                    .sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList());
            beforeCommandInterceptors.put(commandClass, beforeInterceptors);
            List<Interceptor> afterInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.AFTER))
                    .sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList());
            afterCommandInterceptors.put(commandClass, afterInterceptors);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareRepositories(MilkySupport milkySupport) {
        milkySupport.getDaoAdapters().forEach(bean -> {
            Type[] types = Arrays.stream(bean.getClass().getGenericInterfaces())
                    .map(i -> (ParameterizedType) i)
                    .filter(t -> Objects.equals(t.getRawType(), DaoAdapter.class))
                    .map(ParameterizedType::getActualTypeArguments).findFirst()
                    .orElseThrow(() -> new SysException(CONFIG_ERROR));
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
                    .orElseThrow(() -> new SysException(CONFIG_ERROR));
            daoWrappersMap.put((Class<? extends BaseDataObject<?>>) types[0], bean);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareCommandHandlers() {
        Set<Class<? extends AggregateRoot>> classes = reflections.getSubTypesOf(AggregateRoot.class);
        classes.forEach(clazz -> {
            List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(MethodHandler.class))
                    .filter(m -> {
                        SysException.falseThrow(COMMAND_HANDLER_FORMAT.test(m),
                                CONFIG_ERROR.message(m.toGenericString() + " signature not valid!"));
                        return true;
                    }).collect(Collectors.toList());
            methods.forEach(method -> {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Handler handler = new Handler(clazz, method, INSTANCE_HANDLER);
                Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
                Map<Class<? extends AggregateRoot>, Handler> handlerMap = commandHandlers.computeIfAbsent(commandType, c -> new HashMap<>());
                SysException.trueThrow(handlerMap.containsKey(clazz),
                        CONFIG_ERROR.message(() -> commandType.getName() + " has two command handlers in the same class ") + clazz.getName());
                handlerMap.put(clazz, handler);
            });

            methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(ConstructorHandler.class))
                    .filter(m -> {
                        SysException.falseThrow(CONSTRUCTOR_HANDLER_FORMAT.test(m),
                                CONFIG_ERROR.message(m.toGenericString() + " signature not valid!"));
                        return true;
                    }).collect(Collectors.toList());
            methods.forEach(method -> {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Handler handler = new Handler(clazz, method, CONSTRUCTOR_HANDLER);
                Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
                Map<Class<? extends AggregateRoot>, Handler> handlerMap = commandHandlers.computeIfAbsent(commandType, c -> new HashMap<>());
                SysException.trueThrow(handlerMap.containsKey(clazz),
                        CONFIG_ERROR.message(() -> commandType.getName() + " has two command handlers in the same class ") + clazz.getName());
                handlerMap.put(clazz, handler);
            });

        });
    }

    static public <T extends Command> Object acceptMemoryTransactional(T command, Map<Class<? extends Typed<?>>, Object> parameters,
                                                                       Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Object result;
        instance.memoryTxTL.set(true);
        SysException.nullThrow(instance.transactionSupport,
                "transactionSupport is null, so you can't use memory transactional feature, change to CommandBus.accept(command, parameters)!");
        try {
            result = instance.doSend(command, parameters, null, aggregateIdMap);
        } finally {
            instance.memoryTxTL.set(false);
        }
        return result;
    }

    @SuppressWarnings("all")
    static public <T extends Command> Object acceptMemoryTransactional(T command, Map<Class<? extends Typed<?>>, Object> parameters) {
        return acceptMemoryTransactional(command, parameters, null);
    }

    static public <T extends Command> Object accept(T command, Map<Class<? extends Typed<?>>, Object> parameters,
                                                    @Nullable Class<? extends AggregateRoot> clazz,
                                                    @Nullable Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        return instance.doSend(command, parameters, clazz, aggregateIdMap);
    }

    @SuppressWarnings("unused")
    static public <T extends Command> Object accept(T command, Map<Class<? extends Typed<?>>, Object> parameters,
                                                    Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        return accept(command, parameters, null, aggregateIdMap);
    }


    static public <T extends Command> Object accept(T command, Map<Class<? extends Typed<?>>, Object> parameters) {
        return accept(command, parameters, null, null);
    }

    static public DaoAdapter<? extends AggregateRoot> getDaoAdapter(Class<? extends AggregateRoot> clazz) {
        return instance.daoAdapterMap.get(clazz);
    }

    static public DAOWrapper<? extends BaseDataObject<?>, ?> getDaoWrapper(Class<? extends BaseDataObject<?>> clazz) {
        return instance.daoWrappersMap.get(clazz);
    }

    static public void reset() {
        CommandBus.instance = null;
    }

    /**
     * 针对应用层调用的命令总线接口
     * @param command 外部命令
     * @param <T> 命令泛型
     * @return 总结结果
     */
    private <T extends Command> Object doSend(T command, Map<Class<? extends Typed<?>>, Object> parameters,
                                              @Nullable Class<? extends AggregateRoot> clazz,
                                              @Nullable Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Object result;
        Context shouldNull = THREAD_LOCAL_CONTEXT.get();
        SysException.trueThrowGet(shouldNull != null, () -> CONFIG_ERROR
                .message("Inside a event router, you should use CommandBus.send() or CommandBus.acceptMemoryTransactional()"));
        Context context = Context.build(parameters, aggregateIdMap);
        THREAD_LOCAL_CONTEXT.set(context);
        Long invocationId = context.getInvocationId();
        InvokeTrace invokeTrace = new InvokeTrace(invocationId, invocationId);
        command.setInvokeTrace(invokeTrace);
        Boolean memoryTx = Kit.op(memoryTxTL.get()).orElse(false);
        try {
            result = route(command, clazz);
            eventBus.preFinalRoute(context.getFinalRouteEvents(), context);
            if (memoryTx) {
                transactionSupport.begin();
            }
            Map<Class<?>, Set<Object>> createdAggregateIds = context.getCreatedAggregateIds();
            Map<Class<?>, Set<Object>> changedAggregateIds = context.getChangedAggregateIds();
            Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
            if (memoryTx) {
                doMap.forEach((dataObjectClazz, map) -> {
                    DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = daoWrappersMap.get(dataObjectClazz);

                    // all three group of primary ids
                    Set<Object> doPrimaryIds = map.keySet();
                    Set<Object> created = createdAggregateIds.getOrDefault(dataObjectClazz, new HashSet<>());
                    Set<Object> changed = changedAggregateIds.getOrDefault(dataObjectClazz, new HashSet<>());

                    // created and updated primary ids
                    Set<Object> createdPrimaryIds = Collect.inter(doPrimaryIds, created);
                    Set<Object> changedPrimaryIds = Collect.diff(Collect.inter(doPrimaryIds, changed), createdPrimaryIds);

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
            eventBus.postFinalRoute(context.getFinalRouteEvents(), context);
            threadLocalTransferableExecutor.submit(() -> traceRepository.record(context, true));
        } catch (Throwable throwable) {
            threadLocalTransferableExecutor.submit(() -> traceRepository.record(context, false));
            if (memoryTx) {
                transactionSupport.rollback();
            }
            throw throwable;
        } finally {
            THREAD_LOCAL_CONTEXT.remove();
            boolean b = concurrentOperate.unLockAll();
            if (!b) {
                log.error("UNLOCK_ALL_FAILURE");
            }
        }
        if (memoryTx) {
            transactionSupport.commit();
        }
        return result;
    }

    static public <T extends Command> void driveByEvent(T command, Event sourceEvent,
                                                        @Nullable Class<? extends AggregateRoot> clazz) {
        command.setInvokeTrace(InvokeTrace.build(sourceEvent));
        instance.route(command, clazz);
    }

    static public <T extends Command> void driveByEvent(T command, Event sourceEvent) {
        driveByEvent(command, sourceEvent, null);
    }

    private <T extends Command> Object route(@NonNull T command, @Nullable Class<? extends AggregateRoot> aggregateClazz) {
        Handler commandHandler;
        Map<Class<? extends AggregateRoot>, Handler> handlerMap = commandHandlers.get(command.getClass());
        SysException.nullThrow(handlerMap, command.getClass().getSimpleName() + "could not found its handler!");
        if (aggregateClazz == null) {
            boolean eq = Kit.eq(handlerMap.size(), 1);
            SysException.falseThrow(eq, CONFIG_ERROR.message(
                    command.getClass().getName() + " has at least 2 handlers implementations, please assign aggregate class"));
            commandHandler = handlerMap.values().stream().findFirst().orElseThrow(() -> new SysException(ErrorEnums.UNREACHABLE_CODE));
        } else {
            commandHandler = handlerMap.get(aggregateClazz);
        }
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
            BizException.falseThrow(locked, CONCURRENCY_VIOLATION.message(command));
        }
        Object result = doRoute(command, context, commandHandler);
        context.clearDependencies();
        context.popEvents().forEach(event -> {
            event.setInvokeTrace(InvokeTrace.build(command));
            eventBus.route(event, context);
        });
        return result;
    }

    private  <T extends Command> Object doRoute(T command, Context context, Handler commandHandler) {
        DaoAdapter<?> daoAdapter = daoAdapterMap.get(commandHandler.getAggregateClazz());
        SysException.nullThrow(daoAdapter, commandHandler.getAggregateClazz() + "hasn't corresponding command handler");

        // build command record and record it
        String aggregateId = command.getAggregateId();

        // real command handle procedure
        Object result;
        AggregateRoot aggregate;
        AggregateStatus aggregateStatus = AggregateStatus.KEEP;
        if (commandHandler.handlerType == CONSTRUCTOR_HANDLER){

            // before interceptors run, it is corresponding to a create command
            beforeCommandInterceptors.get(command.getClass())
                    .forEach(interceptor -> {
                        interceptor.invoke(command, null, context);
                        MessageRecord messageRecord = MessageRecord.builder()
                                .beanName(interceptor.getClass().getSimpleName())
                                .message(command)
                                .dependencies(new HashMap<>(context.getDependencies()))
                                .build();
                        context.record(messageRecord);
                        context.clearDependencies();
                    });

            // // run command handlers
            aggregate = (AggregateRoot) commandHandler.invoke(null, command, context);

            // update aggregate status to CREATE
            aggregateStatus = AggregateStatus.CREATE;
            result = aggregate;
        } else if (commandHandler.handlerType == INSTANCE_HANDLER){

            // from db or context get aggregate
            aggregate = daoAdapter.getByAggregateId(aggregateId, context);

            // run command before interceptors, it is corresponding to a common command, an instance method
            beforeCommandInterceptors.getOrDefault(command.getClass(), new ArrayList<>())
                    .forEach(interceptor -> interceptor.invoke(command, aggregate, context));

            // run command handlers
            result = commandHandler.invoke(aggregate, command, context);
            boolean present = context.peekEvents().stream().anyMatch(Event::aggregateChanged);

            if (present) {
                aggregateStatus = AggregateStatus.UPDATE;
            }
        } else {
            throw new SysException("unreached part!");
        }
        MessageRecord messageRecord = MessageRecord.builder()
                .beanName(aggregate.getClass().getSimpleName())
                .message(command)
                .dependencies(new HashMap<>(context.getDependencies()))
                .build();
        context.record(messageRecord);

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
            BaseDataObject<?> dataObject = (BaseDataObject<?>) daoAdapter.toDataObjectWrapper(aggregate);

            // merge new data object to the old one, the old one is null when the command which is handling is a create command
            DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = daoWrappersMap.get(dataObjectClazz);
            BaseDataObject<?> mergeResult = daoWrapper.mergeWrapper(dataObject, original);

            Boolean memoryTx = Kit.op(memoryTxTL.get()).orElse(false);
            if (memoryTx) {
                doMap.computeIfAbsent(dataObjectClazz, k -> new HashMap<>(16)).put(primaryId, mergeResult);
            } else {
                Kit.op(doMap.get(dataObjectClazz)).ifPresent(map -> map.remove(primaryId));
            }
             // if memoryTx is true, the created or updated aggregate DO will be saved in cache
             // or else these DO wil save in DB immediately
            if (aggregateStatus == AggregateStatus.CREATE) {
                if (memoryTx) {
                    context.getCreatedAggregateIds().computeIfAbsent(dataObjectClazz, k -> new HashSet<>()).add(primaryId);
                } else {
                    daoWrapper.batchSaveWrapper(Collect.asList(mergeResult));
                }
            } else {
                if (memoryTx) {
                    context.getChangedAggregateIds().computeIfAbsent(dataObjectClazz, k -> new HashSet<>()).add(primaryId);
                } else {
                    daoWrapper.batchUpdateWrapper(Collect.asList(mergeResult));
                }
            }
        }

        // after interceptors
        List<Interceptor> interceptors = afterCommandInterceptors.getOrDefault(command.getClass(), new ArrayList<>());
        for (Interceptor interceptor : interceptors) {
            interceptor.invoke(command, aggregate, context);
            messageRecord = MessageRecord.builder()
                    .beanName(interceptor.getClass().getSimpleName())
                    .message(command)
                    .dependencies(new HashMap<>(context.getDependencies()))
                    .build();
            context.record(messageRecord);
            context.clearDependencies();
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

}
