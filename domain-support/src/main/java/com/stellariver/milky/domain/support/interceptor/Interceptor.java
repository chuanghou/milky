package com.stellariver.milky.domain.support.interceptor;

import com.stellariver.milky.common.tool.util.Reflect;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.context.Context;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author houchuang
 */
@Data
public class Interceptor {

    private Object bean;
    private Method method;
    private PosEnum posEnum;
    private int order;

    public Interceptor(Object bean, Method method, PosEnum posEnum, int order) {
        this.bean = bean;
        this.method = method;
        this.posEnum = posEnum;
        this.order = order;
    }

    public void invoke(Object object, AggregateRoot aggregateRoot, Context context) {
        Reflect.invoke(method, bean, object, aggregateRoot, context);
    }
}