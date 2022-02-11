package com.stellariver.milky.domain.support.event;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.BeanLoader;
import com.stellariver.milky.common.tool.common.ErrorCodeBase;
import com.stellariver.milky.common.tool.common.ReflectTool;
import com.stellariver.milky.common.tool.log.Log;
import com.stellariver.milky.domain.support.ErrorCodeEnum;
import com.stellariver.milky.domain.support.context.Context;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class EventBus {

    private final Map<Class<? extends Event>, Handler> handlerMap = new HashMap<>();

    @Resource
    private BeanLoader beanLoader;

    @PostConstruct
    public void init() {
        List<Object> beans = beanLoader.getBeansForAnnotation(EventHandler.class);
        List<Method> methods = beans.stream().map(Object::getClass).map(Class::getMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(EventHandler.class)).collect(Collectors.toList());
        methods.forEach(method -> {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            Class<? extends Event> eventClass = annotation.value();
            Object bean = beanLoader.getBean(eventClass);
            handlerMap.put(eventClass, new Handler(bean, method, annotation.type()));
        });
    }

    public void handler(Event event, Context context) {
        Handler handler = handlerMap.get(event.getClass());
        BizException.nullThrow(handler, ErrorCodeEnum.CONFIG_ERROR);
        handler.handle(event, context);
    }


    @AllArgsConstructor
    static public class Handler {

        private final Object bean;

        private final Method method;

        private final HandlerTypeEnum type;

        /**
         * 强制获取url的线程池
         */
        private static final ExecutorService asyncEventHandlerPool;
        
        static {
            asyncEventHandlerPool = initAsyncEventHandlerThreadPool();
        }

        private static ExecutorService initAsyncEventHandlerThreadPool() {
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setUncaughtExceptionHandler((t, e) -> 
                            Log.of(() -> log.error("|线程名={}|错误信息={}|", t.getName(), e.getMessage(), e)).log(ErrorCodeBase.UNKNOWN))
                    .setNameFormat("event-handler-url-thread-%d")
                    .build();

            return new ThreadPoolExecutor(10, 20, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(500),
                    threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        }

        public void handle(Event event, Context context) {
            if (Objects.equals(type, HandlerTypeEnum.SYNC)) {
                ReflectTool.invokeBeanMethod(bean, method, event, context);
            } else if (Objects.equals(type, HandlerTypeEnum.ASYNC)){
                asyncEventHandlerPool.submit(() -> ReflectTool.invokeBeanMethod(bean, method, event, context));
            } else {
                throw new BizException(ErrorCodeEnum.CONFIG_ERROR.message("只支持同步及异步调用"));
            }
        }
    }
}
