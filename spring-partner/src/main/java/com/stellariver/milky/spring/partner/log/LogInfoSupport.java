package com.stellariver.milky.spring.partner.log;

import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.spring.partner.AspectTool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
public class LogInfoSupport {

    static Logger log= Logger.getLogger(LogInfoSupport.class);

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.log.LogInfo)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object log(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        long startTime = SystemClock.now();
        AspectTool.MethodInfo methodInfo = AspectTool.methodInfo(proceedingJoinPoint);
        try {
            result = proceedingJoinPoint.proceed();
        } finally {
            long endTime = SystemClock.now();
            LogInfo logInfo = methodInfo.getMethod().getAnnotation(LogInfo.class);
            String logTag = logInfo.logTag().equals("default") ? methodInfo.methodReference() : logInfo.logTag();
            log.with("methodInfo", methodInfo).with("startTime", startTime)
                    .with("result", result).with("endTime", endTime)
                    .with("cost", endTime - startTime)
                    .withLogTag(logTag)
                    .info("");
        }
        return result;
    }
}
