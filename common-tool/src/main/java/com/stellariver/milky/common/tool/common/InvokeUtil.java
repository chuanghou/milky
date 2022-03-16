package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.utils.Json;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class InvokeUtil {

    static final Logger log = Logger.getLogger(InvokeUtil.class);

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
            throw new RuntimeException(targetException.getMessage(), targetException);
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage(), ex);
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

    static public <T> T call(SCallable<T> callable) {
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
            throw new RuntimeException(targetException.getMessage(), targetException);
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    static public <T> T call(SCallable<T> callable, Function<T, Boolean> check) {
        autoLog(callable);
        T result;
        try {
            result = callable.call();
            if (!check.apply(result)) {
                throw new RuntimeException(Json.toString(result));
            }
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new RuntimeException(targetException.getMessage(), targetException);
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return result;
    }


    static public void run(SRunnable runnable) {
        autoLog(runnable);
        try {
            runnable.run();
        } catch (BizException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    static public <T> T fallbackableCall(SCallable<T> callable, T defaultValue) {
        autoLog(callable);
        try {
            return callable.call();
        } catch (Throwable ex) {
            log.with("runnable", callable.toString()).error(ex.getMessage(), ex);
        }
        return defaultValue;
    }

    static public void fallbackableRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ex) {
            log.with("runnable", runnable.toString()).error(ex.getMessage(), ex);
        }
    }

}
