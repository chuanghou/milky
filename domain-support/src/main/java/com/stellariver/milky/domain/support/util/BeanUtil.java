package com.stellariver.milky.domain.support.util;

import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.domain.support.dependency.BeanLoader;
import net.sf.cglib.beans.BeanMap;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 */
public class BeanUtil {

    private static BeanLoader beanLoader;

    public static void setBeanLoader(BeanLoader beanLoader) {
        BeanUtil.beanLoader = beanLoader;
    }

    public static List<Object> getBeansForAnnotation(Class<? extends Annotation> annotationType) {
        SysException.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBeansForAnnotation(annotationType);
    }

    public static <T> List<T> getBeansOfType(Class<T> type) {
        SysException.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBeansOfType(type);
    }

    public static <T> T getBean(Class<T> requiredType) {
        SysException.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBean(requiredType);
    }

    public static Object getBean(String beanName) {
        SysException.nullThrow(beanLoader, "beanLoader need to be set by container");
        return beanLoader.getBean(beanName);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> beanToMap(Object bean) {
        return null == bean ? new HashMap<>() : BeanMap.create(bean);
    }

}
