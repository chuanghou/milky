package com.stellariver.milky.aspectj.tool.rate.limit;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
public class AnnotationRateLimitAspect extends AbstractRateLimitAspect{

    @Pointcut("@annotation(com.stellariver.milky.aspectj.tool.rate.limit.RateLimit)")
    public void pointCut() {}

}

