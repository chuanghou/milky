package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.base.ExceptionType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * <a href="https://stackoverflow.com/questions/38938845/can-not-build-thisjoinpoint-lazily-for-this-advice-since-it-has-no-suitable-guar">关于Xlint:noGuardForLazyTjp</a>
 * @author houchuang
 */
@Aspect
public class ValidateAspect {

    @Pointcut("execution(@Validate * *(..))")
    private void pointCut() {}

    @Before("pointCut()")
    public void valid(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Validate annotation = method.getAnnotation(Validate.class);
        Class<?>[] groups = annotation.groups();
        boolean failFast = annotation.failFast();
        ExceptionType type = annotation.type();
        ValidateUtil.validate(joinPoint.getTarget(), method, args, failFast, type, groups);
    }

}
