package com.stellariver.milky.demo.ajc;

import com.stellariver.milky.aspectj.tool.log.AbstractLogAspect;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class MyLogAspect extends AbstractLogAspect {

    @Pointcut("within(com.stellariver.milky.demo.adapter.ajc.custom..*) && execution(* *(..))")
    public void pointCut() {}

}