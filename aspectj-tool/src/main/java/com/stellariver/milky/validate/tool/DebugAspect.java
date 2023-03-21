package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.log.Log;
import com.stellariver.milky.common.tool.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

/**
 * This aspectj class has not been annotated with aspect, it is designed to be copied to
 * user source code, then set the point cut to the package of project
 * @author houchuang
 */
public class DebugAspect {

    static private final Logger log = Logger.getLogger(DebugAspect.class);

    @Pointcut("execution(* com.stellariver.milky.demo..*.*(..))")
    private void pointCut() {}

    @Around("pointCut()")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
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
            IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));
            log.result(result).cost(Clock.currentTimeMillis() - start);
            log.log(pjp.toShortString(), backUp);
        }
        return result;
    }

}

