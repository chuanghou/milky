package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.domain.support.ErrorCodeEnum;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class EventBus {

    static final private Predicate<Class<?>[]> eventRouterFormat = parameterTypes ->
            (parameterTypes.length == 2
                    && Event.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == Context.class);

    private final Map<Class<? extends Event>, List<Router>> routerMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public EventBus(BeanLoader beanLoader, ExecutorService asyncExecutorService) {

        List<EventRouters> beans = beanLoader.getBeansOfType(EventRouters.class);
        List<Method> methods = beans.stream().map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> eventRouterFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(EventRouter.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            EventRouter annotation = method.getAnnotation(EventRouter.class);
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            Router router = Router.builder().bean(bean).method(method)
                    .type(annotation.type()).order(annotation.order())
                    .asyncExecutorService(asyncExecutorService)
                    .build();
            routerMap.computeIfAbsent(eventClass, eC -> new ArrayList<>()).add(router);
        });
    }

    public void route(Event event, Context context) {
        List<Router> routers = Optional.ofNullable(routerMap.get(event.getClass())).orElse(new ArrayList<>());
        routers.stream().sorted(Comparator.comparing(Router::getOrder))
                .forEach(router -> Runner.run(() -> router.route(event, context)));
    }


    @Data
    @Builder
    @AllArgsConstructor
    static public class Router {

        private final Object bean;

        private final Method method;

        private final TypeEnum type;

        private int order;

        private ExecutorService asyncExecutorService;

        public void route(Event event, Context context) {
            if (Objects.equals(type, TypeEnum.SYNC)) {
                Runner.invoke(bean, method, event, context);
            } else if (Objects.equals(type, TypeEnum.ASYNC)){
                asyncExecutorService.submit(() -> Runner.invoke(bean, method, event, context));
            } else {
                throw new BizException(ErrorCodeEnum.CONFIG_ERROR.message("only support sync and async invoke"));
            }
        }
    }
}
