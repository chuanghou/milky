package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.common.tool.validate.ValidConfig;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author houchuang
 */
public class ValidateAspectDemo {

    @Pointcut("@annotation(com.stellariver.milky.common.tool.validate.ValidConfig)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        ValidConfig annotation = method.getAnnotation(ValidConfig.class);
        Class<?>[] groups = annotation.groups();
        boolean failFast = annotation.failFast();
        ValidateUtil.bizValidate(pjp.getTarget(), method, args, failFast, groups);
        return pjp.proceed();
    }

}
