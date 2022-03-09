package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.InvokeUtil;
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

    static final private Predicate<Class<?>[]> eventHandlerFormat = parameterTypes ->
            (parameterTypes.length == 2
                    && Event.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == Context.class);

    private final Map<Class<? extends Event>, List<Handler>> handlerMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public EventBus(BeanLoader beanLoader, ExecutorService asyncExecutorService) {

        List<EventRouter> beans = beanLoader.getBeansOfType(EventRouter.class);
        List<Method> methods = beans.stream().map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> eventHandlerFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(Router.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            Router annotation = method.getAnnotation(Router.class);
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            Handler handler = Handler.builder().bean(bean).method(method)
                    .type(annotation.type()).order(annotation.order())
                    .asyncExecutorService(asyncExecutorService)
                    .build();
            handlerMap.computeIfAbsent(eventClass, eC -> new ArrayList<>()).add(handler);
        });
    }

    public void handler(Event event, Context context) {
        List<Handler> handlers = Optional.ofNullable(handlerMap.get(event.getClass())).orElse(new ArrayList<>());
        handlers.stream().sorted(Comparator.comparing(Handler::getOrder))
                .forEach(handler -> InvokeUtil.run(() -> handler.handle(event, context)));
    }


    @Data
    @Builder
    @AllArgsConstructor
    static public class Handler {

        private final Object bean;

        private final Method method;

        private final TypeEnum type;

        private int order;

        private ExecutorService asyncExecutorService;

        public void handle(Event event, Context context) {
            if (Objects.equals(type, TypeEnum.SYNC)) {
                InvokeUtil.invoke(bean, method, event, context);
            } else if (Objects.equals(type, TypeEnum.ASYNC)){
                asyncExecutorService.submit(() -> InvokeUtil.invoke(bean, method, event, context));
            } else {
                throw new BizException(ErrorCodeEnum.CONFIG_ERROR.message("only support sync and async invoke"));
            }
        }
    }
}
