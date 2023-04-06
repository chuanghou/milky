package com.stellariver.milky.aspectj.tool;

import com.stellariver.milky.common.tool.log.Logger;
import org.aspectj.lang.annotation.Pointcut;

public abstract class BaseAspect {

    static public final Logger log = Logger.getLogger(BaseAspect.class);

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
    @Pointcut("execution(* *.logConfig(..))")
    public void logConfigPC() {}
    @Pointcut("execution(* *.validateConfig(..))")
    public void validateConfigPC() {}
    @Pointcut("execution(* *.tlcConfig(..))")
    public void tlcConfigPC() {}
    @Pointcut("execution(* *.rateLimitConfig(..))")
    public void rateLimitConfigPC() {}


    @Pointcut("setterPC() || getterPC() || toStringPC() || hashCodePC() || equalsPC() " +
            "|| logConfigPC() || validateConfigPC() || tlcConfigPC() || rateLimitConfigPC()")
    public void ignorePointCut() {}
}
