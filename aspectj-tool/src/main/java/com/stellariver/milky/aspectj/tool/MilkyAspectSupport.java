package com.stellariver.milky.aspectj.tool;

import com.stellariver.milky.aspectj.tool.limit.EnableRateLimit;
import com.stellariver.milky.aspectj.tool.log.Log;
import com.stellariver.milky.aspectj.tool.tlc.EnableTLC;
import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import com.stellariver.milky.aspectj.tool.validate.Validate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection"})
public class MilkyAspectSupport {

    static private final Logger logger = Logger.getLogger(MilkyAspectSupport.class);

    private MilkyStableSupport milkyStableSupport;

    private volatile boolean initMSS = false;

    private final Object lock = new Object();

    private final Map<Method, Set<BaseQuery<?, ?>>> enableBqs = new ConcurrentHashMap<>();

    @Pointcut("execution(@com.stellariver.milky.aspectj.tool.MilkyAspect * *(..))")
    private void pointCut() {}

    @Around("pointCut()")
    public Object valid(ProceedingJoinPoint pjp) throws Throwable {
        long start = Clock.currentTimeMillis();
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        MilkyAspect annotation = method.getAnnotation(MilkyAspect.class);
        Log log = annotation.log();
        Validate validate = annotation.validate();
        EnableTLC enableTLC = annotation.enableTLC();
        EnableRateLimit enableRateLimit = annotation.enableRateLimit();

        if (enableRateLimit != null) {
            rateLimit(pjp);
        }

        Object result = null;
        Set<BaseQuery<?, ?>> enableBaseQueries = null;
        if (enableTLC != null) {
            enableBaseQueries = enableBqs.computeIfAbsent(method, m -> {
                Set<Class<? extends BaseQuery<?, ?>>> disableBQCs = Collect.asSet(enableTLC.disableBaseQueries());
                return BeanUtil.getBeansOfType(BaseQuery.class).stream().filter(aBQ -> disableBQCs.contains(aBQ.getClass()))
                        .map(bq -> (BaseQuery<?, ?>) bq).collect(Collectors.toSet());
            });
        }

        Throwable backUp = null;
        try {
            if (validate != null) {
                ValidateUtil.validate(pjp.getTarget(), method, args, validate.failFast(), validate.type(), validate.groups());
            }
            if (enableTLC != null) {
                enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
            }
            result = pjp.proceed();
            if (validate != null) {
                ValidateUtil.validate(pjp.getTarget(), method, result, validate.failFast(), validate.type(), validate.groups());
            }
        } catch (Throwable throwable) {
            backUp = throwable;
            throw throwable;
        } finally {
            if (enableTLC != null) {
                enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
            }
            IntStream.range(0, args.length).forEach(i -> logger.with("arg" + i, args[i]));
            logger.result(result).cost(Clock.currentTimeMillis() - start);
            if (backUp == null) {
                if (log.debug()) {
                    if (logger.isDebugEnabled()) {
                        logger.success(true).debug(pjp.toShortString());
                    }
                } else {
                    logger.success(true).info(pjp.toShortString());
                }
            } else if (backUp instanceof BizEx) {
                logger.success(false).warn(pjp.toShortString());
            } else {
                logger.success(false).error(pjp.toShortString());
            }
        }
        return result;

    }

    private void rateLimit(ProceedingJoinPoint pjp) {
        if (!initMSS) {
            synchronized (lock) {
                if (!initMSS) {
                    Optional<MilkyStableSupport> beanOptional = BeanUtil.getBeanOptional(MilkyStableSupport.class);
                    beanOptional.ifPresent(stableSupport -> milkyStableSupport = stableSupport);
                    initMSS = true;
                }
            }
        }
        if (milkyStableSupport != null) {
            String key = milkyStableSupport.ruleId(pjp);
            RateLimiterWrapper rateLimiter = milkyStableSupport.rateLimiter(key);
            if (rateLimiter != null) {
                rateLimiter.acquire();
            }
        }
    }

}
