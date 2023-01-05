package com.stellariver.milky.common.tool.stable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.If;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.NonNull;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public abstract class AbstractStableSupport{

    static public AbstractStableSupport abstractStableSupport;

    public void setAbstractStableSupport(AbstractStableSupport support) {
        abstractStableSupport = support;
    }

    private Map<String, CbConfig> cbConfigs = new HashMap<>();

    private Map<String, RlConfig> rlConfigs = new HashMap<>();

    private final Cache<String, RateLimiterWrapper> rateLimiters = CacheBuilder.newBuilder().softValues().build();

    private final Cache<String, CircuitBreaker> circuitBreakers = CacheBuilder.newBuilder().softValues().build();

    public String key(ProceedingJoinPoint pjp) {
        return pjp.toLongString();
    }

    public String key(Method method) {
        return method.toString();
    }

    @Nullable
    public RateLimiterWrapper rateLimiter(@NonNull String key) {
        RateLimiterWrapper rateLimiterWrapper = rateLimiters.getIfPresent(key);
        if (rateLimiterWrapper == null) {
            rateLimiterWrapper = buildRateLimiterWrapper(key);
            if (rateLimiterWrapper != null) {
                rateLimiters.put(key, rateLimiterWrapper);
            }
        }
        return rateLimiterWrapper;
    }

    @Nullable
    public CircuitBreaker circuitBreaker(@NonNull String key) {
        CircuitBreaker circuitBreaker = circuitBreakers.getIfPresent(key);
        if (circuitBreaker == null) {
            circuitBreaker = buildCircuitBreaker(key);
            if (circuitBreaker != null) {
                circuitBreakers.put(key, circuitBreaker);
            }
        }
        return circuitBreaker;
    }

    protected void adjustCircuitBreakerState(CbConfig config) {
        CircuitBreaker circuitBreaker = circuitBreaker(config.getKey());
        if (config.getOperation() == CbConfig.Operation.FORCE_CLOSE) {
            circuitBreaker.transitionToClosedState();
        } else if (config.getOperation() == CbConfig.Operation.FORCE_OPEN) {
            circuitBreaker.transitionToDisabledState();
        } else if (config.getOperation() == CbConfig.Operation.RESET) {
            circuitBreaker.reset();
        } else {
            throw new SysException(ErrorEnumsBase.UNREACHABLE_CODE);
        }
    }

    @Nullable
    protected CircuitBreaker buildCircuitBreaker(String key) {
        if (!cbConfigs.containsKey(key)) {
            return null;
        }
        CbConfig cbConfig = cbConfigs.get(key);
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

        return CircuitBreaker.of(cbConfig.getKey(), builder.build());
    }

    protected RateLimiterWrapper buildRateLimiterWrapper(@NonNull String key) {
        if (!rlConfigs.containsKey(key)){
            return null;
        }
        RlConfig rlConfig = rlConfigs.get(key);
        RateLimiter rateLimiter = RateLimiter.create(rlConfigs.get(key).getQps());
        return RateLimiterWrapper.builder().rateLimiter(rateLimiter)
                .strategy(Kit.op(rlConfig.getStrategy()).orElse(RlConfig.Strategy.FAIL_WAITING))
                .timeout(Kit.op(rlConfig.getTimeOut()).orElseGet(() -> Duration.ofSeconds(3)))
                .warningThreshold(Kit.op(rlConfig.getWarningThreshold()).orElseGet(() -> Duration.ofSeconds(3)))
                .build();
    }

    protected void update(StableConfig stableConfig) {
        Map<String, RlConfig> oldRlConfigs = rlConfigs;
        this.rlConfigs = Kit.op(stableConfig).map(StableConfig::getRlConfigs).orElseGet(ArrayList::new)
                .stream().collect(Collectors.toMap(RlConfig::getKey, Function.identity()));
        this.rlConfigs.keySet().stream().filter(k -> Kit.notEq(oldRlConfigs.get(k), this.rlConfigs.get(k)))
                .forEach(rateLimiters::invalidate);

        Map<String, CbConfig> oldCbConfigs = cbConfigs;
        this.cbConfigs = Kit.op(stableConfig).map(StableConfig::getCbConfigs).orElseGet(ArrayList::new)
                .stream().collect(Collectors.toMap(CbConfig::getKey, Function.identity()));
        this.rlConfigs.keySet().stream().filter(k -> Kit.notEq(oldCbConfigs.get(k), this.cbConfigs.get(k)))
                .forEach(circuitBreakers::invalidate);

    }

}
