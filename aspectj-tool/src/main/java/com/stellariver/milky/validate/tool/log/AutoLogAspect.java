package com.stellariver.milky.validate.tool.log;

import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.exception.BizEx;
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
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
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

    @Pointcut("execution(* com.package..*.*(..))")
    private void packagePC() {}

    @Pointcut("execution(@com.stellariver.milky.validate.tool.log.Log * *(..))")
    private void logAnno() {}

    @Around("packagePC() && !getterPC() && !setterPC() && !toStringPC() && !equalsPC() && !hashCodePC() && !logAnno()")
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
            if (backUp == null) {
                // you can choose one model above, debug enable model or info and error model
                // 1. debug model
                if (log.isDebugEnabled()) {
                    log.success(true).debug(pjp.toShortString());
                }
                // 2. info model
                log.success(true).info(pjp.toShortString());
            } else if (backUp instanceof BizEx) {
                log.success(false).warn(pjp.toShortString());
            } else {
                log.success(false).error(pjp.toShortString());
            }
        }
        return result;
    }

}
