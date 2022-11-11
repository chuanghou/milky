package com.stellariver.milky.spring.partner.limit;

import com.google.common.util.concurrent.RateLimiter;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.stable.RlConfig;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

@SuppressWarnings("all")
@Order
@Aspect
@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitSupport {

    final AbstractStableSupport abstractStableSupport;

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.limit.EnableRateLimit)")
    public void pointCut() {}

    @Around("pointCut()&& @annotation(enableRateLimit)")
    public Object rateLimit(ProceedingJoinPoint pjp, EnableRateLimit enableRateLimit) throws Throwable {
        String key = abstractStableSupport.key(pjp);
        RateLimiterWrapper rateLimiter = abstractStableSupport.rateLimiter(key);
        if (rateLimiter != null) {
            rateLimiter.acquire(Duration.of((long) enableRateLimit.warningWaitTime(), ChronoUnit.MILLIS));
        }
        return pjp.proceed();
    }

}

