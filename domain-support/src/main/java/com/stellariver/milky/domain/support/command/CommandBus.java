package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.Random;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.Typed;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.DependencyKey;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.util.AsyncExecutor;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.interceptor.Intercept;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import com.stellariver.milky.domain.support.dependency.AggregateDaoAdapter;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Data;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONCURRENCY_VIOLATION;
import static com.stellariver.milky.domain.support.ErrorEnums.*;
import static com.stellariver.milky.domain.support.command.HandlerType.*;

/**
 * @author houchuang
 */
@CustomLog
public class CommandBus {

    private static final Predicate<Class<?>[]> FORMAT =
            parameterTypes -> (parameterTypes.length == 2
                    && Command.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == Context.class);

    private static final Predicate<Class<?>[]> COMMAND_INTERCEPTOR_FORMAT =
            parameterTypes -> (parameterTypes.length == 3
                    && Command.class.isAssignableFrom(parameterTypes[0])
                    && AggregateRoot.class.isAssignableFrom(parameterTypes[1])
                    && parameterTypes[2] == Context.class);

    volatile private static CommandBus instance;

    private static final ThreadLocal<Context> THREAD_LOCAL_CONTEXT = ThreadLocal.withInitial(Context::new);

    private final Map<Class<? extends Command>, Handler> commandHandlers = new HashMap<>();

    private final Map<Class<? extends Command>, Map<String, DependencyProvider>> contextValueProviders = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, AggregateDaoAdapter<?>> daoAdapterMap = new HashMap<>();

    private final Map<Class<? extends BaseDataObject<?>>, DAOWrapper<? extends BaseDataObject<?>, ?>> daoWrappersMap = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> beforeCommandInterceptors = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> afterCommandInterceptors = new HashMap<>();

    private final ConcurrentOperate concurrentOperate;

    private final EventBus eventBus;

    private final TraceRepository traceRepository;

    private final AsyncExecutor asyncExecutor;

    private final Reflections reflections;

    private final TransactionSupport transactionSupport;

    private final ThreadLocal<Boolean> memoryTxTL = ThreadLocal.withInitial(() -> false);

    @SuppressWarnings("unused")
    public CommandBus(MilkySupport milkySupport, EventBus eventBus, MilkyConfiguration milkyConfiguration) {

        this.concurrentOperate = milkySupport.getConcurrentOperate();
        this.traceRepository = milkySupport.getTraceRepository();
        this.transactionSupport = milkySupport.getTransactionSupport();
        this.asyncExecutor = milkySupport.getAsyncExecutor();
        this.eventBus = eventBus;
        this.reflections = milkySupport.getReflections();

        prepareCommandHandlers();
        prepareContextValueProviders(milkySupport);
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
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(Intercept.class))
                .filter(m -> Command.class.isAssignableFrom(m.getParameterTypes()[0]))
                .filter(m -> {
                    boolean test = COMMAND_INTERCEPTOR_FORMAT.test(m.getParameterTypes());
                    SysException.falseThrow(test, ErrorEnums.CONFIG_ERROR.message(m.toGenericString()));
                    return test;
                }).collect(Collectors.toList())
                .forEach(method -> {
                    Intercept annotation = method.getAnnotation(Intercept.class);
                    Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
                    Object bean = BeanUtil.getBean(method.getDeclaringClass());
                    Interceptor interceptor = Interceptor.builder().bean(bean).method(method)
                            .order(annotation.order()).posEnum(annotation.pos()).build();
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
                    .filter(t -> Objects.equals(t.getRawType(), AggregateDaoAdapter.class))
                    .map(ParameterizedType::getActualTypeArguments).findFirst()
                    .orElseThrow(() -> new SysException(ErrorEnums.CONFIG_ERROR));
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
                    .orElseThrow(() -> new SysException(ErrorEnums.CONFIG_ERROR));
            daoWrappersMap.put((Class<? extends BaseDataObject<?>>) types[0], bean);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareCommandHandlers() {
        Set<Class<? extends AggregateRoot>> classes = reflections.getSubTypesOf(AggregateRoot.class);
        boolean secondInherited = classes.stream().map(Reflect::ancestorClasses).anyMatch(list -> list.size() > 3);
        SysException.trueThrow(secondInherited, AGGREGATE_INHERITED);
        List<Method> methods = classes.stream().map(Class::getMethods).flatMap(Stream::of)
                .filter(m -> m.isAnnotationPresent(CommandHandler.class))
                .filter(m -> {
                    boolean test = FORMAT.test(m.getParameterTypes());
                    SysException.falseThrow(test, ErrorEnums.CONFIG_ERROR.message(m.toGenericString()));
                    return test;
                }).collect(Collectors.toList());

        methods.forEach(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            CommandHandler annotation = method.getAnnotation(CommandHandler.class);
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) method.getDeclaringClass();
            HandlerType handlerType = Modifier.isStatic(method.getModifiers()) ? STATIC_METHOD : INSTANCE_METHOD;
            if (handlerType == STATIC_METHOD) {
                Class<?> returnType = method.getReturnType();
                SysException.falseThrowGet(returnType != method.getClass(),
                        () -> ErrorEnums.CONFIG_ERROR.message("static Command handler must return corresponding aggregate!"));
            }
            Set<String> requiredKeys = new HashSet<>(Arrays.asList(annotation.dependencies()));
            Handler handler = new Handler(clazz, method, null, handlerType, requiredKeys);
            Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
            SysException.trueThrow(commandHandlers.containsKey(commandType),
                    ErrorEnums.CONFIG_ERROR.message(commandType.getName() + "has two command handlers"));
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });

        List<Constructor<?>> constructors = classes.stream().map(Class::getDeclaredConstructors).flatMap(Stream::of)
                .filter(m -> m.isAnnotationPresent(CommandHandler.class))
                .filter(m -> {
                    boolean test = FORMAT.test(m.getParameterTypes());
                    SysException.falseThrow(test, ErrorEnums.CONFIG_ERROR.message(m.toGenericString()));
                    return test;
                }).collect(Collectors.toList());

        constructors.forEach(constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            CommandHandler annotation = constructor.getAnnotation(CommandHandler.class);
            Set<String> requiredKeys = new HashSet<>(Arrays.asList(annotation.dependencies()));
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) constructor.getDeclaringClass();
            Handler handler = new Handler(clazz, null, constructor, CONSTRUCTOR_METHOD, requiredKeys);
            Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
            SysException.trueThrow(commandHandlers.containsKey(commandType),
                    ErrorEnums.CONFIG_ERROR.message(commandType.getName() + "has two command handlers"));
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareContextValueProviders(MilkySupport milkySupport) {
        Map<Class<? extends Command>, Map<String, DependencyProvider>> tempProviders = new HashMap<>(16);

        List<Method> methods = milkySupport.getDependencyPrepares()
                .stream().map(Object::getClass)
                .flatMap(clazz -> Arrays.stream(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(DependencyKey.class))
                .filter(method -> {
                    boolean test = FORMAT.test(method.getParameterTypes());
                    SysException.falseThrow(test, ErrorEnums.CONFIG_ERROR.message(method.toGenericString()));
                    return test;
                }).collect(Collectors.toList());

        methods.forEach(method -> {
            Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
            DependencyKey annotation = method.getAnnotation(DependencyKey.class);
            String key = annotation.value();
            String[] requiredKeys = annotation.requiredKeys();
            boolean alwaysLog = annotation.alwaysLog();
            Object bean = BeanUtil.getBean(method.getDeclaringClass());
            DependencyProvider dependencyProvider = new DependencyProvider(key, requiredKeys, bean, method, alwaysLog);
            Map<String, DependencyProvider> valueProviderMap = tempProviders.computeIfAbsent(commandClass, cC -> new HashMap<>(16));
            SysException.trueThrow(valueProviderMap.containsKey(key),
                    "对于" + commandClass.getName() + "对于" + key + "提供了两个dependencyProvider");
            valueProviderMap.put(key, dependencyProvider);
        });

        reflections.getSubTypesOf(Command.class).forEach(commandClass -> {
            Map<String, DependencyProvider> map = new HashMap<>(16);
            List<Class<? extends Command>> ancestorClasses = Reflect.ancestorClasses(commandClass);
            ancestorClasses.forEach(c -> map.putAll(Optional.ofNullable(tempProviders.get(c)).orElseGet(HashMap::new)));
            contextValueProviders.put(commandClass, map);
        });
    }

    static public <T extends Command> Object acceptMemoryTransactional(T command, Map<Typed<?>, Object> parameters,
                                                                       Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Object result;
        instance.memoryTxTL.set(true);
        SysException.nullThrow(instance.transactionSupport);
        try {
            result = instance.doSend(command, parameters, aggregateIdMap);
        } finally {
            instance.memoryTxTL.set(false);
        }
        return result;
    }

    @SuppressWarnings("all")
    static public <T extends Command> Object acceptMemoryTransactional(T command, Map<Typed<?>, Object> parameters) {
        return acceptMemoryTransactional(command, parameters, null);
    }

    static public <T extends Command> Object accept(T command, Map<Typed<?>, Object> parameters,
                                                    Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        return instance.doSend(command, parameters, aggregateIdMap);
    }

    static public <T extends Command> Object accept(T command, Map<Typed<?>, Object> parameters) {
        return accept(command, parameters, null);
    }

    static public AggregateDaoAdapter<? extends AggregateRoot> getDaoAdapter(Class<? extends AggregateRoot> clazz) {
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
    private <T extends Command> Object doSend(T command, Map<Typed<?>, Object> parameters, Map<Class<? extends AggregateRoot>, Set<String>> aggregateIdMap) {
        Object result;
        Context context = Context.build(parameters, aggregateIdMap);
        THREAD_LOCAL_CONTEXT.set(context);
        Long invocationId = context.getInvocationId();
        InvokeTrace invokeTrace = new InvokeTrace(invocationId, invocationId);
        command.setInvokeTrace(invokeTrace);
        Boolean memoryTx = Kit.op(memoryTxTL.get()).orElse(false);
        try {
            result = route(command);
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
                    daoWrapper.batchSaveWrapper(createdDataObjects);
                    daoWrapper.batchUpdateWrapper(changedDataObjects);
                });
            }
            eventBus.postFinalRoute(context.getFinalRouteEvents(), context);
            asyncExecutor.submit(() -> traceRepository.record(context, true));
        } catch (Throwable throwable) {
            asyncExecutor.submit(() -> traceRepository.record(context, false));
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

    static public <T extends Command> void driveByEvent(T command, Event sourceEvent) {
        command.setInvokeTrace(InvokeTrace.build(sourceEvent));
        instance.route(command);
    }

    private <T extends Command> Object route(T command) {
        SysException.anyNullThrow(command);
        Handler commandHandler = Kit.op(commandHandlers.get(command.getClass()))
                .orElseThrow(() -> new SysException(HANDLER_NOT_EXIST.message(Json.toJson(command))));
        Context context = THREAD_LOCAL_CONTEXT.get();

        // command bus lock and it will be release finally
        String encryptionKey = UUID.randomUUID().toString();
        UK nameSpace = UK.build(commandHandler.getAggregateClazz());
        String lockKey = command.getAggregateId();
        boolean locked = concurrentOperate.tryReentrantLock(nameSpace, lockKey, encryptionKey, command.lockExpireMils());
        if (!locked) {
            long sleepTimeMs = Random.randomRange(command.violationRandomSleepRange());
            RetryParameter retryParameter = RetryParameter.builder()
                    .nameSpace(nameSpace)
                    .lockKey(lockKey)
                    .encryptionKey(encryptionKey)
                    .milsToExpire(command.lockExpireMils())
                    .times(command.retryTimes())
                    .sleepTimeMils(sleepTimeMs)
                    .build();
            locked = concurrentOperate.tryRetryLock(retryParameter);
            BizException.falseThrow(locked, CONCURRENCY_VIOLATION.message(Json.toJson(command)));
        }
        Object result = doRoute(command, context, commandHandler);
        context.clearDependencies();
        context.popEvents().forEach(event -> {
            event.setInvokeTrace(InvokeTrace.build(command));
            context.recordEvent(EventRecord.builder().message(event).build());
            eventBus.route(event, context);
        });
        return result;
    }


    @SneakyThrows({InstantiationException.class, IllegalAccessException.class, InvocationTargetException.class})
    private  <T extends Command> Object doRoute(T command, Context context, Handler commandHandler) {
        AggregateDaoAdapter<?> daoAdapter = daoAdapterMap.get(commandHandler.getAggregateClazz());
        SysException.nullThrowMessage(daoAdapter, commandHandler.getAggregateClazz() + "hasn't corresponding command handler");

        // invoke dependencies
        Map<String, DependencyProvider> providerMap = Kit.op(contextValueProviders.get(command.getClass())).orElseGet(HashMap::new);
        commandHandler.getRequiredKeys().forEach(key -> invokeDependencyProvider(command, key, context, providerMap, new HashSet<>()));

        // build command record and record it
        CommandRecord commandRecord = CommandRecord.builder().message(command).dependencies(new HashMap<>(context.getDependencies())).build();
        context.recordCommand(commandRecord);
        String aggregateId = command.getAggregateId();

        // real command handle procedure
        Object result;
        AggregateRoot aggregate;
        AggregateStatus aggregateStatus = AggregateStatus.KEEP;
        if (commandHandler.handlerType == CONSTRUCTOR_METHOD) {

            // before interceptors run
            beforeCommandInterceptors.getOrDefault(command.getClass(), new ArrayList<>())
                    .forEach(interceptor -> interceptor.invoke(command, null, context));

            // // run command handlers, it is corresponding to a create command
            aggregate = (AggregateRoot) commandHandler.constructor.newInstance(command, context);

            // update aggregate status to CREATE
            aggregateStatus = AggregateStatus.CREATE;
            result = aggregate;

        } else if (commandHandler.handlerType == STATIC_METHOD){

            // before interceptors run, it is corresponding to a create command
            beforeCommandInterceptors.get(command.getClass())
                    .forEach(interceptor -> interceptor.invoke(command, null, context));

            // // run command handlers
            aggregate = (AggregateRoot) commandHandler.invoke(null, command, context);

            // update aggregate status to CREATE
            aggregateStatus = AggregateStatus.CREATE;
            result = aggregate;
        } else if (commandHandler.handlerType == INSTANCE_METHOD){

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

        // process context cache for aggregate
        DataObjectInfo dataObjectInfo = daoAdapter.dataObjectInfo(aggregateId);
        Class<? extends BaseDataObject<?>> dataObjectClazz = dataObjectInfo.getClazz();
        Object primaryId = dataObjectInfo.getPrimaryId();
        // if aggregateStatus is not KEEP, it means the aggregate has been created or updated
        if (aggregateStatus != AggregateStatus.KEEP) {

            // context DO cache
            Map<Class<? extends BaseDataObject<?>>, Map<Object, Object>> doMap = context.getDoMap();
            // according primaryId, find corresponding data object
            Object temp = Kit.op(doMap.get(dataObjectClazz)).map(map -> map.get(primaryId)).orElse(null);

            // aggregate to data object
            BaseDataObject<?> baseDataObject = (BaseDataObject<?>) daoAdapter.toDataObjectWrapper(aggregate);

            // merge new data object to the old one, the old one is null when the command which is handling is a create command
            DAOWrapper<? extends BaseDataObject<?>, ?> daoWrapper = daoWrappersMap.get(dataObjectClazz);
            BaseDataObject<?> merge = daoWrapper.mergeWrapper(baseDataObject, temp);

            Boolean memoryTx = Kit.op(memoryTxTL.get()).orElse(false);
            if (memoryTx) {
                doMap.computeIfAbsent(dataObjectClazz, k -> new HashMap<>(16)).put(primaryId, merge);
            } else {
                Kit.op(doMap.get(dataObjectClazz)).ifPresent(map -> map.remove(primaryId));
            }
             // if memoryTx is true, the created or updated aggregate DO will be saved in cache
             // or else these DO wil save in DB immediately
            if (aggregateStatus == AggregateStatus.CREATE) {
                if (memoryTx) {
                    context.getCreatedAggregateIds().computeIfAbsent(dataObjectClazz, k -> new HashSet<>()).add(primaryId);
                } else {
                    daoWrapper.batchSaveWrapper(Collect.asList(merge));
                }
            } else {
                if (memoryTx) {
                    context.getChangedAggregateIds().computeIfAbsent(dataObjectClazz, k -> new HashSet<>()).add(primaryId);
                } else {
                    daoWrapper.batchUpdateWrapper(Collect.asList(merge));
                }
            }
        }

        // after interceptors
        List<Interceptor> interceptors = afterCommandInterceptors.getOrDefault(command.getClass(), new ArrayList<>());
        for (Interceptor interceptor : interceptors) {
            interceptor.invoke(command, aggregate, context);
        }

        return result;
    }

    private <T extends Command> void invokeDependencyProvider(T command, String key, Context context,
                                                              Map<String, DependencyProvider> providers,
                                                              Set<String> referKeys) {
        SysException.trueThrow(referKeys.contains(key), "required key:" + key + "circular reference!");
        referKeys.add(key);
        DependencyProvider valueProvider = providers.get(key);
        SysException.nullThrowMessage(valueProvider, "command:" + Json.toJson(command) + ", key:" + key);
        Map<String, Object> stringKeyDependencies = new HashMap<>(16);
        context.getDependencies().forEach((k, v) -> stringKeyDependencies.put(k.getName(), v));
        Arrays.stream(valueProvider.getRequiredKeys())
                .filter(requiredKey -> Objects.equals(null, stringKeyDependencies.get(requiredKey)))
                .forEach(k -> invokeDependencyProvider(command, k, context, providers, referKeys));
        valueProvider.invoke(command, context);
    }

    @Data
    @AllArgsConstructor
    static private class DependencyProvider {

        private String key;

        private String[] requiredKeys;

        private Object bean;

        private Method method;

        private boolean alwaysLog;

        @SneakyThrows(Throwable.class)
        public void invoke(Object object, Context context) {
            Throwable throwable;
            try {
                Runner.invoke(bean, method, object, context);
            } catch (Throwable t) {
                throwable = t;
                if (alwaysLog) {
                    log.arg0(object).log(this.getClass().getSimpleName(), throwable);
                } else {
                    log.arg0(object).logWhenException(this.getClass().getSimpleName(), throwable);
                }
                throw throwable;
            }
        }
    }

    @Data
    @AllArgsConstructor
    static private class Handler {

        private Class<? extends AggregateRoot> aggregateClazz;

        private Method method;

        private Constructor<?> constructor;

        private HandlerType handlerType;

        private Set<String> requiredKeys;

        public Object invoke(AggregateRoot aggregate, Object object, Context context) {
            return Runner.invoke(aggregate, method, object, context);
        }

    }

}
