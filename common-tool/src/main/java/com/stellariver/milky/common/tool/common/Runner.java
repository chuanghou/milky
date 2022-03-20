package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.Json;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class Runner {

    static final Logger log = Logger.getLogger(Runner.class);

    static public Object invoke(Object bean, Method method, Object... params) {
        try {
            return method.invoke(bean, params);
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new SysException(targetException);
        } catch (Throwable ex) {
            throw new SysException(ex);
        }
    }

    static private void autoLog(Serializable lambda) {
        Map<String, String> lambdaInfos = SLambda.resolve(lambda);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        log.with("invokeClassName", stackTraceElement.getClassName())
                .with("invokeLineNumber", stackTraceElement.getLineNumber())
                .with("invokeMethodNumber", stackTraceElement.getMethodName())
                .with(lambdaInfos).info("auto invoke log");
    }

    static public <R> R call(SCallable<R> callable) {
        autoLog(callable);
        try {
            return callable.call();
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new SysException(targetException);
        } catch (Throwable throwable) {
            throw new SysException(throwable);
        }
    }

    static public <R> R call(SCallable<R> callable, Function<R, Boolean> check) {
        autoLog(callable);
        R result;
        try {
            result = callable.call();
            if (!check.apply(result)) {
                throw new SysException(Json.toJson(result));
            }
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new SysException(targetException);
        } catch (Throwable throwable) {
            throw new SysException(throwable);
        }
        return result;
    }

    static public <R, T> T call(SCallable<R> callable, Function<R, Boolean> check, Function<R, T> getData) {
        autoLog(callable);
        return getData.apply(call(callable, check));
    }

    static public <R, T> T fallbackableCall(SCallable<R> callable,
                                            Function<R, Boolean> check,
                                            Function<R, T> getData,
                                            T defaultValue) {
        autoLog(callable);
        try {
            R result = callable.call();
            if (check.apply(result)) {
                return getData.apply(callable.call());
            }
            return defaultValue;
        } catch (Throwable ex) {
            log.with("callable", callable.toString()).error(ex.getMessage(), ex);
        }
        return defaultValue;
    }


    static public void run(SRunnable runnable) {
        autoLog(runnable);
        try {
            runnable.run();
        } catch (BizException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new SysException(ex);
        }
    }

    static public void fallbackableRun(SRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ex) {
            log.with("runnable", Json.toJson(SLambda.resolve(runnable))).error(ex.getMessage(), ex);
        }
    }

}
