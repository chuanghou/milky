package com.stellariver.milky.aspectj.tool.entrance;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.annotation.Annotation;

public interface AnnotationInterceptor {

    void before(ProceedingJoinPoint pjp);

    void after(ProceedingJoinPoint pjp);

    Class<? extends Annotation> annotatedBy();

}
