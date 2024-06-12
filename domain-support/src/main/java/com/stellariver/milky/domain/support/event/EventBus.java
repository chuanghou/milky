package com.stellariver.milky.domain.support.event;

import com.google.common.collect.*;
import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.base.Trail;
import com.stellariver.milky.domain.support.context.Context;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;

/**
 * @author houchuang
 */
@Getter
public class EventBus {

    static final private Predicate<Method> FORMAT = method ->
            Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 2
                    && Event.class.isAssignableFrom(method.getParameterTypes()[0]) && method.getParameterTypes()[1] == Context.class;

    static final private Predicate<Method> FINAL_EVENT_ROUTER_FORMAT = method -> {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean parametersMatch = parameterTypes.length == 2 &&
                List.class.isAssignableFrom(parameterTypes[0]) && Context.class.isAssignableFrom(parameterTypes[1]);
        SysEx.falseThrow(parametersMatch, CONFIG_ERROR.message("FinalEventRouter format wrong! "
                + method.getDeclaringClass().getName() + "#" + method.getName()));
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Type actualTypeArgument = ((ParameterizedType) genericParameterTypes[0]).getActualTypeArguments()[0];
        return actualTypeArgument instanceof Class<?> && Event.class.isAssignableFrom((Class<?>)actualTypeArgument);
    };

    private final Multimap<Class<? extends Event>, Router> eventRouterMap = ArrayListMultimap.create();

    private final List<FinalRouter<Class<? extends Event>>> finalRouters = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public EventBus(MilkySupport milkySupport) {
        BeanLoader beanLoader = milkySupport.getBeanLoader();

        // collect all event routers
        List<Method> methods = milkySupport.getEventRouters().stream()
                .map(Object::getClass).map(Class::getDeclaredMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(EventRouter.class))
                .peek(m -> SysEx.falseThrow(FORMAT.test(m), CONFIG_ERROR.message(m.toGenericString() + " signature not valid!")))
                .collect(Collectors.toList());

        // for every method, add a (eventClass, router) to rawMap
        ListMultimap<Class<? extends Event>, Router> rawRouteMap = MultimapBuilder.hashKeys().arrayListValues().build();
        methods.forEach(method -> {
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            Router router = new Router(bean, method, method.getAnnotation(EventRouter.class).order());
            rawRouteMap.get(eventClass).add(router);
        });

        SetMultimap<Class<? extends Event>, Router> organizedRouteMap = MultimapBuilder.hashKeys().hashSetValues().build();
        rawRouteMap.asMap().forEach((lowest, routers) -> {
            List<Class<? extends Event>> eventClasses = Reflect.ancestorClasses(lowest)
                    .stream().filter(Event.class::isAssignableFrom).filter(e -> e != Event.class).collect(Collectors.toList());
            eventClasses.forEach(eC -> organizedRouteMap.putAll(eC, routers));
        });

        organizedRouteMap.asMap().forEach((ec, rs) -> {
            List<Router> routers = rs.stream().sorted(Comparator.comparing(Router::getOrder)).collect(Collectors.toList());
            eventRouterMap.putAll(ec, routers);
        });

        methods = milkySupport.getEventRouters().stream()
                .map(Object::getClass).map(Class::getDeclaredMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(FinalEventRouter.class))
                .peek(m -> SysEx.falseThrow(FINAL_EVENT_ROUTER_FORMAT.test(m),
                        CONFIG_ERROR.message(m.toGenericString())))
                .collect(Collectors.toList());

        List<FinalRouter<Class<? extends Event>>> tempFinalRouters = methods.stream().map(method -> {
            FinalEventRouter annotation = method.getAnnotation(FinalEventRouter.class);
            BizEx.trueThrow(Kit.eq(annotation.order(), 0.0), CONFIG_ERROR.message("final event router order must not 0!"));
            Type typeArgument = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
            Class<? extends Event> eventClass = (Class<? extends Event>) typeArgument;
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            return new FinalRouter<Class<? extends Event>>(eventClass,
                    bean, method, annotation.asyncable(), annotation.order(), milkySupport.getEnhancedExecutor());
        }).collect(Collectors.toList());
        List<FinalRouter<Class<? extends Event>>> notDefaultOrderRouters = tempFinalRouters.stream()
                .filter(fR -> !Kit.eq(fR.getOrder(), Long.MAX_VALUE)).collect(Collectors.toList());
        Set<Long> orders = Collect.transfer(notDefaultOrderRouters, FinalRouter::getOrder, HashSet::new);
        SysEx.falseThrow(Kit.eq(orders.size(), notDefaultOrderRouters.size()),
                CONFIG_ERROR.message("exists finalEventRouters share same order!"));
        finalRouters.addAll(tempFinalRouters);
    }

    public void route(Event event, Context context) {
        eventRouterMap.get(event.getClass()).forEach(router -> {
            router.route(event, context);
            Trail trail = Trail.builder().message(event)
                .beanName(router.bean.getClass().getSimpleName()).build();
            context.record(trail);
        });
    }

    public void preFinalRoute(List<? extends Event> events, Context context) {
        finalRouters.stream().filter(finalRouter -> finalRouter.order < 0)
                .sorted(Comparator.comparing(FinalRouter::getOrder))
                .forEach(finalRouter -> finalRouter.route(events, context));
    }

    public void postFinalRoute(List<? extends Event> events, Context context) {
        finalRouters.stream().filter(finalRouter -> finalRouter.order > 0)
                .sorted(Comparator.comparing(FinalRouter::getOrder))
                .forEach(finalRouter -> finalRouter.route(events, context));
    }

    @Data
    @RequiredArgsConstructor
    static public class Router {

        private final Object bean;
        private final Method method;
        private final Long order;

        public void route(Event event, Context context) {
            Reflect.invoke(method, bean, event, context);
        }
    }

    @Data
    static public class FinalRouter<T extends Class<? extends Event>> {

        private T eventClass;
        private final Object bean;
        private final Method method;
        private boolean asyncable;
        private Long order;
        private ExecutorService executorService;

        public FinalRouter(T eventClass, Object bean, Method method, boolean asyncable, Long order, ExecutorService executorService) {
            this.eventClass = eventClass;
            this.bean = bean;
            this.method = method;
            this.asyncable = asyncable;
            this.order = order;
            this.executorService = executorService;
        }

        public void route(List<? extends Event> events, Context context) {
            events = events.stream().filter(event -> eventClass.isAssignableFrom(event.getClass())).collect(Collectors.toList());
            if (Collect.isEmpty(events)) {
                return;
            }
            if (asyncable) {
                List<? extends Event> finalEvents = events;
                executorService.submit(() -> Reflect.invoke(method, bean, finalEvents, context));
            } else {
                Reflect.invoke(method, bean, events, context);
            }
            events.forEach(event -> {
                Trail trail = Trail.builder().beanName(this.getClass().getSimpleName()).message(event).build();
                context.record(trail);
            });
        }
    }

}
