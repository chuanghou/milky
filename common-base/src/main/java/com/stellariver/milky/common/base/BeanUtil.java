package com.stellariver.milky.common.base;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author houchuang
 */
public class BeanUtil {

    private static BeanLoader beanLoader;

    public static void setBeanLoader(BeanLoader beanLoader) {
        BeanUtil.beanLoader = beanLoader;
    }

    public static BeanLoader getBeanLoader() {
        return beanLoader;
    }

    public static List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        SysEx.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBeansForAnnotation(annotationType);
    }

    public static <T> List<T> getBeansOfType(Class<T> type) {
        if (beanLoader == null) {
            return Collections.emptyList();
        }
        return beanLoader.getBeansOfType(type);
    }

    public static <T> T getBean(Class<T> requiredType) {
        SysEx.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBean(requiredType);
    }

    public static Object getBean(String beanName) {
        SysEx.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBean(beanName);
    }


    public static <T> Optional<T> getBeanOptional(Class<T> requiredType) {
        if (beanLoader == null) {
            return Optional.empty();
        }
        return beanLoader.getBeanOptional(requiredType);
    }

    public static Optional<Object> getBeanOptional(String beanName) {
        if (beanLoader == null) {
            return Optional.empty();
        }
        return beanLoader.getBeanOptional(beanName);
    }

}
