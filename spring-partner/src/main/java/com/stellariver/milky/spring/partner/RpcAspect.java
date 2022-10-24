package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.base.PageResult;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.common.BaseException;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.common.ValidateUtil;
import com.stellariver.milky.common.tool.util.Collect;
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

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.xxx.adpater.rpc..*(..))")
    private void resultPointCut() {}

    @Pointcut("execution(public com.stellariver.milky.common.base.PageResult com.xxx.adpater.rpc..*(..))")
    private void pageResultPointCut() {}

    @Around("resultPointCut() || pageResultPointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {
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
                ErrorEnum errorEnum = com.stellariver.milky.domain.support.ErrorEnums.SYSTEM_EXCEPTION.message(throwable.getMessage());
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
