package com.stellariver.milky.common.tool.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class UK {

    @Getter
    private final String key;

    public static UK build(Class<?> clazz) {
        return new UK(clazz.getName());
    }

    public String preFix(String value) {
        return String.format("%s_%s", key, value);
    }

    public String unPreFix(String value) {
        return value.substring(value.length() + 1);
    }

    // please paste in your son class
    static {
        // please change UK.class to your custom class to give initial value of your field
        for (Field field : UK.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null) {
                    continue;
                }
            } catch (Throwable ignore) {}
            String name = field.getName();

            try {
                field.set(null, new UK(name));
            } catch (Throwable ignore) {}
        }
    }

}
