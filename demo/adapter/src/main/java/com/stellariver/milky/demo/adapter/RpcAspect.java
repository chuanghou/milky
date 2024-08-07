package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.aspectj.tool.entrance.AnnotationInterceptor;
import com.stellariver.milky.aspectj.tool.entrance.EntranceAspect;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
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

    @Override
    protected List<AnnotationInterceptor> interceptors() {
        return Collections.singletonList(new AnnotationInterceptor() {

            @Override
            public void executeBefore(ProceedingJoinPoint pjp) {
                throw new BizEx(ErrorEnumsBase.PARAM_FORMAT_WRONG.message("时间区间不在可接收范围"));
            }

            @Override
            public Class<? extends Annotation> annotatedBy() {
                return TestForInterceptor.class;
            }

        });
    }

}
