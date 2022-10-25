package com.stellariver.milky.common.tool;

import com.stellariver.milky.common.tool.exception.SysException;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SLambda {

    @SneakyThrows
    public static <T extends Serializable> Map<String, Object> resolve(T lambda) {
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda sLambda = (SerializedLambda) method.invoke(lambda);
        Map<String, Object> result = new HashMap<>();
        if (sLambda.getCapturedArgCount() == 0) {
            return result;
        }
        SysException.trueThrow(sLambda.getCapturedArgCount() > 6, "too much parameters!");
        for (int i = 1; i < sLambda.getCapturedArgCount(); i++) {
            result.put("arg" + (i - 1), sLambda.getCapturedArg(i));
        }
        return result;
    }

}
