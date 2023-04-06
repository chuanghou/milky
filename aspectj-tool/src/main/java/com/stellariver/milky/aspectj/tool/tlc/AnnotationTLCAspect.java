package com.stellariver.milky.aspectj.tool.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.util.Collect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AnnotationTLCAspect extends AbstractTLCAspect{

    private final Map<Method, Set<BaseQuery<?, ?>>> enableBqs = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.stellariver.milky.aspectj.tool.tlc.TLC)")
    public void pointCut() {}

    @Override
    public TLCConfig tlcConfig(ProceedingJoinPoint pjp) {
        TLC tlc = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(TLC.class);
        TLCConfig tlcConfig = new TLCConfig();
        tlcConfig.setDisableBaseQueries(Collect.asSet(tlc.disableBaseQueries()));
        return tlcConfig;
    }

}
