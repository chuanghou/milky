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
import lombok.Setter;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public abstract class AbstractStableSupport{

    static public AbstractStableSupport abstractStableSupport;

    public void setAbstractStableSupport(AbstractStableSupport support) {
        abstractStableSupport = support;
    }

    @Setter
    private Map<String, CbConfig> cbConfigs = new HashMap<>();

    @Setter
    private Map<String, RlConfig> rlConfigs = new HashMap<>();

    private final Cache<String, RateLimiterWrapper> rateLimiters = CacheBuilder.newBuilder().softValues().build();

    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public String key(ProceedingJoinPoint pjp) {
        return pjp.toLongString();
    }

    public String key(Method method) {
        return method.toString();
    }

    @SneakyThrows
    @Nullable
    public RateLimiterWrapper rateLimiter(@NonNull String key) {
        return rlConfigs.containsKey(key) ? rateLimiters.get(key, () -> buildRateLimiterWrapper(key)) : null;
    }

    @Nullable
    public CircuitBreaker circuitBreaker(@NonNull String key) {
        return cbConfigs.containsKey(key) ? circuitBreakers.computeIfAbsent(key, this::buildCircuitBreaker) : null;
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
            throw new SysException(ErrorEnumsBase.NOT_REACHED_PART);
        }
    }

    protected CircuitBreaker buildCircuitBreaker(String key) {
        CbConfig cbConfig = cbConfigs.get(key);
        SysException.nullThrow(cbConfig);
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
        RlConfig rlConfig = rlConfigs.get(key);
        SysException.nullThrow(rlConfig);
        RateLimiter rateLimiter = RateLimiter.create(rlConfigs.get(key).getQps());
        return RateLimiterWrapper.builder().rateLimiter(rateLimiter)
                .strategy(Kit.op(rlConfig.getStrategy()).orElse(RlConfig.Strategy.FAIL_WAITING))
                .timeout(Kit.op(rlConfig.getDuration()).orElse(Duration.ofSeconds(3)))
                .build();
    }

    protected void clearRateLimiters() {
        rateLimiters.invalidateAll();
    }

    protected void clearCircuitBreakers() {
        circuitBreakers.clear();
    }

}
