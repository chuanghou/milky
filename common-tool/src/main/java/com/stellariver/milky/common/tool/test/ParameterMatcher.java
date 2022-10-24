package com.stellariver.milky.common.tool.test;

import com.stellariver.milky.common.tool.common.Kit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mockito.ArgumentMatcher;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class ParameterMatcher<T> implements ArgumentMatcher<T> {

    final T value;

    @Override
    @SneakyThrows
    public boolean matches(T t) {
        for (Field field : value.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object o = field.get(this.value);
            if (o != null) {
                boolean equals = Kit.eq(o, field.get(t));
                if (!equals) {
                    return false;
                }
            }
        }
        return true;
    }

}
