package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.aspectj.tool.BaseAspect;
import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.tool.common.BaseQuery;
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
public abstract class AbstractTLCAspect extends BaseAspect {

    private final Map<Method, Set<BaseQuery<?, ?>>> enableBqs = new ConcurrentHashMap<>();

    @Pointcut
    public abstract void pointCut();

    @Around("pointCut() && !ignorePointCut()")
    public Object tlc(ProceedingJoinPoint pjp) throws Throwable {
        TLCConfig tlcConfig = tlcConfig(pjp);
        return doProceed(pjp, tlcConfig);
    }

    private Object doProceed(ProceedingJoinPoint pjp, TLCConfig tlcConfig) throws Throwable{
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object result;

        Set<BaseQuery<?, ?>> enableBaseQueries = enableBqs.computeIfAbsent(method, m -> {
            Set<Class<? extends BaseQuery<?, ?>>> disableBQCs = tlcConfig.getDisableBaseQueries();
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

    public TLCConfig tlcConfig(ProceedingJoinPoint pjp) {
        return TLCConfig.defaultConfig();
    }

}
