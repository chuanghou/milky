package com.stellariver.milky.spring.partner.limit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.ErrorEnumsBase;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@SuppressWarnings("all")
@Order
@Aspect
@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitSupport {

    static Cache<String, RateLimiter> rateLimiterContainer = CacheBuilder.newBuilder().softValues().build();

    final RateLimitConfigTunnel rateLimitConfigTunnel;

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.limit.EnableRateLimit)")
    public void pointCut() {}

    @Around("pointCut()&& @annotation(enableRateLimit)")
    public Object rateLimit(ProceedingJoinPoint pjp, EnableRateLimit enableRateLimit) throws Throwable {
        String key = rateLimitConfigTunnel.key(pjp);
        Integer qps = rateLimitConfigTunnel.qps(key);
        RateLimiter rateLimiter = rateLimiterContainer.get(key, () -> RateLimiter.create(qps));
        Object[] args = pjp.getArgs();
        if (enableRateLimit.strategy() == Strategy.FAIL_FAST) {
            boolean acquire = rateLimiter.tryAcquire();
            BizException.falseThrow(acquire, ErrorEnumsBase.FLOW_CONFIG);
        } else {
            double acquire = rateLimiter.acquire();
            if (acquire > enableRateLimit.warningWaitTime()) {
                IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));
                String logTag = pjp.getSignature().getName();
                log.cost(acquire).error(logTag);
            }
        }
        return pjp.proceed();
    }

    public void resetRateLimiterContainer() {
        rateLimiterContainer.invalidateAll();
    }

    static public boolean failFastRateLimit(String key) throws ExecutionException {
        RateLimitConfigTunnel tunnel = BeanUtil.getBean(RateLimitConfigTunnel.class);
        Integer qps = tunnel.qps(key);
        RateLimiter rateLimiter = rateLimiterContainer.get(key, () -> RateLimiter.create(qps));
        return rateLimiter.tryAcquire();
    }

    static public void failWaittingRateLimit(String key) throws ExecutionException {
        RateLimitConfigTunnel tunnel = BeanUtil.getBean(RateLimitConfigTunnel.class);
        Integer qps = tunnel.qps(key);
        RateLimiter rateLimiter = rateLimiterContainer.get(key, () -> RateLimiter.create(qps));
        rateLimiter.acquire();
    }

    public enum Strategy {

        FAIL_FAST,

        FAIL_WAITING

    }

}

