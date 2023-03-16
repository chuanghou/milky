package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

@Aspect
public class LogAspect {

    static private final Logger log = Logger.getLogger(LogAspect.class);

    @Pointcut("execution(@Log * *(..))")
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
                log.log(pjp.toShortString(), backUp);
            }
        }
        return result;
    }

}

