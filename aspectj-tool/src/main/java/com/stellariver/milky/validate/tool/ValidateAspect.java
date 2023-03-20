package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.validate.Validate;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * <a href="https://stackoverflow.com/questions/38938845/can-not-build-thisjoinpoint-lazily-for-this-advice-since-it-has-no-suitable-guar">关于Xlint:noGuardForLazyTjp</a>
 * @author houchuang
 */
@Aspect
public class ValidateAspect {

    @Pointcut("execution(@com.stellariver.milky.common.tool.validate.Validate * *(..))")
    private void pointCut() {}

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Validate annotation = method.getAnnotation(Validate.class);
        Class<?>[] groups = annotation.groups();
        boolean failFast = annotation.failFast();
        ExceptionType type = annotation.type();
        ValidateUtil.validate(pjp.getTarget(), method, args, failFast, type, groups);
        Object result = pjp.proceed();
        ValidateUtil.validate(pjp.getTarget(), method, result, failFast, type, groups);
        return result;
    }

}
