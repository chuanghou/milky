package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.aspectj.tool.entrance.AnnotationInterceptor;
import com.stellariver.milky.aspectj.tool.entrance.EntranceAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@Aspect
public class RpcAspect extends EntranceAspect {

    @Pointcut("execution(public * com.stellariver.milky.demo.adapter.controller.ItemController.publish(..))")
    protected void pointCut() {}

    @Override
    protected Boolean useMDC() {
        return false;
    }

    static final private AnnotationInterceptor annotationInterceptor = new AnnotationInterceptor() {

        @Override
        public void before(ProceedingJoinPoint pjp) {
            pjp.getArgs()[0] = "testAjp";
        }

        @Override
        public void after(ProceedingJoinPoint pjp) {
        }

        @Override
        public Class<? extends Annotation> annotatedBy() {
            return TestForInterceptor.class;
        }

    };

    @Override
    protected List<AnnotationInterceptor> interceptors() {
        return Collections.singletonList(annotationInterceptor);
    }

}
