package com.stellariver.milky.aspectj.tool.entrance;

import com.stellariver.milky.aspectj.tool.validate.AnnotationValidateAspect;
import com.stellariver.milky.aspectj.tool.validate.Validate;
import com.stellariver.milky.common.base.*;
import com.stellariver.milky.common.tool.Excavator;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * @author houchuang
 *
 * <p>This aspect will check if the method has been annotated with {@link AnnotationValidateAspect},
 * If the method has been validaed with ValidateAspect, then this aspect will not check the param validation</p>
 * @see AnnotationValidateAspect
 */
@Aspect
@SuppressWarnings({"MissingAspectjAutoproxyInspection", "unused"})
public abstract class EntranceAspect {

    static private MilkyStableSupport milkyStableSupport;
    static private final Object lock = new Object();
    static volatile boolean initialized = false;
    static private final Map<Class<?>, Logger> loggers = new ConcurrentHashMap<>();

    @Pointcut
    abstract protected void pointCut();

    protected Boolean useMDC() {
        return true;
    }

    protected List<AnnotationInterceptor> interceptors() {
        return Collections.emptyList();
    }

    protected Object customResult(ProceedingJoinPoint pjp, Throwable throwable) {
        throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
    }

    @Around("pointCut()")
    public Object resultResponseHandler(ProceedingJoinPoint pjp) {

        String traceId = TraceIdContext.getInstance().buildTraceId();
        TraceIdContext.getInstance().storeTraceId(traceId);

        // dcl to init Stable Support
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    BeanUtil.getBeanOptional(MilkyStableSupport.class).ifPresent(s -> milkyStableSupport = s);
                }
                initialized = true;
            }
        }

        // if there is milkyStableSupport, enable it
        if (milkyStableSupport != null) {
            String key = milkyStableSupport.ruleId(pjp);
            RateLimiterWrapper rateLimiterWrapper = milkyStableSupport.rateLimiter(key);
            if (rateLimiterWrapper != null) {
                rateLimiterWrapper.acquire();
            }
        }

        Object result = null;
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        long start = System.nanoTime();
        List<ErrorEnum> errorEnums = Collections.emptyList();
        Throwable excavated = null, original = null;

        try {

            interceptors().stream().filter(i -> i.conditional(pjp)).forEach(i -> i.executeBefore(pjp));

            if (method.getAnnotation(Validate.class) == null) {
                ValidateUtil.validate(pjp.getTarget(), method, args, true, ExceptionType.BIZ);
            }

            result = pjp.proceed();

            interceptors().stream().filter(i -> i.conditional(pjp)).forEach(i -> i.executeAfterWrapper(pjp));

        } catch (Throwable throwable) {

            interceptors().stream().filter(i -> i.conditional(pjp)).forEach(i -> i.afterThrowingWrapper(pjp, throwable));

            original = throwable;
            excavated = Excavator.excavate(throwable);
            if (excavated instanceof BaseEx) {
                errorEnums = ((BaseEx) excavated).getErrors();
            } else {
                ErrorEnum errorEnum = ErrorEnumsBase.SYS_EX.message(throwable.getMessage());
                errorEnums = Collect.asList(errorEnum);
            }

        } finally {

            interceptors().stream().filter(i -> i.conditional(pjp)).forEach(i -> i.executeFinallyWrapper(pjp));

            if (excavated != null) {

                Class<?> returnType = method.getReturnType();
                ExceptionType exceptionType = excavated instanceof BizEx ? ExceptionType.BIZ : ExceptionType.SYS;
                if (returnType == Result.class) {
                    result = Result.error(errorEnums, exceptionType);
                } else if (returnType == IterableResult.class) {
                    result = IterableResult.pageError(errorEnums, exceptionType);
                } else if (returnType == PageResult.class) {
                    result = PageResult.pageError(errorEnums, exceptionType);
                } else {
                    result = customResult(pjp, excavated);
                }

                if (exceptionType == ExceptionType.BIZ) {
                    ((Result<?>) result).setMessage(excavated.getMessage());
                }

            }

            Logger logger = loggers.computeIfAbsent(method.getDeclaringClass(), Logger::getLogger);
            String position = String.format("%s_%s", method.getDeclaringClass().getSimpleName(), method.getName());
            String message;
            if (useMDC()) {
                IntStream.range(0, args.length).forEach(i -> logger.with("arg" + i, args[i]));
                logger.result(result).cost(System.nanoTime() - start).position(position);
                message = position;
            } else {
                message = String.format("position: %s, args: %s, result: %s", position, Arrays.toString(args), result);
            }

            if (excavated == null) {
                logger.success(true).info(message);
            } else if (excavated instanceof BizEx) {
                logger.success(false).warn(message, excavated);
            } else {
                logger.success(false).error(message, excavated);
            }

            if (original != null && original != excavated) {
                logger.position("excavated").error(original.getMessage(), original);
            }

            TraceIdContext.getInstance().removeTraceId();
        }

        return result;
    }


}
