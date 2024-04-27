package com.stellariver.milky.common.base;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * @author houchuang
 */
public interface BeanLoader {

    List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType);

    <T> List<T> getBeansOfType(Class<T> type);

    <T> T getBean(Class<T> requiredType);

    Object getBean(String beanName);

    <T> Optional<T> getBeanOptional(Class<T> requiredType);

    Optional<Object> getBeanOptional(String beanName);

}
