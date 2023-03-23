package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.log.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

/**
 * @author houchuang
 */

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class LogAspect {

    static private final Logger log = Logger.getLogger(LogAspect.class);

    @Pointcut("execution(@com.stellariver.milky.common.tool.log.Log * *(..))")
    private void pointCut() {}

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        boolean debug = method.getAnnotation(Log.class).debug();
        Object result = null;
        long start = Clock.currentTimeMillis();
        Throwable backUp = null;
        try {
            result= pjp.proceed();
        } catch (Throwable throwable) {
            backUp = throwable;
            throw throwable;
        } finally {
            if (backUp == null && debug) {
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

