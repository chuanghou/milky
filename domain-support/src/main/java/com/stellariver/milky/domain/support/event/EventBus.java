package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.ReflectTool;
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

    private final BeanLoader beanLoader;

    @SuppressWarnings("unchecked")
    public EventBus(BeanLoader beanLoader) {
        this.beanLoader = beanLoader;

        List<EventProcessor> beans = this.beanLoader.getBeansOfType(EventProcessor.class);
        List<Method> methods = beans.stream().map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> eventHandlerFormat.test(m.getParameterTypes()))
                .filter(m -> m.isAnnotationPresent(EventHandler.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            Object bean = beanLoader.getBean(method.getDeclaringClass());
            Handler handler = Handler.builder().bean(bean).method(method)
                    .type(annotation.type()).order(annotation.order())
                    .executorService((ExecutorService) beanLoader.getBean(annotation.executor()))
                    .build();
            handlerMap.computeIfAbsent(eventClass, eC -> new ArrayList<>()).add(handler);
        });
    }

    public void handler(Event event, Context context) {
        List<Handler> handlers = Optional.ofNullable(handlerMap.get(event.getClass())).orElse(new ArrayList<>());
        handlers.stream().sorted(Comparator.comparing(Handler::getOrder))
                .forEach(handler -> ReflectTool.run(() -> handler.handle(event, context)));
    }


    @Data
    @Builder
    @AllArgsConstructor
    static public class Handler {

        private final Object bean;

        private final Method method;

        private final HandlerTypeEnum type;

        private int order;

        private ExecutorService executorService;

        public void handle(Event event, Context context) {
            if (Objects.equals(type, HandlerTypeEnum.SYNC)) {
                ReflectTool.invokeBeanMethod(bean, method, event, context);
            } else if (Objects.equals(type, HandlerTypeEnum.ASYNC)){
                executorService.submit(() -> ReflectTool.invokeBeanMethod(bean, method, event, context));
            } else {
                throw new BizException(ErrorCodeEnum.CONFIG_ERROR.message("只支持同步及异步调用"));
            }
        }
    }
}
