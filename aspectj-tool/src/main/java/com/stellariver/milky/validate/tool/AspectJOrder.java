package com.stellariver.milky.validate.tool;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("LogAspect, ValidateAspect")
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class AspectJOrder {
}
