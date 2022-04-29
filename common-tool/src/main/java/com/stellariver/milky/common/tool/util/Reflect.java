package com.stellariver.milky.common.tool.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Reflect {

    @SuppressWarnings("unchecked")
    static public <T> List<Class<? extends T>> ancestorClasses(Class<? extends T> clazz) {
        List<Class<? extends T>> classes = new ArrayList<>(Collections.singletonList(clazz));
        Class<?> superClazz = clazz.getSuperclass();
        while (!Objects.equals(superClazz, Object.class)) {
            classes.add((Class<? extends T>) superClazz);
            superClazz = superClazz.getSuperclass();
        }
        classes.add((Class<? extends T>) superClazz);
        Collections.reverse(classes);
        return classes;
    }

}
