package com.stellariver.milky.common.tool.slambda;

import com.stellariver.milky.common.tool.exception.SysException;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author houchuang
 */
public class SLambda {

    @SneakyThrows({InvocationTargetException.class, IllegalAccessException.class, NoSuchMethodException.class})
    public static <T extends Serializable> Map<String, Object> resolveArgs(T lambda) {
        Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda sLambda = (SerializedLambda) method.invoke(lambda);
        Map<String, Object> result = new HashMap<>(8);
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
