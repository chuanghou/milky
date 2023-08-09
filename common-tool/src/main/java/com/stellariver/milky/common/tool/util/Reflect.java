package com.stellariver.milky.common.tool.util;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.stellariver.milky.common.tool.slambda.SetAccessibleAction;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author houchuang
 */
public class Reflect {

    @SuppressWarnings("unchecked")
    static public <T> List<Class<? extends T>> ancestorClasses(Class<? extends T> clazz) {
        List<Class<? extends T>> classes = new ArrayList<>();
        Class<?> superClazz = clazz;
        do {
            classes.add((Class<? extends T>) superClazz);
            superClazz = superClazz.getSuperclass();
        } while (superClazz != null);
        Collections.reverse(classes);
        return classes;
    }


    public static <T extends AccessibleObject> T setAccessible(T object) {
        return AccessController.doPrivileged(new SetAccessibleAction<>(object));
    }

    static private final Map<Method, Pair<MethodAccess, Integer>> map = new ConcurrentHashMap<>();

    public static Object invoke(Method method, Object bean, Object... parameters) {
        Pair<MethodAccess, Integer> pair = map.get(method);
        if (pair == null) {
            Class<?> clazz = method.getDeclaringClass();
            MethodAccess methodAccess = MethodAccess.get(clazz);
            int index = methodAccess.getIndex(method.getName(), method.getParameterTypes());
            pair = Pair.of(methodAccess, index);
            map.put(method, pair);
        }
        MethodAccess methodAccess = pair.getKey();
        Integer index = pair.getRight();
        return methodAccess.invoke(bean, index, parameters);
    }

}
