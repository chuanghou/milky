package com.stellariver.milky.common.tool.stable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
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
import java.util.stream.Collectors;

@SuppressWarnings("all")
public abstract class AbstractStableSupport{

    private StableConfig stableConfig = new StableConfig();

    private final StableConfigReader stableConfigReader;

    private final Cache<String, RateLimiterWrapper> rateLimiters = CacheBuilder.newBuilder().softValues().build();

    private final Cache<String, CircuitBreaker> circuitBreakers = CacheBuilder.newBuilder().softValues().build();

    public AbstractStableSupport(StableConfigReader stableConfigReader) {
        this.stableConfigReader = stableConfigReader;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            StableConfig pushedStableConfig = stableConfigReader.read();
            Map<String, CbConfig> newCbConfigs = pushedStableConfig.getCbConfigs();
            if (!stableConfig.getCbConfigs().equals(pushedStableConfig.getCbConfigs())) {
                Map<String, CbConfig> cbConfigs = stableConfig.getCbConfigs();
                Map<String, CbConfig> pushedCbConfigs = pushedStableConfig.getCbConfigs();
                Set<String> cbConfigKetSet = cbConfigs.keySet();
                Set<String> pushedCbConfigKeySet = pushedCbConfigs.keySet();
                Set<String> deletedKeys = Collect.diff(cbConfigKetSet, pushedCbConfigKeySet);
                Set<String> interUpdatedKeys = Collect.inter(cbConfigKetSet, pushedCbConfigKeySet).stream()
                        .filter(k -> cbConfigs.get(k).equals(pushedCbConfigs.get(k))).collect(Collectors.toSet());
                Set<String> addedKeys = Collect.inter(pushedCbConfigKeySet, cbConfigKetSet);
                stableConfig.setCbConfigs(pushedCbConfigs);
                deletedKeys.stream().forEach(rateLimiters::invalidate);
                Arrays.asList(interUpdatedKeys, addedKeys).stream().flatMap(Collection::stream).forEach(this::buildCircuitBreaker);
            }
            if (!stableConfig.getRlConfigs().equals(pushedStableConfig.getRlConfigs())) {
                Map<String, RlConfig> rlConfigs = stableConfig.getRlConfigs();
                Map<String, RlConfig> pushedRlConfigs = pushedStableConfig.getRlConfigs();
                Set<String> rlConfigKetSet = rlConfigs.keySet();
                Set<String> pushedRlConfigKeySet = pushedRlConfigs.keySet();
                Set<String> deletedKeys = Collect.diff(rlConfigKetSet, pushedRlConfigKeySet);
                Set<String> interUpdatedKeys = Collect.inter(rlConfigKetSet, pushedRlConfigKeySet).stream()
                        .filter(k -> rlConfigs.get(k).equals(pushedRlConfigs.get(k))).collect(Collectors.toSet());
                Set<String> addedKeys = Collect.inter(pushedRlConfigKeySet, rlConfigKetSet);
                stableConfig.setRlConfigs(rlConfigs);
                deletedKeys.stream().forEach(rateLimiters::invalidate);
                Arrays.asList(interUpdatedKeys, addedKeys).stream().flatMap(Collection::stream).forEach(this::buildRateLimiterWrapper);
            }

        }, 10, 10, TimeUnit.SECONDS);
    }

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
        Map<String, CbConfig> cbConfigs = stableConfig.getCbConfigs();
        if (cbConfigs.containsKey(key)) {
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
        Map<String, RlConfig> rlConfigs = stableConfig.getRlConfigs();
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


}
