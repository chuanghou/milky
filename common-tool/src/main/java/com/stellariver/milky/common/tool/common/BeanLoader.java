package com.stellariver.milky.common.tool.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Component
public class BeanLoader implements ApplicationContextAware {

    @Resource
    ApplicationContext applicationContext;

    private static ApplicationContext staticApplicationContext;

    public List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        return new ArrayList<>(applicationContext.getBeansWithAnnotation(annotationType).values());
    }

    public <T> List<T> getBeansOfType(Class<T> type) {
        return new ArrayList<T>(applicationContext.getBeansOfType(type).values());
    }

    public <T> T getBean(Class<T> requiredType) {
        return staticApplicationContext.getBean(requiredType);
    }

    public static <T> T staticGetBean(Class<T> requiredType) {
        return staticApplicationContext.getBean(requiredType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        staticApplicationContext = applicationContext;
    }

}
