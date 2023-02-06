package com.stellariver.milky.domain.support.interceptor;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.context.Context;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

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
    private MethodAccess methodAccess;
    private int methodIndex;

    public Interceptor(Object bean, Method method, PosEnum posEnum, int order) {
        this.bean = bean;
        this.method = method;
        this.posEnum = posEnum;
        this.order = order;
        this.methodAccess = MethodAccess.get(bean.getClass());
        this.methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
    }

    public void invoke(Object object, AggregateRoot aggregateRoot, Context context) {
        methodAccess.invoke(bean, methodIndex, object, aggregateRoot, context);
    }
}