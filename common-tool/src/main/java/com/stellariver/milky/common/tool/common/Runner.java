package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.Json;
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

    @SneakyThrows
    static public Object invoke(Object bean, Method method, Object... params) {
        Object result;
        Map<String, String> args = new HashMap<>();
        IntStream.range(0, params.length).forEach(index -> args.put("arg" + index, Json.toJson(params[index])));
        log.with("invokeClassName", bean.getClass().getName())
                .with("methodName", method.getName())
                .with(args);
        Throwable throwableBackup = null;
        try {
            result = method.invoke(bean, params);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            throwableBackup = targetException;
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            } else if (targetException instanceof SysException) {
                throw (SysException) ex.getTargetException();
            }
            throw targetException;
        } catch (Throwable ex) {
            throwableBackup = ex;
            throw ex;
        } finally {
            if (throwableBackup == null) {
                log.info("");
            } else {
                log.error("", throwableBackup);
            }
        }
        return result;
    }

    static private void recordInvokeSignature(Serializable lambda) {
        Map<String, String> lambdaInfos = SLambda.resolve(lambda);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        log.with("invokeClassName", stackTraceElement.getClassName())
                .with("invokeLineNumber", stackTraceElement.getLineNumber())
                .with("invokeMethodName", stackTraceElement.getMethodName())
                .with(lambdaInfos);
    }

    @SneakyThrows
    static public <R> R call(SCallable<R> callable) {
        recordInvokeSignature(callable);
        R result = null;
        Throwable throwableBackup = null;
        try {
            result = callable.call();
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            } else if (targetException instanceof SysException) {
                throw (SysException) ex.getTargetException();
            }
            throwableBackup = targetException;
            throw targetException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw throwable;
        } finally {
            if (throwableBackup == null) {
                log.with("result", Json.toJson(result)).info("");
            } else {
                log.error("", throwableBackup);
            }
        }
        return result;
    }

    @SneakyThrows
    static public <R> R call(SCallable<R> callable, Function<R, Boolean> check) {
        recordInvokeSignature(callable);
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
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            } else if (targetException instanceof SysException) {
                throw (SysException) ex.getTargetException();
            }
            throw targetException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw throwable;
        } finally {
            if (throwableBackup == null) {
                log.with("result", Json.toJson(result)).info("");
            } else {
                log.error("", throwableBackup);
            }
        }
        return result;
    }

    static public <R, T> T call(SCallable<R> callable, Function<R, Boolean> check, Function<R, T> getData) {
        recordInvokeSignature(callable);
        return getData.apply(call(callable, check));
    }

    @SneakyThrows
    static public <R, T> T fallbackableCall(SCallable<R> callable,
                                            Function<R, Boolean> check,
                                            Function<R, T> getData,
                                            T defaultValue) {
        recordInvokeSignature(callable);
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
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            } else if (targetException instanceof SysException) {
                throw (SysException) ex.getTargetException();
            }
            throw targetException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
        } finally {
            if (throwableBackup == null) {
                log.with("result", Json.toJson(result)).info("");
            } else {
                log.with("defaultValue", defaultValue).error("", throwableBackup);
            }
        }
        return defaultValue;
    }


    static public void run(SRunnable runnable) {
        recordInvokeSignature(runnable);
        Throwable throwableBackup = null;
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw throwable;
        } finally {
            if (throwableBackup == null) {
                log.info("");
            } else {
                log.error("", throwableBackup);
            }
        }
    }

    static public void fallbackableRun(SRunnable runnable) {
        recordInvokeSignature(runnable);
        Throwable throwableBackup = null;
        try {
            runnable.run();
        } catch (Throwable throwable) {
           throwableBackup = throwable;
        } finally {
            if (throwableBackup == null) {
                log.info("");
            } else {
                log.error("", throwableBackup);
            }
        }
    }
}
