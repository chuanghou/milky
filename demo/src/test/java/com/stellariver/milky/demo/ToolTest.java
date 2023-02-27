package com.stellariver.milky.demo;


import com.stellariver.milky.common.tool.util.Collect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public class ToolTest {

    interface Subject {

        void print();


    }

    static class SubjectImpl implements Subject {

        @Override
        public void print() {
            System.out.println("hello");
        }

    }

    public static void main(String[] args) {

        SubjectImpl subject = new SubjectImpl();

        Subject proxyInstance = (Subject) Proxy.newProxyInstance(
                subject.getClass().getClassLoader(),
                subject.getClass().getInterfaces(),
                (proxy, method, args1) -> {
                    System.out.println("执行目标方法前");
                    // 执行目标方法
                    Object result = method.invoke(subject, args1);
                    System.out.println("执行目标方法后");
                    // 返回目标方法的执行结果
                    return result;
                });
        proxyInstance.print();
    }
}