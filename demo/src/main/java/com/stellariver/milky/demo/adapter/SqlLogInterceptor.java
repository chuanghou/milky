package com.stellariver.milky.demo.adapter;

import com.stellariver.milky.aspectj.tool.entrance.AnnotationInterceptor;
import com.stellariver.milky.infrastructure.base.database.SqlLogFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

public class SqlLogInterceptor implements AnnotationInterceptor {

    @Override
    public void executeBefore(ProceedingJoinPoint pjp) {
        SqlLogFilter.enableSelectSqlThreadLocal();
    }

    @Override
    public void executeFinally(ProceedingJoinPoint pjp) {
        SqlLogFilter.disableSelectSqlThreadLocal();
    }

    @Override
    public boolean conditional(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        return method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }

    @Override
    public void logThrowable(ProceedingJoinPoint pjp, Throwable throwable) {

    }
}
