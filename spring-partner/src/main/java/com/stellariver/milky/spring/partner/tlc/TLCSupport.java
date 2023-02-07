package com.stellariver.milky.spring.partner.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
@Data
@Aspect
@Order
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TLCSupport{

    private final List<BaseQuery<?, ?>> availableBaseQueries;

    private Set<BaseQuery<?, ?>> enableBaseQueries;

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.tlc.EnableTLC)")
    void pointCut() {}

    @SneakyThrows(Throwable.class)
    @Around("pointCut() && @annotation(enableTLC)")
    public Object resultResponseHandler(ProceedingJoinPoint pjp, EnableTLC enableTLC) {
        Object result;
        if (enableBaseQueries == null) {
            Set<Class<? extends BaseQuery<?, ?>>> enableBQCs = Collect.asSet(enableTLC.enableBaseQueries());
            enableBaseQueries = availableBaseQueries.stream()
                    .filter(aBQ -> enableBQCs.contains(aBQ.getClass())).collect(Collectors.toSet());
        }
        enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            result = pjp.proceed();
        } finally {
            enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
        }
        return result;
    }

}
