package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.IntStream;

public class Runner {

    static final Logger log = Logger.getLogger(Runner.class);

    static private Map<String, Object> getSignature(Object bean, Method method, Object... params) {
        Map<String, Object> args = new HashMap<>();
        StreamMap<String, Object> streamMap = StreamMap.init();
        IntStream.range(0, params.length).forEach(index -> streamMap.put("arg" + index, Json.toJson(params[index])));
        return streamMap.put("invokeClassName", bean.getClass().getName())
                .put("methodName", method.getName())
                .put(args).getMap();
    }

    static public Object invoke(Object bean, Method method, Object... params) {
        Option<Object, Object> option = Option.builder().build();
        return invoke(option, bean, method, params);
    }

    @SneakyThrows
    static private Object invoke(Option<Object, Object> option, Object bean, Method method, Object... params) {
        Object result = null;
        Throwable throwableBackup = null;
        boolean success = true;
        int retryTimes = option.getRetryTimes();
        do {
            try {
                result = method.invoke(bean, params);
            } catch (Throwable ex) {
                success = false;
                if (ex instanceof InvocationTargetException) {
                    throwableBackup = ((InvocationTargetException) ex).getTargetException();
                } else {
                    throwableBackup = ex;
                }
                if (retryTimes == 0) {
                    throw throwableBackup;
                }
            } finally {
                Map<String, Object> signature = getSignature(bean, method, params);
                if (throwableBackup == null && option.isWithLog()) {
                    log.with(signature).with("result", Json.toJson(result)).info("");
                } else if (throwableBackup != null){
                    log.with(signature).error("", throwableBackup);
                }
            }
        } while (!success && retryTimes-- > 0);
        return result;
    }

    static private Map<String, Object> getSignature(Serializable lambda) {
        Map<String, Object> lambdaInfos = SLambda.resolve(lambda);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        StreamMap<String, Object> streamMap = StreamMap.init();
        return streamMap.put("invokeClassName", stackTraceElement.getClassName())
                .put("invokeLineNumber", stackTraceElement.getLineNumber())
                .put("invokeMethodName", stackTraceElement.getMethodName())
                .put(lambdaInfos).getMap();

    }

    static public void run(SRunnable runnable) {
        Option<Object, Object> option = Option.builder().build();
        run(option, runnable);
    }

    @SneakyThrows
    static public void run(Option<Object,Object> option, SRunnable callable) {
        Throwable throwableBackup = null;
        int retryTimes = option.getRetryTimes();
        do {
            try {
                callable.run();
            } catch (Throwable throwable) {
                throwableBackup = throwable;
                if (retryTimes == 0) {
                    throw throwableBackup;
                }
            } finally {
                if (throwableBackup == null && option.isWithLog()) {
                    log.with(getSignature(callable)).info("");
                } else if (throwableBackup != null){
                    log.with(getSignature(callable)).error("", throwableBackup);
                }
            }
        } while (retryTimes-- > 0);
    }

    static private  <R, T> T checkout(SCallable<R> callable) {
        Option<R, T> option = Option.<R, T>builder().build();
        return checkout(option, callable);
    }

    @SneakyThrows
    static public <R, T> T checkout(Option<R,T> option, SCallable<R> callable) {
        SysException.nullThrow(option.getCheck(), option.getTransfer());
        R result = null;
        Throwable throwableBackup = null;
        int retryTimes = option.getRetryTimes();
        do {
            try {
                result = callable.call();
                R finalResult = result;
                SysException.falseThrow(option.getCheck().apply(result), () -> Json.toJson(finalResult));
                return option.getTransfer().apply(callable.call());
            } catch (Throwable throwable) {
                if (throwable instanceof InvocationTargetException) {
                    throwableBackup = ((InvocationTargetException) throwable).getTargetException();
                } else {
                    throwableBackup = throwable;
                }
                if (retryTimes == 0) {
                    if (option.getDefaultValue() == null) {
                        throw throwableBackup;
                    }
                    return option.getDefaultValue();
                }
            } finally {
                if (throwableBackup == null && option.isWithLog()) {
                    log.with(getSignature(callable)).with("result", Json.toJson(result)).info("");
                } else if (throwableBackup != null){
                    log.with(getSignature(callable)).with("defaultValue", option.getDefaultValue()).error("", throwableBackup);
                }
            }
        } while (retryTimes-- > 0);
        throw new SysException("unreached part!");
    }

    @SneakyThrows
    static public <R> R call(Option<R, ?> option, SCallable<R> callable) {
        SysException.trueThrow(option.getCheck() != null,  ErrorEnumBase.CONFIG_ERROR);
        SysException.trueThrow(option.getTransfer() != null,  ErrorEnumBase.CONFIG_ERROR);
        SysException.trueThrow(option.getDefaultValue() != null,  ErrorEnumBase.CONFIG_ERROR);
        R result = null;
        Throwable throwableBackup = null;
        int retryTimes = option.getRetryTimes();
        do {
            try {
                return callable.call();
            } catch (Throwable throwable) {
                if (throwable instanceof InvocationTargetException) {
                    throwableBackup = ((InvocationTargetException) throwable).getTargetException();
                } else {
                    throwableBackup = throwable;
                }
                if (retryTimes == 0) {
                    throw throwableBackup;
                }
            } finally {
                if (throwableBackup == null && option.isWithLog()) {
                    log.with(getSignature(callable)).with("result", Json.toJson(result)).info("");
                } else if (throwableBackup != null){
                    log.with(getSignature(callable)).with("defaultValue", option.getDefaultValue()).error("", throwableBackup);
                }
            }
        } while (retryTimes-- > 0);
        throw new SysException("unreached part!");
    }
}
