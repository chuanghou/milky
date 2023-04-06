package com.stellariver.milky.aspectj.tool;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

/**
 * @author houchuang
 */

@Aspect
@DeclarePrecedence("AnnotationRateLimitAspect, AnnotationLogAspect, AnnotationValidateAspect, AnnotationTLCAspect")
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AspectOrder {
}
