package com.stellariver.milky.spring.partner.tlc;

import com.stellariver.milky.common.tool.common.BaseQuery;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.InitializingBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Aspect
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TLCSupport{

    private final List<BaseQuery<?, ?>> baseQueries;

    @Pointcut("@annotation(com.stellariver.milky.spring.partner.tlc.EnableTLC)")
    void pointCut() {}

    @SneakyThrows
    @Around("pointCut() && @annotation(enableTLC)")
    public Object resultResponseHandler(ProceedingJoinPoint pjp, EnableTLC enableTLC) {
        Object result;
        Set<Class<? extends BaseQuery<?, ?>>> baseQueryClasses =
                Arrays.stream(enableTLC.disableBaseQueries()).collect(Collectors.toSet());
        List<? extends BaseQuery<?, ?>> enableBaseQueries = baseQueries.stream()
                .filter(baseQuery -> !baseQueryClasses.contains(baseQuery.getClass())).collect(Collectors.toList());
        enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
        try {
            result = pjp.proceed();
        } finally {
            enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
        }
        return result;
    }

}
