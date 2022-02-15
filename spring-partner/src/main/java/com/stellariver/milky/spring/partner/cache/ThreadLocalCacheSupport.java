package com.stellariver.milky.spring.partner.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.tool.common.ReflectTool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.util.concurrent.TimeUnit;

@Aspect
public class ThreadLocalCacheSupport {

    private static final int MAX_CACHE_SIZE = 1000;

    private static final int LIVE_MIL_SEC = 1000;

    private static final Object NULL = new Object();

    private final ThreadLocal<Cache<Object, Object>> threadLocal = new ThreadLocal<>();

    @Pointcut("@annotation(com.stellariver.milky.common.tool.cache.ThreadLocalCache)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object responseHandler(ProceedingJoinPoint pjp) throws Throwable {
        Object key = SimpleKeyGenerator.generateKey(ReflectTool.methodInfo(pjp));
        Cache<Object, Object> map = threadLocal.get();
        if(map == null) {
            map = CacheBuilder.newBuilder()
                    .maximumSize(MAX_CACHE_SIZE)
                    .expireAfterWrite(LIVE_MIL_SEC, TimeUnit.MILLISECONDS)
                    .build();
            threadLocal.set(map);
        }
        Object o = map.getIfPresent(key);
        if(o == NULL) {
            return null;
        } else if(o != null) {
            return o;
        }

        Object result = pjp.proceed();
        if(result == null) {
            map.put(key, NULL);
        } else {
            map.put(key, result);
        }
        return result;
    }
}
