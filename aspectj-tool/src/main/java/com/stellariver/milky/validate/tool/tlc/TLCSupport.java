//package com.stellariver.milky.validate.tool.tlc;
//
//import com.stellariver.milky.common.tool.common.BaseQuery;
//import com.stellariver.milky.common.tool.common.BeanUtil;
//import com.stellariver.milky.common.tool.util.Collect;
//import lombok.SneakyThrows;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * @author houchuang
// */
//@Aspect
//@SuppressWarnings({"aspect", "MissingAspectjAutoproxyInspection", "unused"})
//public class TLCSupport{
//
//    private Set<BaseQuery<?, ?>> enableBaseQueries;
//
//    @Pointcut("@annotation(com.stellariver.milky.validate.tool.tlc.EnableTLC)")
//    void pointCut() {}
//
//    @Around("pointCut() && @annotation(enableTLC)")
//    public Object resultResponseHandler(ProceedingJoinPoint pjp, EnableTLC enableTLC) throws Throwable {
//        Object result;
//        if (enableBaseQueries == null) {
//            Set<Class<? extends BaseQuery<?, ?>>> enableBQCs = Collect.asSet(enableTLC.enableBaseQueries());
//            enableBaseQueries = BeanUtil.getBeansOfType(BaseQuery.class).stream()
//                    .filter(aBQ -> enableBQCs.contains(aBQ.getClass())).map(bq -> (BaseQuery<?, ?>) bq)
//                    .collect(Collectors.toSet());
//        }
//        enableBaseQueries.forEach(BaseQuery::enableThreadLocal);
//        try {
//            result = pjp.proceed();
//        } finally {
//            enableBaseQueries.forEach(BaseQuery::clearThreadLocal);
//        }
//        return result;
//    }
//
//}
