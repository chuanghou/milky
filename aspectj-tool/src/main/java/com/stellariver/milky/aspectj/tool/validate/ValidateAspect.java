package com.stellariver.milky.aspectj.tool.validate;

import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

/**
 * <a href="https://stackoverflow.com/questions/38938845/can-not-build-thisjoinpoint-lazily-for-this-advice-since-it-has-no-suitable-guar">关于Xlint:noGuardForLazyTjp</a>
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class ValidateAspect {

    static private final Logger log = Logger.getLogger(ValidateAspect.class);

    @Pointcut("execution(@com.stellariver.milky.aspectj.tool.validate.Validate * *(..))")
    private void pointCut() {}

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Validate annotation = method.getAnnotation(Validate.class);
        Class<?>[] groups = annotation.groups();
        boolean failFast = annotation.failFast();
        ExceptionType type = annotation.type();
        Object result;
        ValidateUtil.validate(pjp.getTarget(), method, args, failFast, type, groups);
        result = pjp.proceed();
        ValidateUtil.validate(pjp.getTarget(), method, result, failFast, type, groups);
        return result;
    }

}
