package com.stellariver.milky.spring.partner;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpringBeanLoader implements ApplicationContextAware {

    private static ApplicationContext staticApplicationContext;

    private final ApplicationContext applicationContext;

    public SpringBeanLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        return new ArrayList<>(applicationContext.getBeansWithAnnotation(annotationType).values());
    }

    public <T> List<T> getBeansOfType(Class<T> type) {
        return new ArrayList<T>(applicationContext.getBeansOfType(type).values());
    }

    public <T> T getBean(Class<T> requiredType) {
        return staticApplicationContext.getBean(requiredType);
    }

    public Object getBean(String beanName) {
        return staticApplicationContext.getBean(beanName);
    }

    public static <T> T staticGetBean(Class<T> requiredType) {
        return staticApplicationContext.getBean(requiredType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        staticApplicationContext = applicationContext;
    }

}
