package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.base.PageResult;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.exception.BaseException;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.common.ValidateUtil;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@CustomLog
@RequiredArgsConstructor
public class RpcAspect<T extends Result<?>> {

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.stellariver.milky.demo.adapter.rpc..*(..))")
    private void resultPointCut() {}

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.stellariver.milky.demo.adapter.rpc..*(..))")
    private void pageResultPointCut() {}

    final AbstractStableSupport abstractStableSupport;

    @Around("resultPointCut() || pageResultPointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {
        String key = abstractStableSupport.key(pjp);
        RateLimiterWrapper rateLimiterWrapper = abstractStableSupport.rateLimiter(key);
        if (rateLimiterWrapper != null) {
            rateLimiterWrapper.acquire();
        }
        Object result = null;
        Object[] args = pjp.getArgs();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = method.getReturnType();
        long start = SystemClock.now();
        List<ErrorEnum> errorEnums = Collections.emptyList();
        Throwable t = null;
        try {
            ValidateUtil.bizValidate(pjp.getTarget(), method, args);
            result = pjp.proceed();
        } catch (Throwable throwable) {
            if (throwable instanceof BaseException) {
                errorEnums = ((BaseException) throwable).getErrors();
            } else {
                ErrorEnum errorEnum = ErrorEnums.SYSTEM_EXCEPTION.message(throwable.getMessage());
                errorEnums = Collect.asList(errorEnum);
            }
            t = throwable;
        } finally {
            if (t != null && returnType == Result.class) {
                result = Result.error(errorEnums);
            } else if (t != null && returnType == PageResult.class) {
                result = PageResult.pageError(errorEnums);
            }
            IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));
            String logTag = ((MethodSignature) pjp.getSignature()).getMethod().getName();
            log.result(result).source("rpc's source").cost(SystemClock.now() - start).log(logTag, t);
        }
        return result;
    }

}