package com.echobaba.milky.domain.support.command;

import com.echobaba.milky.client.base.BizException;
import com.echobaba.milky.client.base.ErrorCode;
import com.echobaba.milky.common.tool.common.BeanLoader;
import com.echobaba.milky.common.tool.common.ReflectTool;
import com.echobaba.milky.common.tool.util.CollectUtils;
import com.echobaba.milky.common.tool.util.JsonUtils;
import com.echobaba.milky.common.tool.util.RandomUtils;
import com.echobaba.milky.domain.support.ErrorCodeEnum;
import com.echobaba.milky.domain.support.base.AggregateRoot;
import com.echobaba.milky.domain.support.context.Context;
import com.echobaba.milky.domain.support.context.ContextPrepare;
import com.echobaba.milky.domain.support.context.ContextPrepareKey;
import com.echobaba.milky.domain.support.context.RequiredContextKeys;
import com.echobaba.milky.domain.support.depend.ConcurrentLock;
import com.echobaba.milky.domain.support.depend.ConcurrentOperate;
import com.echobaba.milky.domain.support.event.EventBus;
import com.echobaba.milky.domain.support.repository.DomainRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.reflections.Reflections;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandBus {

    static final private Predicate<Class<?>[]> commandContextFormat = parameterTypes -> (parameterTypes.length == 2
            || parameterTypes[0].isAssignableFrom(Command.class) || parameterTypes[1] == Context.class);

    private final Map<Class<? extends Command>, Handler> commandHandlers = new HashMap<>();

    private final Map<Class<? extends Command>, Map<String, ContextValueProvider>> contextValueProviders = new HashMap<>();

    private final Map<Class<? extends AggregateRoot>, Repository> repositories = new HashMap<>();

    @Resource
    private BeanLoader beanLoader;

    @Resource
    private ConcurrentLock concurrentLock;

    @Resource
    private ConcurrentOperate concurrentOperate;

    @Resource
    private EventBus eventBus;


    @PostConstruct
    void init() {

        prepareCommandHandlers(new Reflections());

        prepareContextValueProviders();

        prepareRepositories();
    }

    private void prepareRepositories() {
        List<Object> domainRepositories = beanLoader.getBeansForAnnotation(DomainRepository.class);
        domainRepositories.forEach(repo -> {
            Class<? extends AggregateRoot> clazz = repo.getClass().getAnnotation(DomainRepository.class).value();
            Method saveMethod = getMethod(clazz,"save", clazz, Context.class);
            Method getMethod = getMethod(clazz,"getAggregateId", Long.class);
            Repository repository = new Repository(repo, getMethod, saveMethod);
            repositories.put(clazz, repository);
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

    @SuppressWarnings("unchecked")
    private void prepareCommandHandlers(Reflections reflections) {
        Set<Class<? extends AggregateRoot>> classes = reflections.getSubTypesOf(AggregateRoot.class);
        List<Method> methods = classes.stream().map(Class::getMethods).flatMap(Stream::of)
                .filter(m -> commandContextFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(CommandHandler.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Constructor<?> constructor;
            try {
                constructor = method.getDeclaringClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new BizException(ErrorCode.UNKNOWN, e);
            }
            Class<? extends AggregateRoot> clazz = (Class<? extends AggregateRoot>) method.getDeclaringClass();
            boolean hasReturn = !method.getReturnType().getName().equals("void");
            Handler handler = new Handler(clazz, method, constructor, hasReturn);
            commandHandlers.put((Class<? extends Command>) parameterTypes[0], handler);
        });
    }

    @SuppressWarnings("unchecked")
    private void prepareContextValueProviders() {
        Map<Class<? extends Command>, Map<String, ContextValueProvider>> tempContextValueProviders = new HashMap<>();

        List<Method> methods = beanLoader.getBeansOfType(ContextPrepare.class)
                .stream().map(Object::getClass)
                .flatMap(clazz -> Arrays.stream(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(ContextPrepareKey.class))
                .filter(method -> commandContextFormat.test(method.getParameterTypes())).collect(Collectors.toList());

        methods.forEach(m -> {
            Class<? extends Command> commandClass = (Class<? extends Command>) m.getParameterTypes()[0];
            String key = m.getAnnotation(ContextPrepareKey.class).value();
            String[] requiredKeys = m.getAnnotation(ContextPrepareKey.class).requiredKeys();
            List<?> bean = beanLoader.getBeansOfType(m.getDeclaringClass());
            ContextValueProvider valueProvider = new ContextValueProvider(key, requiredKeys, bean, m);
            Map<String, ContextValueProvider> valueProviderMap = tempContextValueProviders.computeIfAbsent(commandClass, cC -> new HashMap<>());
            BizException.trueThrow(valueProviderMap.containsKey(key),
                   ErrorCodeEnum.CONFIG_ERROR.message("对于" + commandClass.getName() + "对于" + key + "提供了两个contextValueProvider"));
            valueProviderMap.put(key, valueProvider);
        });

        tempContextValueProviders.keySet().forEach(command -> {
            Map<String, ContextValueProvider> map = new HashMap<>();
            List<Class<? extends Command>> commands = getAncestorClasses(command);
            commands.forEach(c -> map.putAll(tempContextValueProviders.get(c)));
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
        return doSend(command, context, commandHandler);
    }

    public <T extends Command> Object doSend(T command, Context context, Handler commandHandler) {
        AggregateRoot aggregate;
        Repository repository = repositories.get(commandHandler.clazz);
        AggregateRoot dbAggregate = (AggregateRoot) ReflectTool.invokeBeanMethod(
                repository.bean, repository.getMethod, command.getAggregationId());
        if (dbAggregate == null) {
            try {
                aggregate = (AggregateRoot) commandHandler.constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new BizException(ErrorCodeEnum.UNKNOWN, e);
            }
            aggregate.setAggregateId(command.getAggregationId());
        } else {
            aggregate = dbAggregate;
        }
        context.setAggregateRoot(aggregate);
        Map<String, ContextValueProvider> providerMap = contextValueProviders.get(command.getClass());
        String[] keys = commandHandler.getMethod().getAnnotation(RequiredContextKeys.class).value();
        Arrays.stream(keys).forEach(key -> invokeContextValueProvider(command, key, context, providerMap));
        Object result = ReflectTool.invokeBeanMethod(aggregate, commandHandler.method, command, context);
        if (CollectUtils.isEmpty(context.events)) {
            return result;
        }
        boolean lockResult;
        try {
            String lockKey = command.getAggregationId();
            lockResult = concurrentLock.tryLock(lockKey, 5);
            if (lockResult) {
                ReflectTool.invokeBeanMethod(repository.bean, repository.saveMethod, aggregate);
            } else if (!commandHandler.hasReturn) {
                concurrentOperate.sendOrderly(command);
            } else {
                long sleepTimeMs = RandomUtils.randomRange(100, 300);
                boolean retryResult = concurrentLock.tryRetryLock(lockKey, 5, 3, sleepTimeMs);
                BizException.falseThrow(retryResult, ErrorCodeEnum.CONCURRENT_OPERATE_LOCK);
                ReflectTool.invokeBeanMethod(repository.bean, repository.saveMethod, aggregate);
            }
        } finally {
            boolean unlock = concurrentLock.unlock(command.getAggregationId());
            BizException.falseThrow(unlock, ErrorCode.THIRD_SERVICE.message("解锁失败"));
        }
        context.events.forEach(event -> eventBus.handler(event, context));
        return result;
    }

    private <T extends Command> void invokeContextValueProvider(T command, String key, Context context, Map<String, ContextValueProvider> providers) {
        ContextValueProvider valueProvider = providers.get(key);
        BizException.nullThrow(valueProvider, ErrorCodeEnum.CONTEXT_VALUE_PROVIDER_NOT_EXIST
                .message("command:" + JsonUtils.toString(command) + ", key" + JsonUtils.toString(key)));
        Arrays.stream(valueProvider.getRequiredKeys())
                .filter(requiredKey -> Objects.equals(null, context.get(requiredKey)))
                .forEach(k -> invokeContextValueProvider(command, k, context, providers));
        Object contextPrepareBean = valueProvider.getContextPrepareBean();
        Method providerMethod = valueProvider.getMethod();
        ReflectTool.invokeBeanMethod(contextPrepareBean, providerMethod, command, context);
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

    }
}
