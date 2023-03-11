package com.stellariver.milky.common.tool.stable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.If;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.NonNull;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MilkyStableSupport {

    private StableConfig stableConfig = new StableConfig();

    private final StableConfigReader stableConfigReader;

    private final Cache<String, RateLimiterWrapper> rateLimiters = CacheBuilder.newBuilder().softValues().build();

    private final Cache<String, CircuitBreaker> circuitBreakers = CacheBuilder.newBuilder().softValues().build();

    public MilkyStableSupport(@NonNull StableConfigReader stableConfigReader) {
        this.stableConfigReader = stableConfigReader;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        updateConfig();
        scheduler.scheduleAtFixedRate(this::updateConfig, 10, 10, TimeUnit.SECONDS);
    }

    private void updateConfig() {
        StableConfig readStableConfig = stableConfigReader.read();
        if (!Kit.eq(readStableConfig, stableConfig)) {
            stableConfig = stableConfigReader.read();
            rateLimiters.invalidateAll();
            circuitBreakers.invalidateAll();
        }
    }

    public String ruleId(ProceedingJoinPoint pjp) {
        return pjp.toLongString();
    }

    public String ruleId(Method method) {
        return method.toString();
    }

    @Nullable
    public RateLimiterWrapper rateLimiter(@NonNull String ruleId) {
        return rateLimiter(ruleId, null);
    }

    @Nullable
    @SuppressWarnings("all")
    public RateLimiterWrapper rateLimiter(@NonNull String ruleId, @Nullable String key) {

        String id = String.format("%s_%s", ruleId, key);
        RateLimiterWrapper rateLimiterWrapper = rateLimiters.getIfPresent(id);
        if (rateLimiterWrapper != null) {
            return rateLimiterWrapper;
        }

        Map<String, RlConfig> rlConfigs = stableConfig.getRlConfigs();
        boolean contains = rlConfigs.containsKey(ruleId);
        if (!contains)  {
            return null;
        }

        RlConfig rlConfig = rlConfigs.get(ruleId);
        RateLimiter rateLimiter = RateLimiter.create(rlConfigs.get(ruleId).getQps());
        rateLimiterWrapper = RateLimiterWrapper.builder().id(id).rateLimiter(rateLimiter)
                .strategy(Kit.op(rlConfig.getStrategy()).orElse(RlConfig.Strategy.FAIL_WAITING))
                .timeout(Kit.op(rlConfig.getTimeOut()).orElseGet(() -> Duration.ofSeconds(3)))
                .warningThreshold(Kit.op(rlConfig.getWarningThreshold()).orElseGet(() -> Duration.ofSeconds(3)))
                .build();
        rateLimiters.put(id, rateLimiterWrapper);
        return rateLimiterWrapper;
    }

    @Nullable
    public CircuitBreaker circuitBreaker(@NonNull String ruleId) {
        return circuitBreaker(ruleId,null);
    }

    @Nullable
    public CircuitBreaker circuitBreaker(@NonNull String ruleId, @Nullable String key) {

        String id = String.format("%s_%s", ruleId, key);
        CircuitBreaker circuitBreaker = circuitBreakers.getIfPresent(id);
        if (circuitBreaker != null) {
            return circuitBreaker;
        }

        Map<String, CbConfig> cbConfigs = stableConfig.getCbConfigs();
        if (!cbConfigs.containsKey(ruleId)) {
            return null;
        }

        CbConfig cbConfig = cbConfigs.get(ruleId);
        CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();
        If.isTrue(cbConfig.getSlidingWindowType() != null, () -> builder.slidingWindowType(cbConfig.getSlidingWindowType()));
        If.isTrue(cbConfig.getSlidingWindowSize() != null, () -> builder.slidingWindowSize(cbConfig.getSlidingWindowSize()));
        If.isTrue(cbConfig.getMinimumNumberOfCalls() != null, () -> builder.minimumNumberOfCalls(cbConfig.getMinimumNumberOfCalls()));
        If.isTrue(cbConfig.getFailureRateThreshold() != null, () -> builder.failureRateThreshold(cbConfig.getFailureRateThreshold()));
        If.isTrue(cbConfig.getSlowCallRateThreshold() != null, () -> builder.slowCallDurationThreshold(cbConfig.getSlowCallDurationThreshold()));
        If.isTrue(cbConfig.getSlowCallRateThreshold() == null, () -> builder.slowCallDurationThreshold(Duration.ofSeconds(5L)));
        If.isTrue(cbConfig.getWaitIntervalInOpenState() != null, () -> builder.waitDurationInOpenState(cbConfig.getWaitIntervalInOpenState()));
        If.isTrue(cbConfig.getPermittedNumberOfCallsInHalfOpenState() != null,
                () -> builder.permittedNumberOfCallsInHalfOpenState(cbConfig.getPermittedNumberOfCallsInHalfOpenState()));
        If.isTrue(cbConfig.getAutomaticTransitionFromOpenToHalfOpenEnabled() != null,
                () -> builder.automaticTransitionFromOpenToHalfOpenEnabled(cbConfig.getAutomaticTransitionFromOpenToHalfOpenEnabled()));
        If.isTrue(cbConfig.getAutomaticTransitionFromOpenToHalfOpenEnabled() == null,
                () -> builder.automaticTransitionFromOpenToHalfOpenEnabled(true));

        circuitBreaker = CircuitBreaker.of(cbConfig.getRuleId(), builder.build());
        circuitBreakers.put(id, circuitBreaker);
        return circuitBreaker;
    }

}
