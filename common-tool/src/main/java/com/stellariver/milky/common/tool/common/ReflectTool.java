package com.stellariver.milky.common.tool.common;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j
public class ReflectTool {

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
}
