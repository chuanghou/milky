package com.stellariver.milky.domain.support.interceptor;

import com.stellariver.milky.common.tool.Runner;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.context.Context;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

@Data
@Builder
@AllArgsConstructor
public class Interceptor {

    private Object bean;

    private Method method;

    private PosEnum posEnum;

    private int order;

    @SneakyThrows
    public void invoke(Object object, AggregateRoot aggregateRoot, Context context) {
        Runner.invoke(bean, method, object, aggregateRoot, context);
    }
}