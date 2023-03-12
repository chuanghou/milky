package com.stellariver.milky.validate.tool;

import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;

public class OfEnumValidator implements ConstraintValidator<OfEnum, Object> {

    private Set<Object> enumKeys = new HashSet<>();

    @Override
    public void initialize(OfEnum anno) {
        Class<? extends Enum<?>> clazz = anno.enumType();
        Enum<?>[] enumConstants = clazz.getEnumConstants();
        Field field = null;
        if (!StringUtils.isBlank(anno.field())) {
            try {
                field = clazz.getDeclaredField(anno.field());
            } catch (NoSuchFieldException ignore) {}
            if (field != null) {
                field.setAccessible(true);
            }
        }
        for (Enum<?> enumConstant : enumConstants) {
            Object key = null;
            try {
                key = field != null ? field.get(enumConstant) : enumConstant.name();
            } catch (IllegalAccessException ignore) {}
            boolean add = enumKeys.add(key);
            SysEx.falseThrow(add, CONFIG_ERROR.message(clazz.getSimpleName() + " field: " + key + " duplicated"));
        }
        if (anno.selected().length != 0) {
            boolean b = field == null || field.getType().equals(String.class);
            SysEx.falseThrow(b, CONFIG_ERROR.message("配置的key类型不是字符串"));
            Set<Object> selectKeys = Arrays.stream(anno.selected()).collect(Collectors.toSet());
            Set<Object> diff = Collect.diff(selectKeys, enumKeys);
            SysEx.falseThrow(diff.isEmpty(), CONFIG_ERROR.message("selected keys 包含枚举类中未配置keys" + diff));
            enumKeys = selectKeys;
        }

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        boolean valid = enumKeys.contains(value);
        if (!valid) {
            HibernateConstraintValidatorContext hibernateContext
                    = constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
            hibernateContext.addExpressionVariable("enumKeys", enumKeys.toString());
        }
        return valid;
    }

}
