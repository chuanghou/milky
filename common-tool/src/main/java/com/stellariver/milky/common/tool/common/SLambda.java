package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.util.Json;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SLambda {

    @SneakyThrows
    public static <T extends Serializable> Map<String, String> resolve(T lambda) {
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda sLambda = (SerializedLambda) method.invoke(lambda);
        Map<String, String> result = new HashMap<>();
        if (sLambda.getCapturedArgCount() == 0) {
            return result;
        }
        Object bean = sLambda.getCapturedArg(0);
        result.put("bean", Json.toJson(bean));
        result.put("beanClassName", bean.getClass().getName());
        for (int i = 1; i < sLambda.getCapturedArgCount() - 1; i++) {
            result.put("arg" + (i - 1), Json.toJson(sLambda.getCapturedArg(i)));
        }
        return result;
    }

}
