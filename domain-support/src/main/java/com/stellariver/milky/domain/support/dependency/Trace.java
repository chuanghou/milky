package com.stellariver.milky.domain.support.dependency;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trace {

    Object bean;

    Object[] params;

    Method method;

    Object result;

}
