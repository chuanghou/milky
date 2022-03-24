package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.tool.dependency.BeanLoader;

import java.lang.annotation.Annotation;
import java.util.List;

public class BeanUtils {

    static private BeanLoader beanLoader;

    static public void setBeanLoader(BeanLoader beanLoader) {
        BeanUtils.beanLoader = beanLoader;
    }

    static public List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        return beanLoader.getBeansForAnnotation(annotationType);
    }

    static public <T> List<T> getBeansOfType(Class<T> type) {
        return beanLoader.getBeansOfType(type);
    }

    static public <T> T getBean(Class<T> requiredType) {
        return beanLoader.getBean(requiredType);
    }

    static public Object getBean(String beanName) {
        return beanLoader.getBean(beanName);
    }

}
