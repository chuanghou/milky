package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.tool.common.BaseQuery;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 按方法配置，在调用链内为指定 {@link BaseQuery} 启用线程级缓存并在 finally 中清理。
 *
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public abstract class AbstractTLCAspect {

    private final Map<Method, Set<BaseQuery<?, ?>>> threadLocalBaseQueriesByMethod = new ConcurrentHashMap<>();

    @Pointcut
    public abstract void pointCut();

    @Around("pointCut()")
    public Object tlc(ProceedingJoinPoint pjp) throws Throwable {
        TLCConfig tlcConfig = tlcConfig(pjp);
        return doProceed(pjp, tlcConfig);
    }

    private Object doProceed(ProceedingJoinPoint pjp, TLCConfig tlcConfig) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Set<BaseQuery<?, ?>> threadLocalBaseQueries = threadLocalBaseQueriesByMethod.computeIfAbsent(method,
                m -> resolveThreadLocalBaseQueries(tlcConfig));
        threadLocalBaseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            return pjp.proceed();
        } finally {
            threadLocalBaseQueries.forEach(BaseQuery::clearThreadLocal);
        }
    }

    private static Set<BaseQuery<?, ?>> resolveThreadLocalBaseQueries(TLCConfig tlcConfig) {
        Set<Class<? extends BaseQuery<?, ?>>> types = tlcConfig.getThreadLocalBaseQueryTypes();
        if (types.isEmpty()) {
            return Collections.emptySet();
        }
        return BeanUtil.getBeansOfType(BaseQuery.class).stream()
                .filter(bq -> types.contains(bq.getClass()))
                .map(bq -> (BaseQuery<?, ?>) bq)
                .collect(Collectors.toSet());
    }

    public TLCConfig tlcConfig(ProceedingJoinPoint pjp) {
        return TLCConfig.defaultConfig();
    }

}
