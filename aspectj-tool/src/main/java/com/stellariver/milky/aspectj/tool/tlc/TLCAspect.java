package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.util.Collect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class TLCAspect {
    private final Map<Method, Set<BaseQuery<?, ?>>> enableBqs = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.stellariver.milky.aspectj.tool.tlc.EnableTLC)")
    void pointCut() {}

    @Around("pointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object result;

        Set<BaseQuery<?, ?>> enableBaseQueries = enableBqs.computeIfAbsent(method, m -> {
            EnableTLC enableTLC = m.getAnnotation(EnableTLC.class);
            Set<Class<? extends BaseQuery<?, ?>>> disableBQCs = Collect.asSet(enableTLC.disableBaseQueries());
            return BeanUtil.getBeansOfType(BaseQuery.class).stream()
                    .filter(aBQ -> disableBQCs.contains(aBQ.getClass()))
                    .map(bq -> (BaseQuery<?, ?>) bq).collect(Collectors.toSet());
        });
        enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            result = pjp.proceed();
        } finally {
            enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
        }
        return result;
    }

}
