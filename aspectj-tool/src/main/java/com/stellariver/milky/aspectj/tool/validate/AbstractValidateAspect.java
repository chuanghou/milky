package com.stellariver.milky.aspectj.tool.validate;

import com.stellariver.milky.common.base.ExceptionType;
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
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public abstract class AbstractValidateAspect {

    @Pointcut
    public abstract void pointCut();

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        ValidateConfig config = validateConfig(pjp);
        return doProceed(pjp, config);
    }

    private Object doProceed(ProceedingJoinPoint pjp, ValidateConfig config) throws Throwable {
        Object result;
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        boolean failFast = config.isFailFast();
        ExceptionType type = config.getType();
        Class<?>[] groups = config.getGroups();
        ValidateUtil.validate(pjp.getTarget(), method, args, failFast, type, groups);
        result = pjp.proceed();
        ValidateUtil.validate(pjp.getTarget(), method, result, failFast, type, groups);
        return result;
    }

    public ValidateConfig validateConfig(ProceedingJoinPoint pjp) {
        return ValidateConfig.defaultConfig();
    }

}
