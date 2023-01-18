package com.stellariver.milky.common.tool.validate;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.*;

public class OfEnumValidator implements ConstraintValidator<OfEnum, Object> {

    private final Set<Object> enumKeys = new HashSet<>();

    // 枚举值初始化
    @Override
    @SneakyThrows
    public void initialize(OfEnum anno) {
        Class<? extends Enum<?>> clazz = anno.enumType();
        Enum<?>[] enumConstants = clazz.getEnumConstants();
        Field field = null;
        if (!StringUtils.isBlank(anno.field())) {
            field = clazz.getDeclaredField(anno.field());
            field.setAccessible(true);
        }
        for (Enum<?> enumConstant : enumConstants) {
            Object key = field != null ? field.get(enumConstant) : enumConstant.name();
            enumKeys.add(key);
        }
    }

    @Override
    public boolean isValid(Object key, ConstraintValidatorContext constraintValidatorContext) {
        return enumKeys.contains(key);
    }
}
