package com.stellariver.milky.spring.partner;


import com.stellariver.milky.common.tool.util.Json;
import lombok.Builder;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class AspectTool {

    public static MethodInfo methodInfo(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = proceedingJoinPoint.getArgs();
        Map<String, String> builder = new HashMap<>();
        IntStream.range(0, args.length).forEach(index -> builder.put(parameterNames[index], Objects.toString(args[index])));
        return MethodInfo.builder().className(method.getDeclaringClass().getName())
                .methodName(method.getName()).paramString(Json.toString(builder))
                .method(method).build();
    }

    @Data
    @Builder
    static public class MethodInfo {

        private String className;

        private String methodName;

        private String paramString;

        private Method method;

        public String methodReference() {return className + "#" + methodName;}

    }
}
