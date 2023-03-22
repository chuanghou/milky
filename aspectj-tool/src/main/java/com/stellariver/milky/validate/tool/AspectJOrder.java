package com.stellariver.milky.validate.tool;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

/**
 * @author houchuang
 */

@Aspect
@DeclarePrecedence("LogAspect, ValidateAspect")
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AspectJOrder {
}
