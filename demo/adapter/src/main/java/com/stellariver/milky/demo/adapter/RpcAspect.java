package com.stellariver.milky.demo.adapter;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.stellariver.milky.aspectj.tool.validate.AnnotationValidateAspect;
import com.stellariver.milky.aspectj.tool.validate.Validate;
import com.stellariver.milky.common.base.*;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import com.stellariver.milky.domain.support.ErrorEnums;
import lombok.NonNull;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
        Throwable excavated = null, original = null;

        try {
            if (method.getAnnotation(Validate.class) == null) {
                ValidateUtil.validate(pjp.getTarget(), method, args, true, ExceptionType.BIZ);
            }
            result = pjp.proceed();
        } catch (Throwable throwable) {
            original = throwable;
            excavated = excavate(throwable);
            if (excavated instanceof BaseEx) {
                errorEnums = ((BaseEx) throwable).getErrors();
            } else {
                ErrorEnum errorEnum = ErrorEnums.SYS_EX.message(throwable.getMessage());
                errorEnums = Collect.asList(errorEnum);
            }
        } finally {

            if (excavated != null) {
                ExceptionType exceptionType = excavated instanceof BizEx ? ExceptionType.BIZ : ExceptionType.SYS;
                if (returnType == Result.class) {
                    result = Result.error(errorEnums, exceptionType);
                } else if (returnType == IterableResult.class){
                    result = IterableResult.pageError(errorEnums, exceptionType);
                } else if (returnType == PageResult.class){
                    result = PageResult.pageError(errorEnums, exceptionType);
                } else {
                    //noinspection ThrowFromFinallyBlock
                    throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
                }
                if (exceptionType == ExceptionType.BIZ) {
                    ((Result<?>) result).setMessage(excavated.getMessage());
                }
            }

            IntStream.range(0, args.length).forEach(i -> log.with("arg" + i, args[i]));

            String className = method.getDeclaringClass().getSimpleName();
            String methodName = method.getName();
            String message = Kit.op(excavated).map(Throwable::getMessage).orElse("");
            log.result(result).source("custom").cost(System.nanoTime() - start)
                    .position(String.format("%s_%s", className, methodName)).log(message, excavated);

            if (original != null && original != excavated) {
                log.position("excavated").error(original.getMessage(), original);
            }

        }
        return result;
    }

    private Throwable excavate(@NonNull Throwable original) {
        Throwable current = original;
        while (true) {
            Excavator<? extends Throwable, ? extends Throwable> excavator = excavators.get(current.getClass());
            if (excavator == null) {
                return current;
            }
            Throwable throwable = excavator.excavateWrapper(current);
            if (throwable == null) {
                return current;
            } else {
                current = throwable;
            }
        }
    }


    interface Excavator<Original extends Throwable, Cause extends Throwable> {

        Cause excavate(Original original);

        @SuppressWarnings("unchecked")
        default Cause excavateWrapper(Throwable throwable) {
            return excavate((Original) throwable);
        }

    }

    static class InvocationTargetExceptionExcavator implements Excavator<InvocationTargetException, Throwable> {

        @Override
        public Throwable excavate(InvocationTargetException e) {
            return e.getTargetException();
        }

    }


    static class UncheckedExecutionExceptionExcavator implements Excavator<UncheckedExecutionException, Throwable> {

        @Override
        public Throwable excavate(UncheckedExecutionException e) {
            return e.getCause();
        }

    }

    static class ExecutionExceptionExcavator implements Excavator<ExecutionException, Throwable> {

        @Override
        public Throwable excavate(ExecutionException e) {
            return e.getCause();
        }

    }

    static private final Map<Class<? extends Throwable>, Excavator<? extends Throwable, ? extends Throwable>> excavators = new HashMap<>();


    static {
        excavators.put(UncheckedExecutionException.class, new UncheckedExecutionExceptionExcavator());
        excavators.put(ExecutionException.class, new ExecutionExceptionExcavator());
        excavators.put(InvocationTargetException.class, new InvocationTargetExceptionExcavator());
    }


}
