package com.stellariver.milky.common.tool.common;

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
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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


    static public <R, T> T checkout(Option<R, T> option, SCallable<R> sCallable) {
        RateLimiterWrapper rateLimiter = null;
        CircuitBreaker circuitBreaker = null;
        UK lambdaId = option.getLambdaId();
        if (milkyStableSupport != null && lambdaId != null) {
            rateLimiter = milkyStableSupport.rateLimiter(lambdaId.getKey());
            circuitBreaker = milkyStableSupport.circuitBreaker(lambdaId.getKey());
        }
        return checkout(option, sCallable, circuitBreaker, rateLimiter, lambdaId);
    }

    @SuppressWarnings("all")
    @SneakyThrows
    static public <R, T> T checkout(Option<R, T> option, SCallable<R> sCallable,
                                    @Nullable CircuitBreaker circuitBreaker, @Nullable RateLimiterWrapper rateLimiter, @Nullable UK lambdaId) {
        if (rateLimiter != null) {
            rateLimiter.acquire();
        }
        SysEx.anyNullThrow(option.getCheck(), option.getTransfer());
        R result = null;
        Throwable throwableBackup = null;
        int retryTimes = option.getRetryTimes();
        do {
            long now = Clock.currentTimeMillis();
            try {
                if (circuitBreaker != null) {
                    result = circuitBreaker.decorateCallable(sCallable).call();
                } else {
                    result = sCallable.call();
                }
                Boolean success = option.getCheck().apply(result);
                if (!success) {
                    SysEx sysEx = new SysEx(ErrorEnumsBase.SYS_EX.message(result));
                    if (circuitBreaker != null) {
                        circuitBreaker.onError(Clock.currentTimeMillis() - now, TimeUnit.MILLISECONDS, sysEx);
                    }
                    throw sysEx;
                }
                return option.getTransfer().apply(result);
            } catch (Throwable throwable) {
                if (throwable instanceof InvocationTargetException) {
                    throwableBackup = ((InvocationTargetException) throwable).getTargetException();
                } else {
                    throwableBackup = throwable;
                }
                if (retryTimes == 0 || throwableBackup instanceof CallNotPermittedException) {
                    retryTimes = 0;
                    if (!option.hasDefaultValue()) {
                        throw throwableBackup;
                    }
                    return option.getDefaultValue();
                }
            } finally {
                String logTag = Kit.op(lambdaId).map(UK::getKey).orElse("NOT_SET");
                List<Object> args = null;
                if (throwableBackup == null && option.isAlwaysLog()) {
                    args = SLambda.resolveArgs(sCallable);
                    Function<R, String> printer = Kit.op(option.getRSelector()).orElse(Objects::toString);
                    for (int i = 0; i < args.size() - 1; i++) {
                        log.with("arg" + i, args.get(i - 1));
                    }
                    log.result(printer.apply(result)).success(true).cost(Clock.currentTimeMillis() - now).info(lambdaId.getKey());
                } else if (throwableBackup != null){
                    args = SLambda.resolveArgs(sCallable);
                    if (retryTimes == 0) {
                        for (int i = 0; i < args.size() - 1; i++) {
                            log.with("arg" + i, args.get(i - 1));
                        }
                        log.success(true).cost(Clock.currentTimeMillis() - now).error(logTag, throwableBackup);
                    } else {
                        for (int i = 0; i < args.size() - 1; i++) {
                            log.with("arg" + i, args.get(i - 1));
                        }
                        log.success(true).cost(Clock.currentTimeMillis() - now).warn(logTag, throwableBackup);
                    }
                }
                if (runnerExtension != null) {
                    if (args == null) {
                        args = SLambda.resolveArgs(sCallable);
                    }
                    runnerExtension.watch(args, result, lambdaId, throwableBackup);
                }
            }
            throwableBackup = null;
        } while (retryTimes-- > 0);
        throw new SysEx("unreached part!");
    }

}
