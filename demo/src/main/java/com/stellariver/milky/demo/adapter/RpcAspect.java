package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.base.PageResult;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.validate.ValidateConfig;
import com.stellariver.milky.common.tool.exception.BaseException;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
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

/**
 * @author houchuang
 */
@CustomLog
@RequiredArgsConstructor
public class RpcAspect<T extends Result<?>> {

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.stellariver.milky.demo.adapter.rpc..*(..))")
    private void resultPointCut() {}

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.stellariver.milky.demo.adapter.rpc..*(..))")
    private void pageResultPointCut() {}

    final MilkyStableSupport milkyStableSupport;

    @Around("resultPointCut() || pageResultPointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {
        String key = milkyStableSupport.ruleId(pjp);
        RateLimiterWrapper rateLimiterWrapper = milkyStableSupport.rateLimiter(key);
        if (rateLimiterWrapper != null) {
            rateLimiterWrapper.acquire();
        }
        Object result = null;
        Object[] args = pjp.getArgs();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = method.getReturnType();
        long start = Clock.currentTimeMillis();
        List<ErrorEnum> errorEnums = Collections.emptyList();
        Throwable t = null;
        try {
            ValidateConfig annotation = method.getAnnotation(ValidateConfig.class);
            Class<?>[] groups = annotation.groups();
            boolean failFast = annotation.failFast();
            ValidateUtil.bizValidate(pjp.getTarget(), method, args, failFast, groups);
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
            log.result(result).source("rpc's source").cost(Clock.currentTimeMillis() - start).log(logTag, t);
        }
        return result;
    }

}
