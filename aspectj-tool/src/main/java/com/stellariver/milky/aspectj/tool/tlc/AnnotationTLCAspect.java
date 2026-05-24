package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.util.Collect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AnnotationTLCAspect extends AbstractTLCAspect {

    @Pointcut("@annotation(com.stellariver.milky.aspectj.tool.tlc.TLC)")
    public void pointCut() {}

    @Override
    public TLCConfig tlcConfig(ProceedingJoinPoint pjp) {
        TLC tlc = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(TLC.class);
        TLCConfig tlcConfig = new TLCConfig();
        tlcConfig.setThreadLocalBaseQueryTypes(Collect.asSet(tlc.threadLocalBaseQueries()));
        return tlcConfig;
    }

}
