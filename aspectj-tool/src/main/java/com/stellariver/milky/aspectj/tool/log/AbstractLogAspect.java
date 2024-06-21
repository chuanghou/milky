package com.stellariver.milky.aspectj.tool.log;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * @author houchuang
 */

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public abstract class AbstractLogAspect {

    static private final Map<Class<?>, Logger> loggers = new ConcurrentHashMap<>();

    @Pointcut
    public abstract void pointCut();

    @Around("pointCut() && !ignorePointCut()")
    public Object proceed(ProceedingJoinPoint pjp) throws Throwable {
        LogConfig logConfig = logConfig(pjp);
        return doProceed(pjp, logConfig);
    }

    private Object doProceed(ProceedingJoinPoint pjp, LogConfig logConfig) throws Throwable {
        Object[] args = pjp.getArgs();
        Object result = null;
        long start = Clock.currentTimeMillis();
        Throwable backUp = null;
        Class<?> declaringType = pjp.getSignature().getDeclaringType();
        MethodSignature methodSignature = (MethodSignature)pjp.getSignature();
        Logger logger = loggers.computeIfAbsent(declaringType, Logger::getLogger);
        try {
            result= pjp.proceed();
        } catch (Throwable throwable) {
            backUp = throwable;
            throw throwable;
        } finally {
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getMethod().getName();
            String position = String.format("%s_%s", className, methodName);
            long cost = Clock.currentTimeMillis() - start;
            String message;
            if (logConfig.getUseMDC()) {
                IntStream.range(0, args.length).forEach(i -> logger.with("arg" + i, args[i]));
                logger.result(result).cost(cost).position(position);
                message = position;
            } else {
                message = String.format("position: %s, args: %s, result, %s", position, Arrays.toString(args), result);
            }
            if (backUp == null && logConfig.getDebug()) {
                if (logger.isDebugEnabled()) {
                    logger.success(true).debug(message);
                }
            } else {
                if (backUp == null) {
                    logger.success(true).info(message);
                } else if (backUp instanceof BizEx) {
                    logger.success(false).warn(message, backUp);
                } else {
                    logger.success(false).error(message, backUp);
                }
            }
            logger.clear();
        }
        return result;
    }

    public LogConfig logConfig(ProceedingJoinPoint pjp) {
        return LogConfig.defaultConfig();
    }

}

