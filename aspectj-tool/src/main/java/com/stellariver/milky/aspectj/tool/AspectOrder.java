package com.stellariver.milky.aspectj.tool;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

/**
 * @author houchuang
 */

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
@DeclarePrecedence("AnnotationRateLimitAspect, AnnotationLogAspect, AnnotationValidateAspect, AnnotationTLCAspect")
public class AspectOrder {
}
