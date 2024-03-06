package com.stellariver.milky.aspectj.tool;

import org.aspectj.lang.annotation.Pointcut;

public abstract class BaseAspect {

    @Pointcut("execution(* *.set*(..))")
    private void setterPc() {}
    @Pointcut("execution(* *.get*(..))")
    private void getterPc() {}
    @Pointcut("execution(* *.toString(..))")
    public void toStringPc() {}
    @Pointcut("execution(* *.hashCode(..))")
    public void hashCodePc() {}
    @Pointcut("execution(* *.equals(..))")
    public void equalsPc() {}
    @Pointcut("execution(* *.logConfig(..))")
    public void logConfigPc() {}
    @Pointcut("execution(* *.validateConfig(..))")
    public void validateConfigPc() {}
    @Pointcut("execution(* *.tlcConfig(..))")
    public void tlcConfigPc() {}
    @Pointcut("execution(* *.rateLimitConfig(..))")
    public void rateLimitConfigPc() {}


    @Pointcut("setterPc() || getterPc() || toStringPc() || hashCodePc() || equalsPc() " +
            "|| logConfigPc() || validateConfigPc() || tlcConfigPc() || rateLimitConfigPc()")
    public void ignorePointCut() {}
}
