package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Runner {

    static final Logger log = Logger.getLogger(Runner.class);

    static private Pair<String, Map<String, Object>> getSignature(Serializable lambda, int stackTraceLevel) {
        Map<String, Object> lambdaInfos = SLambda.resolve(lambda);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[stackTraceLevel];
        return Pair.of(stackTraceElement.toString(), lambdaInfos);
    }

    static public void run(SRunnable runnable) {
        Option<Object, Object> option = Option.builder().build();
        option.setStackTraceLevel(option.getStackTraceLevel() + 1);
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
                Pair<String, Map<String, Object>> signature = getSignature(callable, option.getStackTraceLevel());
                if (throwableBackup == null && option.isAlwaysLog()) {
                    log.with(signature.getRight()).info(signature.getLeft());
                } else if (throwableBackup != null){
                    log.with(signature.getRight()).error(signature.getLeft(), throwableBackup);
                }
            }
        } while (retryTimes-- > 0);
    }

    static private  <R, T> T checkout(SCallable<R> callable) {
        Option<R, T> option = Option.<R, T>builder().build();
        option.setStackTraceLevel(option.getStackTraceLevel() + 1);
        return checkout(option, callable);
    }

    @SneakyThrows
    static public <R, T> T checkout(Option<R, T> option, SCallable<R> callable) {
        SysException.anyNullThrow(option.getCheck(), option.getTransfer());
        R result = null;
        Throwable throwableBackup = null;
        int retryTimes = option.getRetryTimes();
        do {
            try {
                result = callable.call();
                SysException.falseThrow(option.getCheck().apply(result), result);
                return option.getTransfer().apply(result);
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
                Pair<String, Map<String, Object>> signature = getSignature(callable, option.getStackTraceLevel());
                if (throwableBackup == null && option.isAlwaysLog()) {
                    Function<R, String> printer = Optional.ofNullable(option.getLogResultSelector()).orElse(Json::toJson);
                    log.with(signature.getRight()).with("result", printer.apply(result)).info(signature.getLeft());
                } else if (throwableBackup != null){
                    log.with(signature.getRight()).error(signature.getLeft(), throwableBackup);
                }
            }
        } while (retryTimes-- > 0);
        throw new SysException("unreached part!");
    }

    static public <R> R call(SCallable<R> callable) {
        Option<R, Object> option = Option.<R, Object>builder().build();
        option.setStackTraceLevel(option.getStackTraceLevel() + 1);
        return call(option, callable);
    }

    @SneakyThrows
    static public <R> R call(Option<R, ?> option, SCallable<R> callable) {
        R result = null;
        Throwable throwableBackup = null;
        int retryTimes = option.getRetryTimes();
        do {
            try {
                result = callable.call();
                return result;
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
                Pair<String, Map<String, Object>> signature = getSignature(callable, option.getStackTraceLevel());
                if (throwableBackup == null && option.isAlwaysLog()) {
                    Function<R, String> printer = Optional.ofNullable(option.getLogResultSelector()).orElse(Json::toJson);
                    log.with(signature.getRight()).with("result", printer.apply(result)).info(signature.getLeft());
                } else if (throwableBackup != null){
                    log.with(signature.getRight()).error(signature.getLeft(), throwableBackup);
                }
            }
        } while (retryTimes-- > 0);
        throw new SysException("unreached part!");
    }

    @SneakyThrows
    public static Object invoke(Object bean, Method method, Object... args) {
        try {
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
