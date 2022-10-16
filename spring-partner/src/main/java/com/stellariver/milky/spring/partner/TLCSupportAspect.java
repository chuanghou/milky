package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TLCSupportAspect {

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.EnableTLC)")
    void pointCut() {}

    final List<BaseQuery<?, ?>> baseQueries;

    @SuppressWarnings("all")
    private Map<Method, Set<BaseQuery<?, ?>>> disableMethodCache = new ConcurrentHashMap<>();

    @SneakyThrows
    @Around("pointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {
        Object result;
        Set<BaseQuery<?, ?>> disableBaseQueries = this.getDisableBaseQueries(pjp);
        disableBaseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            result = pjp.proceed();
        } finally {
            disableBaseQueries.forEach(BaseQuery::clearThreadLocal);
        }
        return result;
    }

    private Set<BaseQuery<?, ?>> getDisableBaseQueries(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Set<BaseQuery<?, ?>> disableBaseQueries = disableMethodCache.get(method);
        if (disableBaseQueries != null) {
            return disableBaseQueries;
        }
        disableBaseQueries = new HashSet<>();
        EnableTLC annoForClazz= pjp.getClass().getAnnotation(EnableTLC.class);
        EnableTLC annoForMethod = method.getAnnotation(EnableTLC.class);
        if (annoForMethod != null) {
            Set<Class<? extends BaseQuery<?, ?>>> classes = Arrays.stream(annoForMethod.disableBaseQueries()).collect(Collectors.toSet());
            Set<BaseQuery<?, ?>> methodBaseQueries = baseQueries.stream().filter(baseQuery -> classes.contains(baseQuery.getClass())).collect(Collectors.toSet());
            disableBaseQueries.addAll(methodBaseQueries);
        }
        if (annoForClazz != null) {
            Set<Class<? extends BaseQuery<?, ?>>> classes = Arrays.stream(annoForClazz.disableBaseQueries()).collect(Collectors.toSet());
            Set<BaseQuery<?, ?>> classBaseQueries = baseQueries.stream().filter(baseQuery -> classes.contains(baseQuery.getClass())).collect(Collectors.toSet());
            disableBaseQueries.addAll(classBaseQueries);
        }
        disableMethodCache.put(method, disableBaseQueries);
        return disableBaseQueries;
    }

}
