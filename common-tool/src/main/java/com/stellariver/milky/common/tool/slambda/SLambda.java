package com.stellariver.milky.common.tool.slambda;

import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Reflect;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author houchuang
 */
public class SLambda {

    @SneakyThrows({InvocationTargetException.class, IllegalAccessException.class, NoSuchMethodException.class})
    public static <T extends Serializable> List<Object> resolveArgs(T lambda) {
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda sLambda = (SerializedLambda) method.invoke(lambda);
        return IntStream.range(0, sLambda.getCapturedArgCount()).mapToObj(sLambda::getCapturedArg).collect(Collectors.toList());
    }

    public static <T> LambdaMeta extract(SFunction<T, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectLambdaMeta((SerializedLambda) Reflect.setAccessible(method).invoke(func));
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(com.stellariver.milky.common.tool.slambda.SerializedLambda.extract(func));
        }
    }

    public static <T> LambdaMeta extract(BiSFunction<T, ?, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectLambdaMeta((SerializedLambda) Reflect.setAccessible(method).invoke(func));
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(com.stellariver.milky.common.tool.slambda.SerializedLambda.extract(func));
        }
    }

}
