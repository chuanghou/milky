package com.stellariver.milky.validate.tool;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

/**
 * @author houchuang
 */

@Aspect
@DeclarePrecedence("RateLimitAspect, LogAspect, ValidateAspect, TLCAspect")
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AspectJOrder {
}
