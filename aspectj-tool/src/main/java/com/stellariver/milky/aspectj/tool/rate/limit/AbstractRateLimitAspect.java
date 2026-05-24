package com.stellariver.milky.aspectj.tool.rate.limit;

import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Optional;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
public abstract class AbstractRateLimitAspect {

    private MilkyStableSupport milkyStableSupport;

    private volatile boolean init = false;

    private final Object lock = new Object();

    @Pointcut
    public abstract void pointCut();

    @Around("pointCut()")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        if (!init) {
            synchronized (lock) {
                if (!init) {
                    Optional<MilkyStableSupport> beanOptional = BeanUtil.getBeanOptional(MilkyStableSupport.class);
                    beanOptional.ifPresent(stableSupport -> milkyStableSupport = stableSupport);
                    init = true;
                }
            }
        }
        if (milkyStableSupport != null) {
            String key = milkyStableSupport.ruleId(pjp);
            RateLimiterWrapper rateLimiter = milkyStableSupport.rateLimiter(key);
            if (rateLimiter != null) {
                rateLimiter.acquire();
            }
        }
        return pjp.proceed();
    }

}
