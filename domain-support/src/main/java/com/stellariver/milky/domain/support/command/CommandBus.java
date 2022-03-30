package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.util.If;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.Random;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnum;
import com.stellariver.milky.domain.support.invocation.Invocation;
import com.stellariver.milky.domain.support.invocation.InvokeTrace;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.DependencyPrepares;
import com.stellariver.milky.domain.support.context.DependencyKey;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.util.AsyncExecutorService;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.interceptor.BusInterceptor;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.BusInterceptors;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import com.stellariver.milky.domain.support.dependency.DomainRepository;
import com.stellariver.milky.domain.support.dependency.MilkyRepository;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.stellariver.milky.common.tool.common.ErrorEnumBase.CONCURRENCY_VIOLATION;
import static com.stellariver.milky.domain.support.ErrorEnum.AGGREGATE_INHERITED;
import static com.stellariver.milky.domain.support.ErrorEnum.HANDLER_NOT_EXIST;

public class CommandBus {

    private static final Predicate<Class<?>[]> format = parameterTypes -> (parameterTypes.length == 2
            && Command.class.isAssignableFrom(parameterTypes[0]) && parameterTypes[1] == Context.class);

    private static final ThreadLocal<Context> tLContext = ThreadLocal.withInitial(Context::new);

    private final Map<Class<? extends Command>, Handler> commandHandlers = new HashMap<>();

    private final Map<Class<? extends Command>, Map<String, DependencyProvider>> contextValueProviders = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, Repository> domainRepositories = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> beforeCommandInterceptors = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> afterCommandInterceptors = new HashMap<>();

    private ConcurrentOperate concurrentOperate;

    private EventBus eventBus;

    private boolean enableMq;

    private MilkyRepository milkyRepository;

    private AsyncExecutorService asyncExecutorService;

    private String[] scanPackages;

    static public CommandBus builder() {
        return new CommandBus();
    }

    public CommandBus milkySupport(MilkySupport milkySupport) {
        this.eventBus = milkySupport.getEventBus();
        this.concurrentOperate = milkySupport.getConcurrentOperate();
        this.milkyRepository = milkySupport.getMilkyRepository();
        this.asyncExecutorService = milkySupport.getAsyncExecutorService();
        return this;
    }

    public CommandBus configuration(MilkyConfiguration configuration) {
        this.enableMq = configuration.isEnableMq();
        this.scanPackages = configuration.getScanPackages();
        return this;
    }

    public CommandBus init() {

        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(scanPackages).addScanners(new SubTypesScanner());

        Reflections reflections = new Reflections(configuration);

        prepareCommandHandlers(reflections);

        prepareContextValueProviders(reflections);

        prepareRepositories();

        prepareCommandBusInterceptors();

        return this;
    }
    @SuppressWarnings("unchecked")
    private void prepareCommandBusInterceptors() {

        HashMap<Class<? extends Command>, List<Interceptor>> tempInterceptorsMap = new HashMap<>();

        HashMap<Class<? extends Command>, List<Interceptor>> finalInterceptorsMap = new HashMap<>();

        // collect all command interceptors into tempInterceptorsMap group by commandClass
        BeanUtil.getBeansOfType(BusInterceptors.class).stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> format.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(BusInterceptor.class)).collect(Collectors.toList())
                .forEach(method -> {
                    BusInterceptor annotation = method.getAnnotation(BusInterceptor.class);
                    Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
                    Object bean = BeanUtil.getBean(method.getDeclaringClass());
                    Interceptor interceptor = Interceptor.builder().bean(bean).method(method)
                            .order(annotation.order()).posEnum(annotation.pos()).build();
                    tempInterceptorsMap.computeIfAbsent(commandClass, cC -> new ArrayList<>()).add(interceptor);
                });

        // according to inherited relation to collect final command interceptors map, all ancestor interceptor
        tempInterceptorsMap.forEach((commandClass, tempInterceptors) -> {
            List<Class<? extends Command>> ancestorClasses = Reflect.ancestorClasses(commandClass)
                    .stream().filter(c -> c.isAssignableFrom(Command.class)).collect(Collectors.toList());
            ancestorClasses.forEach(ancestor -> {
                List<Interceptor> ancestorInterceptors = tempInterceptorsMap.get(ancestor);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void prepareRepositories() {
        List<DomainRepository> repositories = BeanUtil.getBeansOfType(DomainRepository.class);
        repositories.forEach(bean -> {
            List<Method> methods = Arrays.stream(bean.getClass().getMethods())
                    .filter(m -> Objects.equals(m.getName(), "save"))
                    .filter(m -> m.getParameterTypes().length == 2)
                    .collect(Collectors.toList());
            Method saveMethod = methods.get(0);
            Class<?> aggregateClazz = saveMethod.getParameterTypes()[0];
            Class<?> repositoryClazz = bean.getClass();
            Method getMethod = getMethod(repositoryClazz,"getByAggregateId", String.class);
            Method updateMethod = getMethod(repositoryClazz,"updateByAggregateId", aggregateClazz, Context.class);
            SysException.nullThrow(getMethod, updateMethod);
            Repository repository = new Repository(bean, getMethod, saveMethod, updateMethod);
            domainRepositories.put((Class<? extends AggregateRoot>) aggregateClazz, repository);
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
    private void prepareCommandHandlers(Reflections reflections) {
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
            boolean hasReturn = !method.getReturnType().getName().equals("void");
            List<String> requiredKeys = Arrays.asList(annotation.dependencyKeys());
            Handler handler = new Handler(clazz, method, null, hasReturn, requiredKeys);
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });

        List<Constructor<?>> constructors = classes.stream().map(Class::getDeclaredConstructors).flatMap(Stream::of)
                .filter(m -> format.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(CommandHandler.class)).collect(Collectors.toList());

        constructors.forEach(constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            CommandHandler annotation = constructor.getAnnotation(CommandHandler.class);
            List<String> requiredKeys = Arrays.asList(annotation.dependencyKeys());
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) constructor.getDeclaringClass();
            Handler handler = new Handler(clazz, null, constructor, false, requiredKeys);
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareContextValueProviders(Reflections reflections) {
        Map<Class<? extends Command>, Map<String, DependencyProvider>> tempProviders = new HashMap<>();

        List<Method> methods = BeanUtil.getBeansOfType(DependencyPrepares.class)
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

    /**
     * 针对应用层调用的命令总线接口
     * @param command 外部命令
     * @param <T> 命令泛型
     * @return 总结结果
     */
    public <T extends Command> Object send(T command, Context context, Invocation invocation) {
        Object result;
        tLContext.set(context);
        Long invocationId = invocation.getInvocationId();
        InvokeTrace invokeTrace = new InvokeTrace(invocationId, invocationId);
        command.setInvokeTrace(invokeTrace);
        try {
            result = route(command);
            context.getProcessedEvents().forEach(event -> eventBus.asyncRoute(event, context));
            Map<String, Object> metaData = context.getMetaData();
            List<Message> recordedMessages = context.getRecordedMessages();
            asyncExecutorService.execute(() -> {
                milkyRepository.insert(invocation, metaData);
                milkyRepository.batchInsert(recordedMessages, metaData);
            });
        } finally {
            tLContext.remove();
        }
        return result;
    }
    /**
     * 针对内部事件调用的命令总线接口
     * @param command 命令
     * @param <T> 命令泛型
     * @return 总结结果
     */
    public <T extends Command> Object route(T command) {
        SysException.nullThrow(command);
        Handler commandHandler= commandHandlers.get(command.getClass());
        SysException.nullThrow(commandHandler, HANDLER_NOT_EXIST.message(Json.toJson(command)));
        Object result = null;
        Context context = tLContext.get();
        if (command.getInvokeTrace() == null) {
            command.setInvokeTrace(InvokeTrace.build(context.peekEvent()));
        }
        String lockKey = command.getClass().getName() + "_" + command.getAggregateId();
        context.recordMessage(command);
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
        Event event = context.popEvent();
        while (event != null) {
            event.setInvokeTrace(InvokeTrace.build(event));
            Event finalEvent = event;
            Runner.run(() -> eventBus.syncRoute(finalEvent, tLContext.get()));
            event = context.popEvent();
        }
        return result;
    }


    @SneakyThrows
    @SuppressWarnings("unchecked")
    private  <T extends Command> Object doRoute(T command, Context context, Handler commandHandler) {
        Repository repository = domainRepositories.get(commandHandler.clazz);
        SysException.nullThrow(repository, commandHandler.getClazz() + "hasn't corresponding command handler");
        Map<String, DependencyProvider> providerMap =
                Optional.ofNullable(contextValueProviders.get(command.getClass())).orElseGet(HashMap::new);
        commandHandler.getRequiredKeys().forEach(key ->
                invokeDependencyProvider(command, key, context, providerMap, new HashSet<>()));
        AggregateRoot aggregate;
        Object result = null;
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
        } else {
            Optional<? extends AggregateRoot> optional = (Optional<? extends AggregateRoot>)
                    Runner.invoke(repository.bean, repository.getMethod, command.getAggregateId(), context);
            aggregate = optional.orElseThrow(() -> new SysException("aggregateId: " + command.getAggregateId() + " not exists!"));
            result = Runner.invoke(aggregate, commandHandler.method, command, context);
            boolean present = context.peekEvents().stream().anyMatch(Event::isAggregateChange);
            If.isTrue(present, () -> Runner.invoke(repository.bean, repository.saveMethod, aggregate, context));
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
        SysException.nullThrow(valueProvider, "command:" + Json.toJson(command) + ", key" + Json.toJson(key));
        Arrays.stream(valueProvider.getRequiredKeys())
                .filter(requiredKey -> Objects.equals(null, context.getMetaData(requiredKey)))
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

        private boolean hasReturn;

        private List<String> requiredKeys;

    }
}
