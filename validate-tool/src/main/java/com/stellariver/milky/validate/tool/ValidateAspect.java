package com.stellariver.milky.validate.tool;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author houchuang
 */
@Aspect
public class ValidateAspect {

    @Pointcut("@annotation(com.stellariver.milky.validate.tool.ValidConfig)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        ValidConfig annotation = method.getAnnotation(ValidConfig.class);
        Class<?>[] groups = annotation.groups();
        boolean failFast = annotation.failFast();
        ValidateUtil.ExceptionType type = annotation.type();
        ValidateUtil.validate(pjp.getTarget(), method, args, failFast, type, groups);
        return pjp.proceed();
    }

}
