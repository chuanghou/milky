package com.stellariver.milky.starter;

import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.spring.partner.SpringBeanLoader;

import java.lang.annotation.Annotation;
import java.util.List;

public class BeanLoaderImpl implements BeanLoader {

    private final SpringBeanLoader springBeanLoader;

    public BeanLoaderImpl(SpringBeanLoader springBeanLoader) {
        this.springBeanLoader = springBeanLoader;
    }

    @Override
    public List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        return springBeanLoader.getBeansForAnnotation(annotationType);
    }

    @Override
    public <T> List<T> getBeansOfType(Class<T> type) {
        return springBeanLoader.getBeansOfType(type);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return springBeanLoader.getBean(requiredType);
    }

    @Override
    public Object getBean(String beanName) {
        return springBeanLoader.getBean(beanName);
    }
}
