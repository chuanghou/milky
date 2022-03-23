package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.common.tool.util.StreamMap;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Runner {

    static final Logger log = Logger.getLogger(Runner.class);

    static private Map<String, Object> getSignature(Serializable lambda) {
        Map<String, Object> lambdaInfos = SLambda.resolve(lambda);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        StreamMap<String, Object> streamMap = StreamMap.init();
        return streamMap.put("invokeClassName", stackTraceElement.getClassName())
                .put("invokeLineNumber", stackTraceElement.getLineNumber())
                .put("invokeMethodName", stackTraceElement.getMethodName())
                .put(lambdaInfos).getMap();

    }

    public static Map<String, Object> getSignature(Object bean, Method method, Object... params) {
        Map<String, Object> args = new HashMap<>();
        StreamMap<String, Object> streamMap = StreamMap.init();
        IntStream.range(0, params.length).forEach(index -> streamMap.put("arg" + index, Json.toJson(params[index])));
        return streamMap.put("invokeClassName", bean.getClass().getName())
                .put("methodName", method.getName())
                .put(args).getMap();
    }

    static public Object invoke(Object bean, Method method, Object... params) {
        return invoke(false, bean, method, params);
    }

    static public Object invokeWithLog(Object bean, Method method, Object... params) {
        return invoke(true, bean, method, params);
    }

    @SneakyThrows
    static private Object invoke(boolean withLog, Object bean, Method method, Object... params) {
        Object result = null;
        Throwable throwableBackup = null;
        try {
            result = method.invoke(bean, params);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            throwableBackup = targetException;
            throw targetException;
        } catch (Throwable ex) {
            throwableBackup = ex;
            throw ex;
        } finally {
            if (throwableBackup == null) {
                if (withLog) {
                    Map<String, Object> signature = getSignature(bean, method, params);
                    log.with(signature).with("result", Json.toJson(result)).info("");
                }
            } else {
                Map<String, Object> signature = getSignature(bean, method, params);
                log.with(signature).error("", throwableBackup);
            }
        }
        return result;
    }

    static public <R> R call(SCallable<R> callable) {
        return call(false, callable);
    }

    static public <R> R callWithLog(SCallable<R> callable) {
        return call(true, callable);
    }

    @SneakyThrows
    static private <R> R call(boolean withLog, SCallable<R> callable) {
        R result = null;
        Throwable throwableBackup = null;
        try {
            result = callable.call();
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            throwableBackup = targetException;
            throw targetException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw throwable;
        } finally {
            if (throwableBackup == null && withLog) {
                log.with(getSignature(callable)).with("result", Json.toJson(result)).info("");
            } else {
                log.with(getSignature(callable)).error("", throwableBackup);
            }
        }
        return result;
    }

    static public <R> R call(SCallable<R> callable, Function<R, Boolean> check) {
        return call(false, callable, check);
    }

    static public <R> R callWithLog(SCallable<R> callable, Function<R, Boolean> check) {
        return call(true, callable, check);
    }

    @SneakyThrows
    static public <R> R call(boolean withLog, SCallable<R> callable, Function<R, Boolean> check) {
        R result = null;
        Throwable throwableBackup = null;
        try {
            result = callable.call();
            if (!check.apply(result)) {
                throw new SysException(Json.toJson(result));
            }
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            throwableBackup = targetException;
            throw targetException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw throwable;
        } finally {
            if (throwableBackup == null && withLog) {
                log.with(getSignature(callable)).with("result", Json.toJson(result)).info("");
            } else {
                log.with(getSignature(callable)).error("", throwableBackup);
            }
        }
        return result;
    }

    static public <R, T> T call(SCallable<R> callable, Function<R, Boolean> check, Function<R, T> getData) {
        return getData.apply(call(callable, check));
    }
    static public <R, T> T callWithLog(SCallable<R> callable, Function<R, Boolean> check, Function<R, T> getData) {
        return getData.apply(call(true, callable, check));
    }

    static public <R, T> T fallbackableCall(SCallable<R> callable,
                                            Function<R, Boolean> check,
                                            Function<R, T> getData,
                                            T defaultValue) {
        return fallbackableCall(false, callable, check, getData, defaultValue);
    }

    static public <R, T> T fallbackableCallWithLog(SCallable<R> callable,
                                            Function<R, Boolean> check,
                                            Function<R, T> getData,
                                            T defaultValue) {
        return fallbackableCall(true, callable, check, getData, defaultValue);
    }

    @SneakyThrows
    static private  <R, T> T fallbackableCall(boolean withLog, SCallable<R> callable,
                                            Function<R, Boolean> check,
                                            Function<R, T> getData,
                                            T defaultValue) {
        R result = null;
        Throwable throwableBackup = null;
        try {
            result = callable.call();
            if (check.apply(result)) {
                return getData.apply(callable.call());
            }
            return defaultValue;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            throwableBackup = targetException;
            throw targetException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
        } finally {
            if (throwableBackup == null && withLog) {
                log.with(getSignature(callable)).with("result", Json.toJson(result)).info("");
            } else {
                log.with(getSignature(callable)).with("defaultValue", defaultValue).error("", throwableBackup);
            }
        }
        return defaultValue;
    }

    static public void run(SRunnable runnable) {
        run(false, runnable);
    }

    static public void runWithLog(SRunnable runnable) {
        run(true, runnable);
    }

    static private void run(boolean withLog, SRunnable runnable) {
        Throwable throwableBackup = null;
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw throwable;
        } finally {
            if (throwableBackup == null && withLog) {
                log.with(getSignature(runnable)).info("");
            } else {
                log.with(getSignature(runnable)).error("", throwableBackup);
            }
        }
    }

    static public void fallbackableRun(SRunnable runnable) {
        fallbackableRun(false, runnable);
    }
    static public void fallbackableRunWithLog(SRunnable runnable) {
        fallbackableRun(true, runnable);
    }
    static public void fallbackableRun(boolean withLog, SRunnable runnable) {
        Throwable throwableBackup = null;
        try {
            runnable.run();
        } catch (Throwable throwable) {
           throwableBackup = throwable;
        } finally {
            if (throwableBackup == null && withLog) {
                log.with(getSignature(runnable)).info("");
            } else {
                log.with(getSignature(runnable)).error("", throwableBackup);
            }
        }
    }
}
