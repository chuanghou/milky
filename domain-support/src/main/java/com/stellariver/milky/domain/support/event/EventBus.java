package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.interceptor.Intercept;
import com.stellariver.milky.domain.support.interceptor.Interceptor;
import com.stellariver.milky.domain.support.interceptor.PosEnum;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventBus {

    static final private Predicate<Method> singleEventFormat = method -> {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 2
                && Event.class.isAssignableFrom(parameterTypes[0])
                && parameterTypes[1] == Context.class;
    };

    static final private Predicate<Method> batchEventFormat = method -> {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean parametersMatch = parameterTypes.length == 2 &&
                List.class.isAssignableFrom(parameterTypes[0]) && Context.class.isAssignableFrom(parameterTypes[1]);
        if (!parametersMatch) {
            return false;
        }
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Type actualTypeArgument = ((ParameterizedType) genericParameterTypes[0]).getActualTypeArguments()[0];
        return actualTypeArgument instanceof Class<?> && Event.class.isAssignableFrom(parameterTypes[1]);
    };

    private final Map<Class<? extends Event>, List<Router>> singleEventRouterMap = new HashMap<>();

    private List<BatchRouter<Class<? extends Event>>> batchRouters;

    private final Map<Class<? extends Event>, List<Interceptor>> beforeEventInterceptors = new HashMap<>();

    private final Map<Class<? extends Event>, List<Interceptor>> afterEventInterceptors = new HashMap<>();

    @SuppressWarnings("unchecked")
    public EventBus(MilkySupport milkySupport) {

        List<Method> methods = milkySupport.getEventRouters().stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(singleEventFormat)
                .filter(m -> m.isAnnotationPresent(EventRouter.class))
                .collect(Collectors.toList());
        Map<Class<? extends Event>, List<Router>> tempRouterMap = new HashMap<>();
        methods.forEach(method -> {
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = BeanUtil.getBean(method.getDeclaringClass());
            Router router = Router.builder().bean(bean).method(method)
                    .executorService(milkySupport.getAsyncExecutor())
                    .build();
            tempRouterMap.computeIfAbsent(eventClass, clazz -> new ArrayList<>()).add(router);
        });

        Reflections reflections = milkySupport.getReflections();
        Set<Class<? extends Event>> eventClasses = reflections.getSubTypesOf(Event.class);
        eventClasses.forEach(eventClass -> Reflect.ancestorClasses(eventClass).stream().filter(Event.class::isAssignableFrom)
                .forEach(aC -> {
                    List<Router> routers = Optional.ofNullable(tempRouterMap.get(aC)).orElseGet(ArrayList::new);
                    singleEventRouterMap.computeIfAbsent(eventClass, clazz -> new ArrayList<>()).addAll(routers);
                }));

        HashMap<Class<? extends Event>, List<Interceptor>> tempInterceptorsMap = new HashMap<>();

        milkySupport.getInterceptors().stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(singleEventFormat)
                .filter(m -> m.isAnnotationPresent(Intercept.class)).collect(Collectors.toList())
                .forEach(method -> {
                    Intercept annotation = method.getAnnotation(Intercept.class);
                    Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                    Object bean = BeanUtil.getBean(method.getDeclaringClass());
                    Interceptor interceptor = Interceptor.builder().bean(bean).method(method)
                            .order(annotation.order()).posEnum(annotation.pos()).build();
                    Reflect.ancestorClasses(eventClass).stream().filter(Event.class::isAssignableFrom)
                            .forEach(eC -> tempInterceptorsMap.computeIfAbsent(eventClass, cC -> new ArrayList<>()).add(interceptor));
                });

        // divided into before and after
        tempInterceptorsMap.forEach((eventClass, interceptors) -> {
            List<Interceptor> beforeInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.BEFORE)).collect(Collectors.toList());
            beforeEventInterceptors.put(eventClass, beforeInterceptors);
            List<Interceptor> afterInterceptors = interceptors.stream()
                    .filter(interceptor -> interceptor.getPosEnum().equals(PosEnum.AFTER)).collect(Collectors.toList());
            afterEventInterceptors.put(eventClass, afterInterceptors);
        });

        // internal order
        beforeEventInterceptors.forEach((k, v) ->
                v = v.stream().sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList()));
        afterEventInterceptors.forEach((k, v) ->
                v = v.stream().sorted(Comparator.comparing(Interceptor::getOrder)).collect(Collectors.toList()));

        methods = milkySupport.getEventRouters().stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(batchEventFormat)
                .filter(m -> m.isAnnotationPresent(BatchEventRouter.class))
                .collect(Collectors.toList());

        this.batchRouters = methods.stream().map(method -> {
            BatchEventRouter annotation = method.getAnnotation(BatchEventRouter.class);
            Class<? extends Event> eventClass = (Class<? extends Event>) ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
            Object bean = BeanUtil.getBean(method.getDeclaringClass());
            return BatchRouter.builder().bean(bean).method(method)
                    .eventClass(eventClass)
                    .order(annotation.order())
                    .executorService(milkySupport.getAsyncExecutor())
                    .build();
        }).sorted(Comparator.comparing(BatchRouter::getOrder)).collect(Collectors.toList());

    }

    public void route(Event event, Context context) {
        Optional.ofNullable(beforeEventInterceptors.get(event.getClass())).ifPresent(interceptors -> interceptors
                .forEach(interceptor -> interceptor.invoke(event, null, context)));
        Optional.ofNullable(singleEventRouterMap.get(event.getClass())).orElseGet(ArrayList::new)
                .forEach(router -> router.route(event, context));
        Optional.ofNullable(afterEventInterceptors.get(event.getClass())).ifPresent(interceptors -> interceptors
            .forEach(interceptor -> interceptor.invoke(event, null, context)));
    }

    public void batchRoute(List<? extends Event> events, Context context) {
        batchRouters.forEach(batchRouter -> batchRouter.route(events, context));
    }

    @Data
    @Builder
    @AllArgsConstructor
    static public class Router {

        private final Object bean;

        private final Method method;

        private ExecutorService executorService;

        @SneakyThrows
        public void route(Event event, Context context) {
            Runner.invoke(bean, method, event, context);
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    static public class BatchRouter<T extends Class<? extends Event>> {

        private T eventClass;

        private final Object bean;

        private final Method method;

        private boolean asyncable;

        private int order;

        private ExecutorService executorService;

        @SneakyThrows
        public void route(List<? extends Event> events, Context context) {
            events = events.stream().filter(event -> eventClass.isAssignableFrom(event.getClass())).collect(Collectors.toList());
            if (Collect.isEmpty(events)) {
                return;
            }
            if (asyncable) {
                List<? extends Event> finalEvents = events;
                executorService.submit(() -> method.invoke(bean, finalEvents, context));
            } else {
                Runner.invoke(bean, method, events, context);
            }
        }
    }

}
