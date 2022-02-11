package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.log.Log;
import com.stellariver.milky.common.tool.log.LogTagValue;
import com.stellariver.milky.common.tool.log.MDCTag;
import com.stellariver.milky.common.tool.utils.Json;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
public class ReflectTool {

    static public Object invokeAndLogInfo(ProceedingJoinPoint pjp,
                                          LogTagValue inLogTagValue, LogTagValue outLogTagValue) throws Throwable{
        MethodInfo methodInfo = ReflectTool.methodInfo(pjp);
        long start = System.currentTimeMillis();
        Object result = null;
        try {
            Log.of(() -> log.info("|class={}|method={}|paramStr={}|",
                        methodInfo.getClassName(), methodInfo.getMethodName(), methodInfo.getParamStr()))
                    .withLogTag(inLogTagValue)
                    .log();
            result = pjp.proceed();
            return result;
        } finally {
            Object finalResult = result;
            Log.of(() -> log.info("|result={}|",
                        Optional.ofNullable(finalResult).map(Json::toString).orElse("null")))
                    .withLogTag(outLogTagValue)
                    .withInfo(MDCTag.cost,System.currentTimeMillis() - start + "")
                    .log();
        }
    }

    public static MethodInfo methodInfo(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = pjp.getArgs();
        StringBuilder paramStrBuilder = new StringBuilder();
        paramStrBuilder.append("{");
        for (int i = 0; i < args.length; i++) {
            paramStrBuilder.append("\"").append(paramNames[i]).append("\"").append(":");
            paramStrBuilder.append(args[i] == null ? "null" : Json.toString(args[i]))
                    .append(i != args.length - 1 ? "," : "");
        }
        paramStrBuilder.append("}");
        String paramStr = paramStrBuilder.toString();
        return MethodInfo.builder().className(className).methodName(methodName).paramStr(paramStr).build();
    }

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

    @Data
    @Builder
    static public class MethodInfo {
        private String className;
        private String methodName;
        private String paramStr;

        @Override
        public String toString() {
            return className + "#" + methodName + "#" + paramStr;
        }
    }
}
