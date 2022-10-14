package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.domain.support.util.BeanUtil;
import com.stellariver.milky.spring.partner.EnableThreadLocalCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThreadLocalCacheableSupportAspect {

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.EnableThreadLocalCache)")
    void pointCut() {}

    @SneakyThrows
    @Around("pointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {
        Object result;
        MethodSignature pjpSignature = (MethodSignature) pjp.getSignature();
        Class<? extends BaseQuery<?, ?>>[] bQClasses = pjpSignature.getMethod()
                .getAnnotation(EnableThreadLocalCache.class).enableBaseQueries();
        List<? extends BaseQuery<?, ?>> baseQueries = Arrays.stream(bQClasses)
                .map(BeanUtil::getBean).collect(Collectors.toList());
        baseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            result = pjp.proceed();
        } finally {
            baseQueries.forEach(BaseQuery::clearThreadLocal);
        }
        return result;
    }

}
