package com.stellariver.milky.aspectj.tool.rate.limit;

import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Optional;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
public class AnnotationRateLimitAspect extends AbstractRateLimitAspect{

    @Pointcut("@annotation(com.stellariver.milky.aspectj.tool.rate.limit.RateLimit)")
    public void pointCut() {}

    @Override
    public RateLimitConfig rateLimitConfig(ProceedingJoinPoint pjp) {
        return RateLimitConfig.defaultConfig();
    }

}

