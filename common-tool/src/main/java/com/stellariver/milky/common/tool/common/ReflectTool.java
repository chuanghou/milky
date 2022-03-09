package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ReflectTool {

    static final Logger log = Logger.getLogger(ReflectTool.class);

    static public Object invokeBeanMethod(Object bean, Method method, Object... params) {
        try {
            return method.invoke(bean, params);
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new BizException(ErrorCodeBase.UNKNOWN, ex.getTargetException());
        } catch (Throwable ex) {
            throw new BizException(ErrorCodeBase.UNKNOWN, ex);
        }
    }

    static public <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (BizException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof BizException) {
                throw (BizException) ex.getTargetException();
            }
            throw new BizException(ErrorCodeBase.UNKNOWN, ex.getTargetException());
        } catch (Throwable ex) {
            throw new BizException(ErrorCodeBase.UNKNOWN, ex);
        }
    }

    static public void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (BizException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BizException(ErrorCodeBase.UNKNOWN, ex);
        }
    }

    static public <T> T downGradeCall(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable ex) {
            log.with("runnable", callable.toString()).error(ex.getMessage(), ex);
        }
        return null;
    }

    static public void downGradeRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ex) {
            log.with("runnable", runnable.toString()).error(ex.getMessage(), ex);
        }
    }
}
