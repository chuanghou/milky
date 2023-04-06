package com.stellariver.milky.aspectj.tool.log;

import com.stellariver.milky.aspectj.tool.BaseAspect;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.exception.BizEx;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.stream.IntStream;

/**
 * @author houchuang
 */

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public abstract class AbstractLogAspect extends BaseAspect {

    @Pointcut
    public abstract void pointCut();

    @Around("pointCut() && !ignorePointCut()")
    public Object proceed(ProceedingJoinPoint pjp) throws Throwable {
        LogConfig logConfig = logConfig(pjp);
        return doProceed(pjp, logConfig);
    }

    public LogConfig logConfig(ProceedingJoinPoint pjp) {
        return LogConfig.defaultConfig();
    }

    private Object doProceed(ProceedingJoinPoint pjp, LogConfig logConfig) throws Throwable {
        Object[] args = pjp.getArgs();
        Object result = null;
        long start = Clock.currentTimeMillis();
        Throwable backUp = null;
        try {
            result= pjp.proceed();
        } catch (Throwable throwable) {
            backUp = throwable;
            throw throwable;
        } finally {
            if (backUp == null && logConfig.isDebug()) {
                if (log.isDebugEnabled()) {
                    IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));
                    log.result(result).cost(Clock.currentTimeMillis() - start);
                    log.debug(pjp.toShortString());
                }
            } else {
                IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));
                log.result(result).cost(Clock.currentTimeMillis() - start);
                if (backUp == null) {
                    log.success(true).info(pjp.toShortString());
                } else if (backUp instanceof BizEx) {
                    log.success(false).warn(pjp.toShortString());
                } else {
                    log.success(false).error(pjp.toShortString());
                }
            }
        }
        return result;
    }
}

