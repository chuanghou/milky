package com.stellariver.milky.spring.partner.log;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.spring.partner.AspectTool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Date;

public class LogInfoSupport {

    static Logger log= Logger.getLogger(LogInfoSupport.class);

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.log.LogInfo)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object log(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        Date startTime = new Date();
        AspectTool.MethodInfo methodInfo = AspectTool.methodInfo(proceedingJoinPoint);
        log.with("methodInfo", methodInfo).with("startTime", startTime);
        try {
            result = proceedingJoinPoint.proceed();
        } finally {
            Date endTime = new Date();
            LogInfo logInfo = methodInfo.getMethod().getAnnotation(LogInfo.class);
            String logTag = logInfo.logTag().equals("default") ? methodInfo.methodReference() : logInfo.logTag();
            log.with("result", result).with("endTime", endTime)
                    .with("cost", endTime.getTime() - startTime.getTime())
                    .withLogTag(logTag)
                    .info(null);
        }
        return result;
    }
}
