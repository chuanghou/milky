package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.aspectj.tool.entrance.AnnotationInterceptor;
import com.stellariver.milky.aspectj.tool.entrance.EntranceAspect;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;
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
        return Arrays.asList(new AnnotationInterceptor() {

            @Override
            public void executeBefore(ProceedingJoinPoint pjp) {
                throw new BizEx(ErrorEnumsBase.PARAM_FORMAT_WRONG.message("时间区间不在可接收范围"));
            }

            @Override
            public boolean conditional(ProceedingJoinPoint pjp) {
                return ((MethodSignature)pjp.getSignature()).getMethod().isAnnotationPresent(TestForInterceptor.class);
            }

            @Override
            public void logThrowable(ProceedingJoinPoint pjp, Throwable throwable) {
            }

        }, new SqlLogInterceptor());
    }



}
