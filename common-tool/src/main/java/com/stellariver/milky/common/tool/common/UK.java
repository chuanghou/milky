package com.stellariver.milky.common.tool.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

/**
 * @author houchuang
 */
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

}
