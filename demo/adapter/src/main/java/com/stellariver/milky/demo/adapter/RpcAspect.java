package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.aspectj.tool.validate.AnnotationValidateAspect;
import com.stellariver.milky.aspectj.tool.validate.Validate;
import com.stellariver.milky.common.base.*;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import com.stellariver.milky.domain.support.ErrorEnums;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author houchuang
 *
 * <p>This aspect will check if the method has been annotated with {@link AnnotationValidateAspect},
 * If the method has been validaed with ValidateAspect, then this aspect will not check the param validation</p>
 * @see AnnotationValidateAspect
 */
@Aspect
public class RpcAspect {

    static private final Logger log = Logger.getLogger(RpcAspect.class);

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.stellariver.milky.demo.adapter.rpc..*(..))")
    private void resultPointCut() {}

    @Pointcut("execution(public com.stellariver.milky.common.base.Result com.stellariver.milky.demo.adapter.rpc..*(..))")
    private void pageResultPointCut() {}

    MilkyStableSupport milkyStableSupport;

    final Object lock = new Object();

    volatile boolean init = false;

    @Around("resultPointCut() || pageResultPointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {
        if (!init) {
            synchronized (lock) {
                if (!init) {
                    BeanUtil.getBeanOptional(MilkyStableSupport.class).ifPresent(s -> milkyStableSupport = s);
                }
                init = true;
            }
        }
        if (milkyStableSupport != null) {
            String key = milkyStableSupport.ruleId(pjp);
            RateLimiterWrapper rateLimiterWrapper = milkyStableSupport.rateLimiter(key);
            if (rateLimiterWrapper != null) {
                rateLimiterWrapper.acquire();
            }
        }
        Object result = null;
        Object[] args = pjp.getArgs();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = method.getReturnType();
        long start = System.nanoTime();
        List<ErrorEnum> errorEnums = Collections.emptyList();
        Throwable t = null;
        try {
            if (method.getAnnotation(Validate.class) == null) {
                ValidateUtil.validate(pjp.getTarget(), method, args, true, ExceptionType.BIZ);
            }
            result = pjp.proceed();
        } catch (Throwable throwable) {
            if (throwable instanceof BaseEx) {
                errorEnums = ((BaseEx) throwable).getErrors();
            } else {
                ErrorEnum errorEnum = ErrorEnums.SYS_EX.message(throwable.getMessage());
                errorEnums = Collect.asList(errorEnum);
            }
            t = throwable;
        } finally {
            if (t != null) {
                ExceptionType exceptionType = t instanceof BizEx ? ExceptionType.BIZ : ExceptionType.SYS;
                if (returnType == Result.class) {
                    result = Result.error(errorEnums, exceptionType);
                } else {
                    result = PageResult.pageError(errorEnums, exceptionType);
                }
            }
            IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));
            String logTag = ((MethodSignature) pjp.getSignature()).getMethod().getName();
            log.result(result).source("rpc's source").cost(System.nanoTime() - start).log(logTag, t);
        }
        return result;
    }

}
