package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.ErrorCodeEnumBase;
import com.stellariver.milky.common.tool.common.InvokeUtil;
import com.stellariver.milky.common.tool.utils.Collect;
import com.stellariver.milky.common.tool.utils.Json;
import com.stellariver.milky.common.tool.utils.Random;
import com.stellariver.milky.domain.support.ErrorCodeEnum;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.PrepareProcessor;
import com.stellariver.milky.domain.support.context.PrepareKey;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.repository.DomainRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    private final Map<Class<? extends Command>, Handler> commandHandlers = new HashMap<>();

    private final Map<Class<? extends Command>, Map<String, ContextValueProvider>> contextValueProviders = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, Repository> domainRepositories = new HashMap<>();

    private final BeanLoader beanLoader;

    private final ConcurrentOperate concurrentOperate;

    private final EventBus eventBus;

    private final boolean enableMq;

    public CommandBus(BeanLoader beanLoader, ConcurrentOperate concurrentOperate,
                      EventBus eventBus, String[] domainPackages, boolean enableMq) {
        this.beanLoader = beanLoader;
        this.concurrentOperate = concurrentOperate;
        this.eventBus = eventBus;
        this.enableMq = enableMq;
        init(domainPackages);
    }


    void init(String[] domainPackages) {

        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(domainPackages)
                .addScanners(new SubTypesScanner());

        Reflections reflections = new Reflections(configuration);

        prepareCommandHandlers(reflections);

        prepareContextValueProviders(reflections);

        prepareRepositories();

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void prepareRepositories() {
        List<DomainRepository> repositories = beanLoader.getBeansOfType(DomainRepository.class);
        repositories.forEach(bean -> {
            List<Method> methods = Arrays.stream(bean.getClass().getMethods()).filter(m -> Objects.equals(m.getName(), "save"))
                    .filter(m -> !m.getParameterTypes()[0].equals(Object.class))
                    .collect(Collectors.toList());
            BizException.trueThrow(methods.size() != 1, ErrorCodeEnum.CONFIG_ERROR);
            Method saveMethod = methods.get(0);
            Class<?> aggregateClazz = saveMethod.getParameterTypes()[0];
            Class<?> repositoryClazz = bean.getClass();
            Method getMethod = getMethod(repositoryClazz,"getByAggregateId", String.class, Context.class);
            BizException.nullThrow(getMethod);
            Repository repository = new Repository(bean, getMethod, saveMethod);
            domainRepositories.put((Class<? extends AggregateRoot>) aggregateClazz, repository);
        });
    }

    private Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new BizException(ErrorCodeEnum.CONFIG_ERROR);
        }
        return method;
    }

    private Method getMethodByName(Class<?> clazz, String methodName) {
        List<Method> methods = Arrays.stream(clazz.getMethods())
                .filter(m -> Objects.equals(m.getName(), methodName)).collect(Collectors.toList());
        BizException.trueThrow(methods.size() != 1, ErrorCodeEnum.CONFIG_ERROR.message("methodName: " + methodName + "不唯一"));
        return methods.get(0);
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
            Constructor<?> constructor;
            try {
                constructor = method.getDeclaringClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new BizException(ErrorCodeEnumBase.CONFIG_ERROR
                        .message("corresponding domain class should include a constructor!"), e);
            }
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) method.getDeclaringClass();
            boolean hasReturn = !method.getReturnType().getName().equals("void");
            List<String> requiredKeys = Arrays.asList(annotation.requiredKeys());
            Handler handler = new Handler(clazz, method, constructor, hasReturn, requiredKeys);
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareContextValueProviders(Reflections reflections) {
        Map<Class<? extends Command>, Map<String, ContextValueProvider>> tempProviders = new HashMap<>();

        List<Method> methods = beanLoader.getBeansOfType(PrepareProcessor.class)
                .stream().map(Object::getClass)
                .flatMap(clazz -> Arrays.stream(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(PrepareKey.class))
                .filter(method -> commandHandlerFormat.test(method.getParameterTypes())).collect(Collectors.toList());

        methods.forEach(m -> {
            Class<? extends Command> commandClass = (Class<? extends Command>) m.getParameterTypes()[0];
            String key = m.getAnnotation(PrepareKey.class).value();
            BizException.trueThrow(key.equals(""), ErrorCodeEnum.CONFIG_ERROR.message("contextPrepare 必须有指定key"));
            String[] requiredKeys = m.getAnnotation(PrepareKey.class).requiredKeys();
            Object bean = beanLoader.getBean(m.getDeclaringClass());
            ContextValueProvider valueProvider = new ContextValueProvider(key, requiredKeys, bean, m);
            Map<String, ContextValueProvider> valueProviderMap = tempProviders.computeIfAbsent(commandClass, cC -> new HashMap<>());
            BizException.trueThrow(valueProviderMap.containsKey(key),
                   ErrorCodeEnum.CONFIG_ERROR.message("对于" + commandClass.getName() + "对于" + key + "提供了两个contextValueProvider"));
            valueProviderMap.put(key, valueProvider);
        });

        reflections.getSubTypesOf(Command.class).forEach(command -> {
            Map<String, ContextValueProvider> map = new HashMap<>();
            List<Class<? extends Command>> commands = getAncestorClasses(command);
            commands.forEach(c -> map.putAll(Optional.ofNullable(tempProviders.get(c)).orElse(new HashMap<>())));
            contextValueProviders.put(command, map);
        });
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends Command>> getAncestorClasses(Class<? extends Command> clazz) {
        List<Class<? extends Command>> classes = new ArrayList<>();
        Class<?> superClazz = clazz;
        while (!Objects.equals(superClazz, Command.class)) {
            classes.add((Class<? extends Command>) superClazz);
            superClazz = superClazz.getSuperclass();
        }
        Collections.reverse(classes);
        return classes;
    }

    public <T extends Command> Object send(T command) {
        return send(command, new Context());
    }

    public <T extends Command> Object send(T command, Context context) {
        BizException.nullThrow(command);
        BizException.nullThrow(command.getAggregationId());
        context = Optional.ofNullable(context).orElse(new Context());
        Handler commandHandler= commandHandlers.get(command.getClass());
        BizException.nullThrow(commandHandler, ErrorCodeEnum.HANDLER_NOT_EXIST);
        Object result = null;
        try {
            String lockKey = command.getClass().getName() + "_" + command.getAggregationId();
            if (concurrentOperate.tryLock(lockKey, 5)) {
                 result = doSend(command, context, commandHandler);
            } else if (enableMq && !commandHandler.hasReturn && command.allowAsync()) {
                concurrentOperate.sendOrderly(command);
            } else {
                long sleepTimeMs = Random.randomRange(100, 300);
                boolean retryResult = concurrentOperate.tryRetryLock(lockKey, 5, 3, sleepTimeMs);
                BizException.falseThrow(retryResult, ErrorCodeEnum.CONCURRENCY_VIOLATION);
                result = doSend(command, context, commandHandler);
            }
        } finally {
            boolean unlock = concurrentOperate.unlock(command.getAggregationId());
            BizException.falseThrow(unlock, ErrorCodeEnumBase.THIRD_SERVICE.message("unlock failure"));
        }
        return result;
    }

    public <T extends Command> Object doSend(T command, Context context, Handler commandHandler) {

        Repository repository = domainRepositories.get(commandHandler.clazz);
        BizException.trueThrow(repository == null, ErrorCodeEnum.CONFIG_ERROR
                .message(commandHandler.getClazz().toString() + "hasn't corresponding command handler"));
        Map<String, ContextValueProvider> providerMap =
                Optional.ofNullable(contextValueProviders.get(command.getClass())).orElse(new HashMap<>());
        commandHandler.getRequiredKeys().forEach(key ->
                invokeContextValueProvider(command, key, context, providerMap, new HashSet<>()));
        AggregateRoot aggregate;
        Object result = null;
        if (commandHandler.constructor != null) {
            try {
                aggregate = (AggregateRoot) commandHandler.constructor.newInstance(command, context);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            aggregate = (AggregateRoot) InvokeUtil.invoke(
                    repository.bean, repository.getMethod, command.getAggregationId(), context);
            BizException.nullThrow(aggregate);
            result = InvokeUtil.invoke(aggregate, commandHandler.method, command, context);
        }
        context.setAggregateRoot(aggregate);
        if (Collect.isEmpty(context.events)) {
            return result;
        }
        InvokeUtil.invoke(repository.bean, repository.saveMethod, aggregate, context);
        context.events.forEach(event -> InvokeUtil.run(() -> eventBus.handler(event, context)));
        context.events.clear();
        return result;
    }

    private <T extends Command> void invokeContextValueProvider(T command, String key, Context context,
                                                                Map<String, ContextValueProvider> providers, Set<String> referKeys) {
        BizException.trueThrow(referKeys.contains(key), ErrorCodeEnum.CONFIG_ERROR.message("required key 循环引用"));
        referKeys.add(key);
        ContextValueProvider valueProvider = providers.get(key);
        BizException.nullThrow(valueProvider, ErrorCodeEnum.CONTEXT_VALUE_PROVIDER_NOT_EXIST
                .message("command:" + Json.toString(command) + ", key" + Json.toString(key)));
        Arrays.stream(valueProvider.getRequiredKeys())
                .filter(requiredKey -> Objects.equals(null, context.get(requiredKey)))
                .forEach(k -> invokeContextValueProvider(command, k, context, providers, referKeys));
        Object contextPrepareBean = valueProvider.getContextPrepareBean();
        Method providerMethod = valueProvider.getMethod();
        InvokeUtil.invoke(contextPrepareBean, providerMethod, command, context);
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
