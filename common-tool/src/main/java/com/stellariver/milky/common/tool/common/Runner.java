package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.base.BaseEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.slambda.SCallable;
import com.stellariver.milky.common.tool.slambda.SLambda;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.RunnerExtension;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.CustomLog;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@CustomLog
@SuppressWarnings("all")
public class Runner {

    /**
     * need to be instaniate by a MilkyStableSupport impl
     */
    @Nullable
    static private MilkyStableSupport milkyStableSupport;

    /**
     * need to be instaniate by a failureExtendableImpl
     */
    @Nullable
    static private RunnerExtension runnerExtension;

    static public void setMilkyStableSupport(MilkyStableSupport milkyStableSupportImpl) {
        milkyStableSupport = milkyStableSupportImpl;
    }

    static public void setFailureExtendable(RunnerExtension runnerExtensionImpl) {
        runnerExtension = runnerExtensionImpl;
    }

    @SneakyThrows
    static public <R, T> T checkout(Option<R, T> option, SCallable<R> sCallable) {

        UK lambdaId = option.getLambdaId();
        SerializedLambda serializedLambda = SLambda.resolveArgs(sCallable);
        String position = Kit.op(lambdaId).map(UK::getKey).orElse(serializedLambda.getImplMethodSignature());

        RateLimiterWrapper rateLimiter = null;
        CircuitBreaker circuitBreaker = null;

        if (milkyStableSupport != null && lambdaId != null) {
            rateLimiter = milkyStableSupport.rateLimiter(lambdaId.getKey());
            circuitBreaker = milkyStableSupport.circuitBreaker(lambdaId.getKey());
        }

        if (rateLimiter != null) {
            rateLimiter.acquire();
        }

        R result = null;
        Throwable backup = null;
        int retryTimes = option.getRetryTimes();
        String printableResult = null;
        boolean retryable;
        do {
            retryable = false;
            long now = Clock.currentTimeMillis();
            try {
                if (circuitBreaker != null) {
                    result = circuitBreaker.decorateCallable(sCallable).call();
                } else {
                    result = sCallable.call();
                }
                BaseEx baseEx = option.getChecker().apply(result);
                printableResult = Kit.op(option.getResultPrinter()).orElse(Objects::toString).apply(result);
                if (baseEx != null) {
                    if (circuitBreaker != null) {
                        circuitBreaker.onError(Clock.currentTimeMillis() - now, TimeUnit.MILLISECONDS, baseEx);
                    }
                    throw baseEx;
                }
                return option.getTransfer().apply(result);
            } catch (Throwable throwable) {
                if (throwable instanceof InvocationTargetException) {
                    backup = ((InvocationTargetException) throwable).getTargetException();
                } else {
                    backup = throwable;
                }

                retryable = option.getRetryable().apply(result, backup);
                boolean notRetry = (!retryable) || retryTimes == 0;

                if (notRetry || backup instanceof CallNotPermittedException) {
                    retryTimes = 0;
                    if (!option.hasDefaultValue()) {
                        throw backup;
                    }
                    return option.getDefaultValue();
                }
            } finally {
                try {

                    log.result(printableResult).position(position).cost(Clock.currentTimeMillis() - now);

                    for (int i = 0; i < serializedLambda.getCapturedArgCount() - 1; i++) {
                        Object capturedArg = serializedLambda.getCapturedArg(i + 1);
                        log.with("arg" + i, capturedArg);
                    }

                    if (backup == null && option.isAlwaysLog()) {
                        log.success(true).info(position);
                    } else if (backup != null){
                        if (retryTimes == 0) {
                            log.success(false).error(position, backup);
                        } else {
                            log.success(false).warn(position, backup);
                        }
                    }
                    // 特殊场景，有可能上面的log函数一个都走不到，所以需要手动清理一遍mdc，防止参数串台
                    log.clear();

                    if (runnerExtension != null) {
                        runnerExtension.watch(serializedLambda, result, null, backup);
                    }

                } catch (Throwable throwable) {
                    log.position("THROW_IN_FINALLY_" + position).error(position, throwable);
                    if (backup != null) {
                        throw backup;
                    }
                }
            }
            backup = null;
            retryable = retryTimes-- > 0 && retryable;
            if (retryable) {
                String retryRecord = String.format("Th %sth retry!", option.getRetryTimes() - retryTimes - 1);
                log.arg0(retryRecord).position("RETRY_" + position).error(position, backup);
            }
        } while (retryable);
        throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
    }


}
