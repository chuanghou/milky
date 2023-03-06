package com.stellariver.milky.domain.support.dependency;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trace {

    String beanName;

    Field field;

    Object[] params;

    Object bean;

    Method method;

    Object result;

}
