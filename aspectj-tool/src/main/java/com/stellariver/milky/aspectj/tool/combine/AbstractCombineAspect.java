package com.stellariver.milky.aspectj.tool.combine;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.exception.BizEx;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author houchuang
 */
@Aspect
@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
public abstract class AbstractCombineAspect {

    static private final Logger logger = Logger.getLogger(AbstractCombineAspect.class);

    // global forbidden point cut
    @Pointcut("execution(* *.set*(..))")
    private void setterPC() {}
    @Pointcut("execution(* *.get*(..))")
    private void getterPC() {}
    @Pointcut("execution(* *.toString(..))")
    public void toStringPC() {}
    @Pointcut("execution(* *.hashCode(..))")
    public void hashCodePC() {}
    @Pointcut("execution(* *.equals(..))")
    public void equalsPC() {}

    @Pointcut
    public abstract LogConfig logConfig();
    @Pointcut
    public abstract ValidateConfig validateConfig();
    @Pointcut
    public abstract TLCConfig tlcConfig();
    @Pointcut
    public abstract RateLimitConfig rateLimitConfig();

    private MilkyStableSupport milkyStableSupport;
    private volatile boolean initMSS = false;
    private final Object lock = new Object();
    private final Map<Method, Set<BaseQuery<?, ?>>> enableBqs = new ConcurrentHashMap<>();

    @Around("!getterPC() && !setterPC() && !toStringPC() && !equalsPC() && !hashCodePC()" + " && " +
            "(logConfig() || validateConfig() || tlcConfig() || rateLimitConfig())")
    public Object cut(ProceedingJoinPoint pjp) throws Throwable {
        long start = Clock.currentTimeMillis();
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        LogConfig logConfig = logConfig();
        ValidateConfig validateConfig = validateConfig();
        TLCConfig tlcConfig = tlcConfig();
        RateLimitConfig rateLimitConfig = rateLimitConfig();

        if (rateLimitConfig != null) {
            rateLimit(pjp);
        }

        Object result = null;
        Set<BaseQuery<?, ?>> enableBaseQueries = null;
        if (tlcConfig != null) {
            enableBaseQueries = enableBqs.computeIfAbsent(method, m -> {
                Set<Class<? extends BaseQuery<?, ?>>> disableBQCs = Collect.asSet(tlcConfig.getDisableBaseQueries());
                return BeanUtil.getBeansOfType(BaseQuery.class).stream().filter(aBQ -> disableBQCs.contains(aBQ.getClass()))
                        .map(bq -> (BaseQuery<?, ?>) bq).collect(Collectors.toSet());
            });
        }

        Throwable backUp = null;
        try {
            if (validateConfig != null) {
                ValidateUtil.validate(pjp.getTarget(), method, args,
                        validateConfig.isFailFast(), validateConfig.getType(), validateConfig.getGroups());
            }
            if (tlcConfig != null) {
                enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
            }
            result = pjp.proceed();
            if (validateConfig != null) {
                ValidateUtil.validate(pjp.getTarget(), method, result,
                        validateConfig.isFailFast(), validateConfig.getType(), validateConfig.getGroups());
            }
        } catch (Throwable throwable) {
            backUp = throwable;
            throw throwable;
        } finally {
            if (tlcConfig != null) {
                enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
            }
            IntStream.range(0, args.length).forEach(i -> logger.with("arg" + i, args[i]));
            logger.result(result).cost(Clock.currentTimeMillis() - start);
            if (backUp == null) {
                if (logConfig.isDebug()) {
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

