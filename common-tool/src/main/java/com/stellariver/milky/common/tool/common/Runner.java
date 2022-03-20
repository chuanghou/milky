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

    static private void recordInvokeSignature(Serializable lambda) {
        Map<String, String> lambdaInfos = SLambda.resolve(lambda);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        log.with("invokeClassName", stackTraceElement.getClassName())
                .with("invokeLineNumber", stackTraceElement.getLineNumber())
                .with("invokeMethodNumber", stackTraceElement.getMethodName())
                .with(lambdaInfos);
    }

    static public <R> R call(SCallable<R> callable) {
        recordInvokeSignature(callable);
        R result = null;
        Throwable throwableBackup = null;
        try {
            result = callable.call();
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throwableBackup = targetException;
            throw new SysException(targetException);
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw new SysException(throwable);
        } finally {
            if (throwableBackup == null) {
                log.with("result", Json.toJson(result)).info("");
            } else {
                log.error("", throwableBackup);
            }
        }
        return result;
    }

    static public <R> R call(SCallable<R> callable, Function<R, Boolean> check) {
        recordInvokeSignature(callable);
        R result = null;
        Throwable throwableBackup = null;
        try {
            result = callable.call();
            if (!check.apply(result)) {
                throw new SysException(Json.toJson(result));
            }
        } catch (BizException ex) {
            throwableBackup = ex;
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            throwableBackup = targetException;
            if (targetException instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new SysException(targetException);
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw new SysException(throwable);
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
        } catch (BizException bizException) {
            throwableBackup = bizException;
            throw bizException;
        } catch (Throwable throwable) {
            throwableBackup = throwable;
            throw new SysException(throwable);
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
