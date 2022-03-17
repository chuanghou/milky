package com.stellariver.milky.domain.support.interceptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@Builder
@AllArgsConstructor
public class Interceptor {

    private Object bean;

    private Method method;

    private PosEnum posEnum;

    private int order;
}