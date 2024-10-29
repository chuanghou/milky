package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.OfEnum;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;

public class OfEnumValidator implements ConstraintValidator<OfEnum, Object> {

    private final Set<Object> enumKeys = new HashSet<>();

    private boolean blankAsNull = false;

    @Override
    public void initialize(OfEnum anno) {
        Class<? extends Enum<?>> clazz = anno.enumType();
        List<Enum<?>> enumConstants = Arrays.stream(clazz.getEnumConstants()).collect(Collectors.toList());
        if (anno.selectedEnums().length != 0) {
            Set<String> existedEnumNames = enumConstants.stream().map(Enum::name).collect(Collectors.toSet());
            Set<String> selectedEnums = Arrays.stream(anno.selectedEnums()).collect(Collectors.toSet());
            Set<String> diff = Collect.subtract(selectedEnums, existedEnumNames);
            SysEx.falseThrow(diff.isEmpty(), CONFIG_ERROR.message("selected Enums 包含未配置枚举" + diff));
            enumConstants = enumConstants.stream().filter(e -> selectedEnums.contains(e.name())).collect(Collectors.toList());
        }

        Field field = null;
        if (!StringUtils.isBlank(anno.field())) {
            try {
                field = clazz.getDeclaredField(anno.field());
            } catch (NoSuchFieldException ignore) {}
            Reflect.setAccessible(field);
        }

        for (Enum<?> enumConstant : enumConstants) {
            Object key = null;
            try {
                key = field != null ? field.get(enumConstant) : enumConstant.name();
            } catch (IllegalAccessException ignore) {}
            enumKeys.add(key);
        }

        if (anno.blankAsNull()) {
            blankAsNull = true;
        }

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        List<Object> vs = Collect.asList(value);
        if (value == null) {
            return true;
        } else if (value instanceof String && StringUtils.isBlank((String)value) && blankAsNull) {
            return true;
        } else if (value instanceof Collection) {
            vs = new ArrayList<>((Collection<?>) value);
        }

        for (Object v : vs) {
            boolean valid = enumKeys.contains(v);
            if (!valid) {
                HibernateConstraintValidatorContext hibernateContext
                        = constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
                hibernateContext.addExpressionVariable("enumKeys", enumKeys.toString());
                return false;
            }
        }
        return true;
    }

}
