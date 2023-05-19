package com.stellariver.milky.aspectj.tool.validate;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * <a href="https://stackoverflow.com/questions/38938845/can-not-build-thisjoinpoint-lazily-for-this-advice-since-it-has-no-suitable-guar">关于Xlint:noGuardForLazyTjp</a>
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AnnotationValidateAspect extends AbstractValidateAspect {

    @Pointcut("execution(@com.stellariver.milky.aspectj.tool.validate.Validate * *(..))")
    public void pointCut() {}

    @Override
    public ValidateConfig validateConfig(ProceedingJoinPoint pjp) {
        Validate annotation = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(Validate.class);
        ValidateConfig validateConfig = new ValidateConfig();
        validateConfig.setFailFast(annotation.failFast());
        validateConfig.setType(annotation.type());
        validateConfig.setGroups(annotation.groups());
        return validateConfig;
    }

}
