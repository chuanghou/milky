package com.stellariver.milky.demo.basic.ajc;

import com.stellariver.milky.aspectj.tool.validate.AbstractValidateAspect;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@SuppressWarnings({"aspect"})
public class MyValidateAspect extends AbstractValidateAspect {

    @Pointcut("within(com.stellariver.milky.demo.adapter.ajc.custom..*) && execution(* *(..))")
    public void pointCut() {}


}