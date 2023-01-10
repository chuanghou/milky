package com.stellariver.milky.spring.partner;

import com.stellariver.milky.domain.support.dependency.BeanLoader;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author houchuang
 */
public class BeanLoaderImpl implements BeanLoader {

    private final ApplicationContext applicationContext;

    public BeanLoaderImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        return new ArrayList<>(applicationContext.getBeansWithAnnotation(annotationType).values());
    }

    @Override
    public <T> List<T> getBeansOfType(Class<T> type) {
        return new ArrayList<>(applicationContext.getBeansOfType(type).values());
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    @Override
    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

}
