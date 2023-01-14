package com.stellariver.milky.spring.partner.limit;

import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

@SuppressWarnings("all")
@Order
@Aspect
@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitSupport {

    final MilkyStableSupport milkyStableSupport;

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.limit.EnableRateLimit)")
    public void pointCut() {}

    @Around("pointCut()&& @annotation(enableRateLimit)")
    public Object rateLimit(ProceedingJoinPoint pjp, EnableRateLimit enableRateLimit) throws Throwable {
        String key = milkyStableSupport.ruleId(pjp);
        RateLimiterWrapper rateLimiter = milkyStableSupport.rateLimiter(key);
        if (rateLimiter != null) {
            rateLimiter.acquire();
        }
        return pjp.proceed();
    }

}

