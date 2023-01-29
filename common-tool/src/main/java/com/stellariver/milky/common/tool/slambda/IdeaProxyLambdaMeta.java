package com.stellariver.milky.common.tool.slambda;

import com.stellariver.milky.common.tool.exception.SysException;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 在 IDEA 的 Evaluate 中执行的 Lambda 表达式元数据需要使用该类处理元数据
 * <p>
 * Create by hcl at 2021/5/17
 */
public class IdeaProxyLambdaMeta implements LambdaMeta {
    private static final Field FIELD_MEMBER_NAME;
    private static final Field FIELD_MEMBER_NAME_CLAZZ;
    private static final Field FIELD_MEMBER_NAME_NAME;

    static {
        try {
            Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
            FIELD_MEMBER_NAME = Reflection.setAccessible(classDirectMethodHandle.getDeclaredField("member"));
            Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
            FIELD_MEMBER_NAME_CLAZZ = Reflection.setAccessible(classMemberName.getDeclaredField("clazz"));
            FIELD_MEMBER_NAME_NAME = Reflection.setAccessible(classMemberName.getDeclaredField("name"));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new SysException(e);
        }
    }

    private final Class<?> clazz;
    private final String name;

    @SneakyThrows
    public IdeaProxyLambdaMeta(Proxy func) {
        InvocationHandler handler = Proxy.getInvocationHandler(func);
        Object dmh = Reflection.setAccessible(handler.getClass().getDeclaredField("val$target")).get(handler);
        Object member = FIELD_MEMBER_NAME.get(dmh);
        clazz = (Class<?>) FIELD_MEMBER_NAME_CLAZZ.get(member);
        name = (String) FIELD_MEMBER_NAME_NAME.get(member);
    }

    @Override
    public String getImplMethodName() {
        return name;
    }

    @Override
    public Class<?> getInstantiatedClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return clazz.getSimpleName() + "::" + name;
    }

}
