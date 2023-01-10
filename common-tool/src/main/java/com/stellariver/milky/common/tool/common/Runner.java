package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.slambda.SCallable;
import com.stellariver.milky.common.tool.slambda.SLambda;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.RunnerExtension;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.CustomLog;
import lombok.NonNull;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@CustomLog
@SuppressWarnings("all")
public class Runner {

    /**
     * need to be instaniate by a AbstractStableSupport Impl
     */
    @Nullable
    static private AbstractStableSupport abstractStableSupport;


    /**
     * need to be instaniate by a failureExtendableImpl
     */
    @Nullable
    static private RunnerExtension runnerExtension;

    static public void setAbstractStableSupport(@NonNull AbstractStableSupport support) {
        abstractStableSupport = support;
    }
    static public void setFailureExtendable(@NonNull RunnerExtension runnerExtensionImpl) {
        runnerExtension = runnerExtensionImpl;
    }


    static public <R, T> T checkout(Option<R, T> option, SCallable<R> sCallable) {
        RateLimiterWrapper rateLimiter = null;
        CircuitBreaker circuitBreaker = null;
        UK lambdaId = option.getLambdaId();
        if (abstractStableSupport != null && lambdaId != null) {
            rateLimiter = abstractStableSupport.rateLimiter(lambdaId.getKey());
            circuitBreaker = abstractStableSupport.circuitBreaker(lambdaId.getKey());
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
        SysException.anyNullThrow(option.getCheck(), option.getTransfer());
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
                    SysException sysException = new SysException(ErrorEnumsBase.SYSTEM_EXCEPTION.message(result));
                    if (circuitBreaker != null) {
                        circuitBreaker.onError(Clock.currentTimeMillis() - now, TimeUnit.MILLISECONDS, sysException);
                    }
                    throw sysException;
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
                Map<String, Object> args = null;
                if (throwableBackup == null && option.isAlwaysLog()) {
                    args = SLambda.resolveArgs(sCallable);
                    Function<R, String> printer = Kit.op(option.getRSelector()).orElse(Objects::toString);
                    log.with(args).result(printer.apply(result)).cost(Clock.currentTimeMillis() - now).info(lambdaId.getKey());
                } else if (throwableBackup != null){
                    args = SLambda.resolveArgs(sCallable);
                    if (retryTimes == 0) {
                        log.with(args).cost(Clock.currentTimeMillis() - now).error(logTag, throwableBackup);
                    } else {
                        log.with(args).cost(Clock.currentTimeMillis() - now).warn(logTag, throwableBackup);
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
        throw new SysException("unreached part!");
    }

    @SneakyThrows(Throwable.class)
    public static Object invoke(Object bean, Method method, Object... args) {
        try {
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
