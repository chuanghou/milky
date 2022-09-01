package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.ErrorEnum;
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

    static final private Predicate<Method> eventRouterFormat = method -> {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 2
                && Event.class.isAssignableFrom(parameterTypes[0])
                && parameterTypes[1] == Context.class;
    };

    static final private Predicate<Method> finalEventRouterFormat = method -> {
        if (!method.isAnnotationPresent(FinalEventRouter.class)) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean parametersMatch = parameterTypes.length == 2 &&
                List.class.isAssignableFrom(parameterTypes[0]) && Context.class.isAssignableFrom(parameterTypes[1]);
        SysException.falseThrow(parametersMatch, ErrorEnum.CONFIG_ERROR.message("FinalEventRouter format wrong! "
                 + method.getDeclaringClass().getName() + "#" + method.getName()));
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Type actualTypeArgument = ((ParameterizedType) genericParameterTypes[0]).getActualTypeArguments()[0];
        return actualTypeArgument instanceof Class<?> && Event.class.isAssignableFrom((Class<?>)actualTypeArgument);
    };

    private final Map<Class<? extends Event>, List<Router>> eventRouterMap = new HashMap<>();

    private final List<FinalRouter<Class<? extends Event>>> finalRouters;

    private final Map<Class<? extends Event>, List<Interceptor>> beforeEventInterceptors = new HashMap<>();

    private final Map<Class<? extends Event>, List<Interceptor>> afterEventInterceptors = new HashMap<>();

    @SuppressWarnings("unchecked")
    public EventBus(MilkySupport milkySupport) {

        List<Method> methods = milkySupport.getEventRouters().stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(eventRouterFormat)
                .filter(m -> m.isAnnotationPresent(EventRouter.class))
                .collect(Collectors.toList());
        Map<Class<? extends Event>, List<Router>> tempRouterMap = new HashMap<>();
        methods.forEach(method -> {
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = BeanUtil.getBean(method.getDeclaringClass());
            Router router = Router.builder().bean(bean).method(method).build();
            tempRouterMap.computeIfAbsent(eventClass, clazz -> new ArrayList<>()).add(router);
        });

        Reflections reflections = milkySupport.getReflections();
        Set<Class<? extends Event>> eventClasses = reflections.getSubTypesOf(Event.class);
        eventClasses.forEach(eventClass -> Reflect.ancestorClasses(eventClass).stream().filter(Event.class::isAssignableFrom)
                .forEach(aC -> {
                    List<Router> routers = Optional.ofNullable(tempRouterMap.get(aC)).orElseGet(ArrayList::new);
                    eventRouterMap.computeIfAbsent(eventClass, clazz -> new ArrayList<>()).addAll(routers);
                }));

        HashMap<Class<? extends Event>, List<Interceptor>> tempInterceptorsMap = new HashMap<>();

        milkySupport.getInterceptors().stream()
                .map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(eventRouterFormat)
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
                .filter(finalEventRouterFormat)
                .filter(m -> m.isAnnotationPresent(FinalEventRouter.class))
                .collect(Collectors.toList());

        this.finalRouters = methods.stream().map(method -> {
            FinalEventRouter annotation = method.getAnnotation(FinalEventRouter.class);
            Class<? extends Event> eventClass = (Class<? extends Event>) ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
            Object bean = BeanUtil.getBean(method.getDeclaringClass());
            return FinalRouter.builder().bean(bean).method(method)
                    .eventClass(eventClass)
                    .order(annotation.order())
                    .executorService(milkySupport.getAsyncExecutor())
                    .build();
        }).sorted(Comparator.comparing(FinalRouter::getOrder)).collect(Collectors.toList());

    }

    public void route(Event event, Context context) {
        beforeEventInterceptors.getOrDefault(event.getClass(), new ArrayList<>(0))
                .forEach(interceptor -> interceptor.invoke(event, null, context));
        eventRouterMap.getOrDefault(event.getClass(), new ArrayList<>(0))
                .forEach(router -> router.route(event, context));
        afterEventInterceptors.getOrDefault(event.getClass(), new ArrayList<>(0))
            .forEach(interceptor -> interceptor.invoke(event, null, context));
    }

    public void finalRoute(List<? extends Event> events, Context context) {
        finalRouters.forEach(finalRouter -> finalRouter.route(events, context));
    }

    public void postFinalRoute(List<Event> finalRouteEvents, Context context) {
    }

    public void preFinalRoute(List<Event> finalRouteEvents, Context context) {
    }

    @Data
    @Builder
    @AllArgsConstructor
    static public class Router {

        private final Object bean;

        private final Method method;

        @SneakyThrows
        public void route(Event event, Context context) {
            Runner.invoke(bean, method, event, context);
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    static public class FinalRouter<T extends Class<? extends Event>> {

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
