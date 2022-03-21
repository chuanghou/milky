package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.Random;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnum;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.ContextPrepares;
import com.stellariver.milky.domain.support.context.PrepareKey;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import com.stellariver.milky.domain.support.event.Event;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.interceptor.BusInterceptor;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.BusInterceptors;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import com.stellariver.milky.domain.support.repository.DomainRepository;
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

public class CommandBus {

    static final private Predicate<Class<?>[]> commandHandlerFormat = parameterTypes ->
            (parameterTypes.length == 2
            && Command.class.isAssignableFrom(parameterTypes[0])
            && parameterTypes[1] == Context.class);

    static final private Predicate<Class<?>[]> commandBusInterceptorFormat =
            parameterTypes -> (parameterTypes.length == 2
                    && Command.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == Context.class);

    private final Map<Class<? extends Command>, Handler> commandHandlers = new HashMap<>();

    private final Map<Class<? extends Command>, Map<String, ContextValueProvider>> contextValueProviders = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, Repository> domainRepositories = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> beforeCommandInterceptors = new HashMap<>();

    private final Map<Class<? extends Command>, List<Interceptor>> afterCommandInterceptors = new HashMap<>();

    private final BeanLoader beanLoader;

    private final ConcurrentOperate concurrentOperate;

    private final EventBus eventBus;

    private final boolean enableMq;

    static public final ThreadLocal<List<Event>> threadLocalEvents = ThreadLocal.withInitial(ArrayList::new);

    public CommandBus(BeanLoader beanLoader, ConcurrentOperate concurrentOperate,
                      EventBus eventBus, String[] scanPackages, boolean enableMq) {
        this.beanLoader = beanLoader;
        this.concurrentOperate = concurrentOperate;
        this.eventBus = eventBus;
        this.enableMq = enableMq;
        init(scanPackages);
    }


    void init(String[] scanPackages) {

        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(scanPackages)
                .addScanners(new SubTypesScanner());

        Reflections reflections = new Reflections(configuration);

        prepareCommandHandlers(reflections);

        prepareContextValueProviders(reflections);

        prepareRepositories();

        prepareCommandBusInterceptors();

    }
    @SuppressWarnings("unchecked")
    private void prepareCommandBusInterceptors() {

        HashMap<Class<? extends Command>, List<Interceptor>> tempInterceptorsMap = new HashMap<>();
        HashMap<Class<? extends Command>, List<Interceptor>> finalInterceptorsMap = new HashMap<>();

        // collect all command interceptors into tempInterceptorsMap group by commandClass
        beanLoader.getBeansOfType(BusInterceptors.class).stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> commandBusInterceptorFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(BusInterceptor.class)).collect(Collectors.toList())
                .forEach(method -> {
                    BusInterceptor annotation = method.getAnnotation(BusInterceptor.class);
                    Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
                    Object bean = beanLoader.getBean(method.getDeclaringClass());
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
        List<DomainRepository> repositories = beanLoader.getBeansOfType(DomainRepository.class);
        repositories.forEach(bean -> {
            List<Method> methods = Arrays.stream(bean.getClass().getMethods()).filter(m -> Objects.equals(m.getName(), "save"))
                    .filter(m -> !m.getParameterTypes()[0].equals(Object.class))
                    .collect(Collectors.toList());
            Method saveMethod = methods.get(0);
            Class<?> aggregateClazz = saveMethod.getParameterTypes()[0];
            Class<?> repositoryClazz = bean.getClass();
            Method getMethod = getMethod(repositoryClazz,"getByAggregateId", String.class, Context.class);
            BizException.nullThrow(getMethod);
            Repository repository = new Repository(bean, getMethod, saveMethod);
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
        List<Method> methods = classes.stream().map(Class::getMethods).flatMap(Stream::of)
                .filter(m -> commandHandlerFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(CommandHandler.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            CommandHandler annotation = method.getAnnotation(CommandHandler.class);
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) method.getDeclaringClass();
            boolean hasReturn = !method.getReturnType().getName().equals("void");
            List<String> requiredKeys = Arrays.asList(annotation.requiredKeys());
            Handler handler = new Handler(clazz, method, null, hasReturn, requiredKeys);
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });
        List<Constructor<?>> constructors = classes.stream().map(Class::getDeclaredConstructors).flatMap(Stream::of)
                .filter(m -> commandHandlerFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(CommandHandler.class)).collect(Collectors.toList());

        constructors.forEach(constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            CommandHandler annotation = constructor.getAnnotation(CommandHandler.class);
            List<String> requiredKeys = Arrays.asList(annotation.requiredKeys());
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) constructor.getDeclaringClass();
            Handler handler = new Handler(clazz, null, constructor, false, requiredKeys);
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareContextValueProviders(Reflections reflections) {
        Map<Class<? extends Command>, Map<String, ContextValueProvider>> tempProviders = new HashMap<>();

        List<Method> methods = beanLoader.getBeansOfType(ContextPrepares.class)
                .stream().map(Object::getClass)
                .flatMap(clazz -> Arrays.stream(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(PrepareKey.class))
                .filter(method -> commandHandlerFormat.test(method.getParameterTypes())).collect(Collectors.toList());

        methods.forEach(method -> {
            Class<? extends Command> commandClass = (Class<? extends Command>) method.getParameterTypes()[0];
            String key = method.getAnnotation(PrepareKey.class).value();
            String[] requiredKeys = method.getAnnotation(PrepareKey.class).requiredKeys();
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            ContextValueProvider valueProvider = new ContextValueProvider(key, requiredKeys, bean, method);
            Map<String, ContextValueProvider> valueProviderMap = tempProviders.computeIfAbsent(commandClass, cC -> new HashMap<>());
            SysException.trueThrow(valueProviderMap.containsKey(key),
                    "对于" + commandClass.getName() + "对于" + key + "提供了两个contextValueProvider");
            valueProviderMap.put(key, valueProvider);
        });

        reflections.getSubTypesOf(Command.class).forEach(commandClass -> {
            Map<String, ContextValueProvider> map = new HashMap<>();
            List<Class<? extends Command>> ancestorClasses = Reflect.ancestorClasses(commandClass);
            ancestorClasses.forEach(c -> map.putAll(Optional.ofNullable(tempProviders.get(c)).orElseGet(HashMap::new)));
            contextValueProviders.put(commandClass, map);
        });
    }

    public <T extends Command> Object send(T command) {
        SysException.nullThrow(command);
        Context context = new Context();

        Handler commandHandler= commandHandlers.get(command.getClass());
        SysException.nullThrow(commandHandler, ErrorEnum.HANDLER_NOT_EXIST.message(Json.toJson(command)));
        Object result = null;
        try {
            String lockKey = command.getClass().getName() + "_" + command.getAggregationId();
            if (concurrentOperate.tryLock(lockKey, command.lockExpireSeconds())) {
                 result = doSend(command, context, commandHandler);
            } else if (enableMq && !commandHandler.hasReturn && command.allowAsync()) {
                concurrentOperate.sendOrderly(command);
            } else {
                long sleepTimeMs = Random.randomRange(command.violationRandomSleepRange()[0], command.violationRandomSleepRange()[1]);
                boolean retryResult = concurrentOperate.tryRetryLock(lockKey, command.lockExpireSeconds(), command.retryTimes(), sleepTimeMs);
                BizException.falseThrow(retryResult, () -> ErrorEnum.CONCURRENCY_VIOLATION.message(Json.toJson(command)));
                result = doSend(command, context, commandHandler);
            }
        } finally {
            boolean unlock = concurrentOperate.unlock(command.getAggregationId());
            SysException.falseThrow(unlock, "unlock " + command.getAggregationId() + " failure!");
        }
        threadLocalEvents.get().forEach(event -> Runner.run(() -> eventBus.commitRoute(event)));
        threadLocalEvents.get().clear();
        return result;
    }

    @SneakyThrows
    private  <T extends Command> Object doSend(T command, Context context, Handler commandHandler) {
        Repository repository = domainRepositories.get(commandHandler.clazz);
        SysException.nullThrow(repository, commandHandler.getClazz() + "hasn't corresponding command handler");
        Map<String, ContextValueProvider> providerMap =
                Optional.ofNullable(contextValueProviders.get(command.getClass())).orElse(new HashMap<>());
        commandHandler.getRequiredKeys().forEach(key ->
                invokeContextValueProvider(command, key, context, providerMap, new HashSet<>()));
        AggregateRoot aggregate;
        Object result = null;
        Optional.ofNullable(beforeCommandInterceptors.get(command.getClass())).orElseGet(ArrayList::new)
                .forEach(interceptor -> Runner.invoke(interceptor.getBean(), interceptor.getMethod(), command, context));
        if (commandHandler.constructor != null) {
            try {
                aggregate = (AggregateRoot) commandHandler.constructor.newInstance(command, context);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                if (e instanceof InvocationTargetException) {
                    throw ((InvocationTargetException) e).getTargetException();
                }
                throw new SysException(e);
            }
        } else {
            aggregate = (AggregateRoot) Runner.invoke(
                    repository.bean, repository.getMethod, command.getAggregationId(), context);
            result = Runner.invoke(aggregate, commandHandler.method, command, context);
        }
        Runner.invoke(repository.bean, repository.saveMethod, aggregate, context);
        Optional.ofNullable(afterCommandInterceptors.get(command.getClass())).orElseGet(ArrayList::new)
                .forEach(interceptor -> Runner.invoke(interceptor.getBean(), interceptor.getMethod(), command, context));
        context.getEvents().forEach(event -> Runner.run(() -> eventBus.route(event)));
        return result;
    }

    private <T extends Command> void invokeContextValueProvider(T command, String key, Context context,
                                                                Map<String, ContextValueProvider> providers, Set<String> referKeys) {
        SysException.trueThrow(referKeys.contains(key), "required key " + key + "circular reference!");
        referKeys.add(key);
        ContextValueProvider valueProvider = providers.get(key);
        SysException.nullThrow(valueProvider, "command:" + Json.toJson(command) + ", key" + Json.toJson(key));
        Arrays.stream(valueProvider.getRequiredKeys())
                .filter(requiredKey -> Objects.equals(null, context.get(requiredKey)))
                .forEach(k -> invokeContextValueProvider(command, k, context, providers, referKeys));
        Object contextPrepareBean = valueProvider.getContextPrepareBean();
        Method providerMethod = valueProvider.getMethod();
        Runner.invoke(contextPrepareBean, providerMethod, command, context);
    }

    @Data
    @AllArgsConstructor
    static private class Repository {

        private Object bean;

        private Method getMethod;

        private Method saveMethod;

    }

    @Data
    @AllArgsConstructor
    static private class ContextValueProvider {

        private String key;

        private String[] requiredKeys;

        private Object contextPrepareBean;

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
