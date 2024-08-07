package com.stellariver.milky.aspectj.tool.entrance;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.annotation.Annotation;

@SuppressWarnings("unused")
public interface AnnotationInterceptor {

    void executeBefore(ProceedingJoinPoint pjp);

    default void executeAfter(ProceedingJoinPoint pjp) {}

    default void afterThrowing(ProceedingJoinPoint pjp, Throwable throwable) {}

    default void executeFinally(ProceedingJoinPoint pjp) {}

    Class<? extends Annotation> annotatedBy();

}
