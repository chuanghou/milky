package com.stellariver.milky.validate.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.util.Collect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
public class TLCAspect {

    private Set<BaseQuery<?, ?>> enableBaseQueries;

    private boolean init = false;

    @Pointcut("@annotation(com.stellariver.milky.validate.tool.tlc.EnableTLC)")
    void pointCut() {}

    @Around("pointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        if (!init) {
            EnableTLC enableTLC = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(EnableTLC.class);
            Set<Class<? extends BaseQuery<?, ?>>> enableBQCs = Collect.asSet(enableTLC.enableBaseQueries());
            enableBaseQueries = BeanUtil.getBeansOfType(BaseQuery.class).stream()
                    .filter(aBQ -> enableBQCs.contains(aBQ.getClass())).map(bq -> (BaseQuery<?, ?>) bq).collect(Collectors.toSet());
            init = true;
        }

        enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            result = pjp.proceed();
        } finally {
            enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
        }
        return result;
    }

}
