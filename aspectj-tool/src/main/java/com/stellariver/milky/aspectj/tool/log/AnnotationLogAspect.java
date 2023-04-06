package com.stellariver.milky.aspectj.tool.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AnnotationLogAspect extends AbstractLogAspect {

    @Pointcut("execution(@com.stellariver.milky.aspectj.tool.log.Log * *(..))")
    public void pointCut() {}

    @Override
    public LogConfig logConfig(ProceedingJoinPoint pjp) {
        Log annotation = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(Log.class);
        LogConfig logConfig = new LogConfig();
        logConfig.setDebug(annotation.debug());
        return logConfig;
    }

}
