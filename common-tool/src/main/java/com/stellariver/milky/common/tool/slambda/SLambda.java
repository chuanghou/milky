package com.stellariver.milky.common.tool.slambda;

import com.stellariver.milky.common.tool.util.Reflect;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * @author houchuang
 */
public class SLambda {


    @SneakyThrows
    public static <T extends Serializable> SerializedLambda resolveArgs(T lambda) {
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(lambda);
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

}
