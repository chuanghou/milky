package com.stellariver.milky.demo.basic;

import com.stellariver.milky.common.tool.common.UK;

import java.lang.reflect.Field;

public class UKs extends UK {

    private UKs(String key) {
        super(key);
        throw new RuntimeException("should not be used, just for grammar check!");
    }

    static public UK stableTest;

    static public UK sqlRateLimiter;

    static {
        for (Field field : UKs.class.getDeclaredFields()) {
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
