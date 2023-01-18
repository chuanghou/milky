package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class OfEnumValidator implements ConstraintValidator<OfEnum, Object> {

    private Set<Object> enumKeys = new HashSet<>();

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
        if (anno.selected().length != 0) {
            boolean b = field == null || field.getType().equals(String.class);
            SysException.falseThrow(b, ErrorEnumsBase.CONFIG_ERROR.message("配置的key类型不是字符串"));
            Set<Object> selectKeys = Arrays.stream(anno.selected()).collect(Collectors.toSet());
            Set<Object> diff = Collect.diff(selectKeys, enumKeys);
            SysException.falseThrow(diff.isEmpty(),
                    ErrorEnumsBase.CONFIG_ERROR.message("selected keys 包含枚举类中未配置keys" + diff));
            enumKeys = selectKeys;
        }

    }

    @Override
    public boolean isValid(Object key, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid = enumKeys.contains(key);
        if (!valid) {
            HibernateConstraintValidatorContext hibernateContext = constraintValidatorContext.unwrap(
                    HibernateConstraintValidatorContext.class
            );
            hibernateContext.addExpressionVariable("enumKeys", Json.toJson(enumKeys));
        }
        return valid;
    }
}
