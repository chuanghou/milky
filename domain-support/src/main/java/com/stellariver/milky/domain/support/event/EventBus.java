package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnum;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.interceptor.BusInterceptor;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.BusInterceptors;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventBus {

    static final private Predicate<Class<?>[]> eventRouterFormat =
            parameterTypes -> (parameterTypes.length == 2
                    && Event.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == Context.class);

    static final private Predicate<Class<?>[]> eventInterceptorFormat =
            parameterTypes -> (parameterTypes.length == 2
                    && Event.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == Context.class);

    private final Map<Class<? extends Event>, List<Router>> routerMap = new HashMap<>();

    private final Map<Class<? extends Event>, List<Interceptor>> beforeEventInterceptors = new HashMap<>();

    private final Map<Class<? extends Event>, List<Interceptor>> afterEventInterceptors = new HashMap<>();

    @SuppressWarnings("unchecked")
    public EventBus(BeanLoader beanLoader, AsyncExecutorService asyncExecutorService) {

        List<EventRouters> beans = beanLoader.getBeansOfType(EventRouters.class);
        List<Method> methods = beans.stream().map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> eventRouterFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(EventRouter.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            EventRouter annotation = method.getAnnotation(EventRouter.class);
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            Router router = Router.builder().bean(bean).method(method)
                    .type(annotation.type()).executorService(asyncExecutorService)
                    .build();
            routerMap.computeIfAbsent(eventClass, eC -> new ArrayList<>()).add(router);
        });

        HashMap<Class<? extends Event>, List<Interceptor>> tempInterceptorsMap = new HashMap<>();
        HashMap<Class<? extends Event>, List<Interceptor>> finalInterceptorsMap = new HashMap<>();

        // collect all command interceptors into tempInterceptorsMap group by commandClass
        beanLoader.getBeansOfType(BusInterceptors.class).stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> eventInterceptorFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(BusInterceptor.class)).collect(Collectors.toList())
                .forEach(method -> {
                    BusInterceptor annotation = method.getAnnotation(BusInterceptor.class);
                    Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                    Object bean = beanLoader.getBean(method.getDeclaringClass());
                    Interceptor interceptor = Interceptor.builder().bean(bean).method(method)
                            .order(annotation.order()).posEnum(annotation.pos()).build();
                    tempInterceptorsMap.computeIfAbsent(eventClass, cC -> new ArrayList<>()).add(interceptor);
                });

        // according to inherited relation to collect final command interceptors map, all ancestor interceptor
        tempInterceptorsMap.forEach((eventClass, tempInterceptors) -> {
            List<Class<? extends Event>> ancestorClasses = Reflect.ancestorClasses(eventClass)
                    .stream().filter(c -> c.isAssignableFrom(Event.class)).collect(Collectors.toList());
            ancestorClasses.forEach(ancestor -> {
                List<Interceptor> ancestorInterceptors = tempInterceptorsMap.get(ancestor);
                finalInterceptorsMap.computeIfAbsent(eventClass, c -> new ArrayList<>()).addAll(ancestorInterceptors);
            });
        });

        // divided into before and after
        finalInterceptorsMap.forEach((commandClass, interceptors) -> {
            List<Interceptor> beforeInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.BEFORE)).collect(Collectors.toList());
            beforeEventInterceptors.put(commandClass, beforeInterceptors);
            List<Interceptor> afterInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.AFTER)).collect(Collectors.toList());
            afterEventInterceptors.put(commandClass, afterInterceptors);
        });

        // internal order
        beforeEventInterceptors.forEach((k, v) ->
                v = v.stream().sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList()));
        afterEventInterceptors.forEach((k, v) ->
                v = v.stream().sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList()));
    }

    public void syncRoute(Event event, Context context) {
        Optional.ofNullable(beforeEventInterceptors.get(event.getClass())).ifPresent(interceptors -> interceptors
                .forEach(interceptor -> Runner.invoke(interceptor.getBean(), interceptor.getMethod(), event, context)));
        Optional.ofNullable(routerMap.get(event.getClass())).orElseGet(ArrayList::new)
                .stream().filter(router -> router.type.equals(TypeEnum.SYNC))
                .forEach(router -> Runner.run(() -> router.route(event, context)));
        Optional.ofNullable(afterEventInterceptors.get(event.getClass())).ifPresent(interceptors -> interceptors
            .forEach(interceptor -> Runner.invoke(interceptor.getBean(), interceptor.getMethod(), event, context)));
    }

    public void asyncRoute(Event event, Context context) {
        Optional.ofNullable(routerMap.get(event.getClass())).orElseGet(ArrayList::new)
                .stream().filter(router -> router.type.equals(TypeEnum.ASYNC))
                .forEach(router -> Runner.run(() -> router.route(event, context)));
    }

    @Data
    @Builder
    @AllArgsConstructor
    static public class Router {

        private final Object bean;

        private final Method method;

        private final TypeEnum type;

        private ExecutorService executorService;

        public void route(Event event, Context context) {
            if (Objects.equals(type, TypeEnum.SYNC)) {
                Runner.invoke(bean, method, context);
            } else if (Objects.equals(type, TypeEnum.ASYNC)){
                executorService.submit(() -> Runner.invoke(bean, method, context));
            } else {
                throw new BizException(ErrorEnum.CONFIG_ERROR.message("only support sync and async invoke"));
            }
        }
    }

}
