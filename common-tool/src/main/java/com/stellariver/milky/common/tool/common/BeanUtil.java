package com.stellariver.milky.common.tool.common;

import net.sf.cglib.beans.BeanMap;

import java.util.Map;

public class BeanUtil {
    @SuppressWarnings("unchecked")
    public static Map<String, Object> beanToMap(Object bean) {
        return null == bean ? null : BeanMap.create(bean);
    }

}
