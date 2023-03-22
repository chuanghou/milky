package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.stream.IntStream;

/**
 * This aspectj class has not been annotated with aspect, it is designed to be copied to
 * user source code, then set the point cut to the package of project
 * @author houchuang
 */
//@Aspect
public class AutoLogAspect {

    static private final Logger log = Logger.getLogger(AutoLogAspect.class);

    @Pointcut("execution(* *.set*(..))")
    private void setterPC() {}

    @Pointcut("execution(* *.get*(..))")
    private void getterPC() {}

    @Pointcut("execution(* *.toString(..))")
    public void toStringPC() {}

    @Pointcut("execution(* *.hashCode(..))")
    public void hashCodePC() {}

    @Pointcut("execution(* *.equals(..))")
    public void equalsPC() {}

    @Pointcut("execution(public * com.stellariver.milky.demo..*.*(..))")
    private void packagePC() {}

    @Pointcut("execution(@com.stellariver.milky.common.tool.log.Log * *(..))")
    private void logAnno() {}

//    @Around("packagePC() && !getterPC() && !setterPC() && !toStringPC() && !equalsPC() && !hashCodePC() && !logAnno()")
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

            // you can choose one model above, debug enable or info

            // 1. debug enable model
            if (log.isDebugEnabled()) {
                log.log(pjp.toShortString(), backUp);
            }

            // 2. info and error model
            log.log(pjp.toShortString(), backUp);

        }
        return result;
    }

}

