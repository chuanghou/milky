package com.stellariver.milky.common.tool.slambda;

import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Reflect;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class IdeaProxyLambdaMeta implements LambdaMeta {
    private static final Field FIELD_MEMBER_NAME;
    private static final Field FIELD_MEMBER_NAME_CLAZZ;
    private static final Field FIELD_MEMBER_NAME_NAME;

    static {
        try {
            Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
            FIELD_MEMBER_NAME = Reflect.setAccessible(classDirectMethodHandle.getDeclaredField("member"));
            Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
            FIELD_MEMBER_NAME_CLAZZ = Reflect.setAccessible(classMemberName.getDeclaredField("clazz"));
            FIELD_MEMBER_NAME_NAME = Reflect.setAccessible(classMemberName.getDeclaredField("name"));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new SysEx(e);
        }
    }

    private final Class<?> clazz;
    private final String name;

    @SneakyThrows
    public IdeaProxyLambdaMeta(Proxy func) {
        InvocationHandler handler = Proxy.getInvocationHandler(func);
        Object dmh = Reflect.setAccessible(handler.getClass().getDeclaredField("val$target")).get(handler);
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
