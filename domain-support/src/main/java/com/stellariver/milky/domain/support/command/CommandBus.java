package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.If;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.Random;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnum;
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
import com.stellariver.milky.domain.support.dependency.DomainRepository;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.stellariver.milky.common.tool.common.ErrorEnumBase.CONCURRENCY_VIOLATION;
import static com.stellariver.milky.domain.support.ErrorEnum.AGGREGATE_INHERITED;
import static com.stellariver.milky.domain.support.ErrorEnum.HANDLER_NOT_EXIST;

public class CommandBus {

    private static final Logger log = Logger.getLogger(CommandBus.class);

    private static final Predicate<Class<?>[]> format =
            parameterTypes -> (parameterTypes.length == 2
                && Command.class.isAssignableFrom(parameterTypes[0])
                && parameterTypes[1] == Context.class);

    private static CommandBus instance;

    private static final ThreadLocal<Context> tLContext = ThreadLocal.withInitial(Context::new);

    private final Map<Class<? extends Command>, Handler> commandHandlers = new HashMap<>();

    private final Map<Class<? extends Command>, Map<String, DependencyProvider>> contextValueProviders = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, Repository> domainRepositoryMap = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> beforeCommandInterceptors = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> afterCommandInterceptors = new HashMap<>();

    private final ConcurrentOperate concurrentOperate;

    private final EventBus eventBus;

    private final boolean enableMq;

    private final TraceRepository traceRepository;

    private final AsyncExecutor asyncExecutor;

    private final Reflections reflections;


    public CommandBus(MilkySupport milkySupport, EventBus eventBus, MilkyConfiguration milkyConfiguration) {

        this.concurrentOperate = milkySupport.getConcurrentOperate();
        this.traceRepository = milkySupport.getTraceRepository();
        this.asyncExecutor = milkySupport.getAsyncExecutor();
        this.eventBus = eventBus;
        this.enableMq = milkyConfiguration.isEnableMq();
        this.reflections = milkySupport.getReflections();

        prepareCommandHandlers();

        prepareContextValueProviders(milkySupport);

        prepareRepositories(milkySupport);

        prepareCommandInterceptors(milkySupport);

        instance = this;

    }

    @SuppressWarnings("unchecked")
    private void prepareCommandInterceptors(MilkySupport milkySupport) {

        HashMap<Class<? extends Command>, List<Interceptor>> tempInterceptorsMap = new HashMap<>();

        HashMap<Class<? extends Command>, List<Interceptor>> finalInterceptorsMap = new HashMap<>();

        // collect all command interceptors into tempInterceptorsMap group by commandClass
        milkySupport.getInterceptors().stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> format.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(Intercept.class)).collect(Collectors.toList())
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
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.BEFORE)).collect(Collectors.toList());
            beforeCommandInterceptors.put(commandClass, beforeInterceptors);
            List<Interceptor> afterInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.AFTER)).collect(Collectors.toList());
            afterCommandInterceptors.put(commandClass, afterInterceptors);
        });

        // internal order
        beforeCommandInterceptors.forEach((k, v) ->
                v = v.stream().sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList()));
        afterCommandInterceptors.forEach((k, v) ->
                v = v.stream().sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private void prepareRepositories(MilkySupport milkySupport) {
        milkySupport.getDomainRepositories().forEach(bean -> {
            Optional<Type> optional = Arrays.stream(bean.getClass().getGenericInterfaces())
                    .map(i -> (ParameterizedType) i)
                    .filter(t -> Objects.equals(t.getRawType(), DomainRepository.class))
                    .map(t -> t.getActualTypeArguments()[0]).findFirst();
            SysException.falseThrow(optional.isPresent(), ErrorEnum.CONFIG_ERROR);
            Class<?> aggregateClazz = (Class<?>) optional.get();
            Class<?> repositoryClazz = bean.getClass();
            Method saveMethod = getMethod(repositoryClazz,"save", aggregateClazz, Context.class);
            Method getMethod = getMethod(repositoryClazz,"getByAggregateId", String.class, Context.class);
            Method updateMethod = getMethod(repositoryClazz,"updateByAggregateId", aggregateClazz, Context.class);
            SysException.anyNullThrow(getMethod, updateMethod);
            Repository repository = new Repository(bean, getMethod, saveMethod, updateMethod);
            domainRepositoryMap.put((Class<? extends AggregateRoot>) aggregateClazz, repository);
        });
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new SysException(ErrorEnum.CONFIG_ERROR);
        }
        return method;
    }

    @SuppressWarnings("unchecked")
    private void prepareCommandHandlers() {
        Set<Class<? extends AggregateRoot>> classes = reflections.getSubTypesOf(AggregateRoot.class);
        boolean secondInherited = classes.stream().map(Reflect::ancestorClasses).anyMatch(list -> list.size() > 3);
        SysException.trueThrow(secondInherited, AGGREGATE_INHERITED);
        List<Method> methods = classes.stream().map(Class::getMethods).flatMap(Stream::of)
                .filter(m -> format.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(CommandHandler.class)).collect(Collectors.toList());

        methods.forEach(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            CommandHandler annotation = method.getAnnotation(CommandHandler.class);
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) method.getDeclaringClass();
            String name = method.getName();
            SysException.trueThrow(!Objects.equals(name, "handle"),
                    ErrorEnum.CONFIG_ERROR.message("command handler's name should be handle, not " + name));
            boolean hasReturn = !method.getReturnType().getName().equals("void");
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            List<String> requiredKeys = Arrays.asList(annotation.dependencies());
            Handler handler = new Handler(clazz, method, null, isStatic, hasReturn, requiredKeys);
            Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
            if (commandHandlers.containsKey(commandType)) {
                throw new SysException(ErrorEnum.CONFIG_ERROR.message(commandType.getName() + "has two command handlers"));
            } else {
                commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
            }
        });

        List<Constructor<?>> constructors = classes.stream().map(Class::getDeclaredConstructors).flatMap(Stream::of)
                .filter(m -> format.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(CommandHandler.class)).collect(Collectors.toList());

        constructors.forEach(constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            CommandHandler annotation = constructor.getAnnotation(CommandHandler.class);
            List<String> requiredKeys = Arrays.asList(annotation.dependencies());
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) constructor.getDeclaringClass();
            Handler handler = new Handler(clazz, null, constructor, false, true, requiredKeys);
            Class<? extends Command> commandType = (Class<? extends Command>) parameterTypes[0];
            if (commandHandlers.containsKey(commandType)) {
                throw new SysException(ErrorEnum.CONFIG_ERROR.message(commandType.getName() + "has two command handlers"));
            } else {
                commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareContextValueProviders(MilkySupport milkySupport) {
        Map<Class<? extends Command>, Map<String, DependencyProvider>> tempProviders = new HashMap<>();

        List<Method> methods = milkySupport.getDependencyPrepares()
                .stream().map(Object::getClass)
                .flatMap(clazz -> Arrays.stream(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(DependencyKey.class))
                .filter(method -> format.test(method.getParameterTypes())).collect(Collectors.toList());

        methods.forEach(method -> {
            Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
            String key = method.getAnnotation(DependencyKey.class).value();
            String[] requiredKeys = method.getAnnotation(DependencyKey.class).requiredKeys();
            Object bean = BeanUtil.getBean(method.getDeclaringClass());
            DependencyProvider dependencyProvider = new DependencyProvider(key, requiredKeys, bean, method);
            Map<String, DependencyProvider> valueProviderMap = tempProviders.computeIfAbsent(commandClass, cC -> new HashMap<>());
            SysException.trueThrow(valueProviderMap.containsKey(key),
                    "对于" + commandClass.getName() + "对于" + key + "提供了两个dependencyProvider");
            valueProviderMap.put(key, dependencyProvider);
        });

        reflections.getSubTypesOf(Command.class).forEach(commandClass -> {
            Map<String, DependencyProvider> map = new HashMap<>();
            List<Class<? extends Command>> ancestorClasses = Reflect.ancestorClasses(commandClass);
            ancestorClasses.forEach(c -> map.putAll(Optional.ofNullable(tempProviders.get(c)).orElseGet(HashMap::new)));
            contextValueProviders.put(commandClass, map);
        });
    }


    static public <T extends Command> Object accept(T command, Map<String, Object> parameters) {
        return instance.doSend(command, parameters);
    }

    /**
     * 针对应用层调用的命令总线接口
     * @param command 外部命令
     * @param <T> 命令泛型
     * @return 总结结果
     */
    private <T extends Command> Object doSend(T command, Map<String, Object> parameters) {
        Object result;
        Context context = Context.fromParameters(parameters);
        tLContext.set(context);
        Long invocationId = context.getInvocationId();
        InvokeTrace invokeTrace = new InvokeTrace(invocationId, invocationId);
        command.setInvokeTrace(invokeTrace);
        try {
            result = route(command);
            context.getProcessedEvents().forEach(event -> eventBus.asyncRoute(event, context));
            List<MessageRecord> messageRecords = context.getMessageRecords();
            asyncExecutor.execute(() -> {
                traceRepository.insert(invocationId, context);
                traceRepository.batchInsert(messageRecords, context);
            });
        } catch (Throwable throwable) {
            log.with("context", Json.toJson(context)).error("", throwable);
            List<MessageRecord> recordedMessages = context.getMessageRecords();
            asyncExecutor.execute(() -> {
                traceRepository.insert(invocationId, context, false);
                traceRepository.batchInsert(recordedMessages, context);
            });
            throw throwable;
        } finally {
            tLContext.remove();
        }
        return result;
    }

    static public <T extends Command> void driveByEvent(T command, Event sourceEvent) {
        command.setInvokeTrace(InvokeTrace.build(sourceEvent));
        instance.route(command);
    }

    private <T extends Command> Object route(T command) {
        SysException.anyNullThrow(command);
        Handler commandHandler= commandHandlers.get(command.getClass());
        SysException.nullThrow(commandHandler, () -> HANDLER_NOT_EXIST.message(Json.toJson(command)));
        Object result = null;
        Context context = tLContext.get();
        String lockKey = command.getClass().getName() + "_" + command.getAggregateId();
        String encryptionKey = UUID.randomUUID().toString();
        try {
            if (concurrentOperate.tryLock(lockKey, encryptionKey, command.lockExpireMils())) {
                result = doRoute(command, context, commandHandler);
            } else if (enableMq && !commandHandler.hasReturn && command.allowAsync()) {
                concurrentOperate.sendOrderly(command);
            } else {
                long sleepTimeMs = Random.randomRange(command.violationRandomSleepRange());
                RetryParameter retryParameter = RetryParameter.builder().lockKey(lockKey)
                        .encryptionKey(encryptionKey)
                        .milsToExpire(command.lockExpireMils())
                        .times(command.retryTimes())
                        .sleepTimeMils(sleepTimeMs)
                        .build();
                boolean retryResult = concurrentOperate.tryRetryLock(retryParameter);
                BizException.falseThrow(retryResult, CONCURRENCY_VIOLATION.message(Json.toJson(command)));
                result = doRoute(command, context, commandHandler);
            }
            tLContext.get().clearDependencies();
        } finally {
            boolean unlock = concurrentOperate.unlock(command.getAggregateId(), encryptionKey);
            SysException.falseThrow(unlock, "unlock " + command.getAggregateId() + " failure!");
        }
        context.popEvents().forEach(event -> {
            event.setInvokeTrace(InvokeTrace.build(command));
            context.recordEvent(EventRecord.builder().message(event).build());
            Runner.run(() -> eventBus.syncRoute(event, tLContext.get()));
        });
        return result;
    }


    @SneakyThrows
    @SuppressWarnings("unchecked")
    private  <T extends Command> Object doRoute(T command, Context context, Handler commandHandler) {
        Repository repository = domainRepositoryMap.get(commandHandler.clazz);
        SysException.anyNullThrow(repository, commandHandler.getClazz() + "hasn't corresponding command handler");
        Map<String, DependencyProvider> providerMap =
                Optional.ofNullable(contextValueProviders.get(command.getClass())).orElseGet(HashMap::new);
        commandHandler.getRequiredKeys().forEach(key ->
                invokeDependencyProvider(command, key, context, providerMap, new HashSet<>()));
        AggregateRoot aggregate;
        Object result = null;
        CommandRecord commandRecord = CommandRecord.builder().message(command).dependencies(context.getDependencies()).build();
        context.recordCommand(commandRecord);
        Optional.ofNullable(beforeCommandInterceptors.get(command.getClass())).ifPresent(interceptors -> interceptors
                .forEach(interceptor -> Runner.invoke(interceptor.getBean(), interceptor.getMethod(), command, context)));
        if (commandHandler.constructor != null) {
            try {
                aggregate = (AggregateRoot) commandHandler.constructor.newInstance(command, context);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                if (e instanceof InvocationTargetException) {
                    throw ((InvocationTargetException) e).getTargetException();
                }
                throw new SysException(e);
            }
            Runner.invoke(repository.bean, repository.saveMethod, aggregate, context);
        } else if (!commandHandler.isStatic){
            Optional<? extends AggregateRoot> optional = (Optional<? extends AggregateRoot>)
                    Runner.invoke(repository.bean, repository.getMethod, command.getAggregateId(), context);
            aggregate = optional.orElseThrow(() -> new SysException("aggregateId: " + command.getAggregateId() + " not exists!"));
            result = Runner.invoke(aggregate, commandHandler.method, command, context);
            boolean present = context.peekEvents().stream().anyMatch(Event::aggregateChanged);
            If.isTrue(present, () -> Runner.invoke(repository.bean, repository.updateMethod, aggregate, context));
        } else {
            aggregate = (AggregateRoot) Runner.invoke(null, commandHandler.method, command, context);
            Runner.invoke(repository.bean, repository.saveMethod, aggregate, context);
        }
        Optional.ofNullable(afterCommandInterceptors.get(command.getClass())).ifPresent(interceptors -> interceptors
                .forEach(interceptor -> Runner.invoke(interceptor.getBean(), interceptor.getMethod(), command, context)));
        return result;
    }

    private <T extends Command> void invokeDependencyProvider(T command, String key, Context context,
                                                              Map<String, DependencyProvider> providers,
                                                              Set<String> referKeys) {
        SysException.trueThrow(referKeys.contains(key), "required key " + key + "circular reference!");
        referKeys.add(key);
        DependencyProvider valueProvider = providers.get(key);
        SysException.nullThrowMessage(valueProvider, "command:" + Json.toJson(command) + ", key" + Json.toJson(key));
        Arrays.stream(valueProvider.getRequiredKeys())
                .filter(requiredKey -> Objects.equals(null, context.peekMetaData(requiredKey)))
                .forEach(k -> invokeDependencyProvider(command, k, context, providers, referKeys));
        Object bean = valueProvider.getBean();
        Method method = valueProvider.getMethod();
        Runner.invoke(bean, method, command, context);
    }

    @Data
    @AllArgsConstructor
    static private class Repository {

        private Object bean;

        private Method getMethod;

        private Method saveMethod;

        private Method updateMethod;

    }

    @Data
    @AllArgsConstructor
    static private class DependencyProvider {

        private String key;

        private String[] requiredKeys;

        private Object bean;

        private Method method;

    }

    @Data
    @AllArgsConstructor
    static private class Handler {

        private Class<? extends AggregateRoot> clazz;

        private Method method;

        private Constructor<?> constructor;

        private boolean isStatic;

        private boolean hasReturn;

        private List<String> requiredKeys;

    }
}
