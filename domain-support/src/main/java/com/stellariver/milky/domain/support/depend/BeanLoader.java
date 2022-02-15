package com.stellariver.milky.domain.support.depend;

import java.lang.annotation.Annotation;
import java.util.List;

public interface BeanLoader {

    List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType);

    <T> List<T> getBeansOfType(Class<T> type);

    <T> T getBean(Class<T> requiredType);

    Object getBean(String beanName);


}
