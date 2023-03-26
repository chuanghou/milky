package com.stellariver.milky.validate.tool.limit;

import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
public class RateLimitSupport {

    private MilkyStableSupport milkyStableSupport;

    @Pointcut("@annotation(com.stellariver.milky.validate.tool.limit.EnableRateLimit)")
    public void pointCut() {}

    @Around("pointCut()")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        if (milkyStableSupport == null) {
            milkyStableSupport = BeanUtil.getBean(MilkyStableSupport.class);
        }
        String key = milkyStableSupport.ruleId(pjp);
        RateLimiterWrapper rateLimiter = milkyStableSupport.rateLimiter(key);
        if (rateLimiter != null) {
            rateLimiter.acquire();
        }
        return pjp.proceed();
    }

}

