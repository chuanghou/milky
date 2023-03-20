package com.stellariver.milky.financial.base;

import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;

public class ShareValidator implements ConstraintValidator<Share, Object> {

    private long share;

    @Override
    public void initialize(Share anno) {
       if (anno.value() < 0) {
           throw new SysEx(anno.value() + " should a positive number!");
       }
       share = anno.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        if (value instanceof Integer) {
            return ((Integer) value)%share == 0;
        } else if (value instanceof Long) {
            return ((Long) value)%share == 0;
        } else {
            throw new SysEx("field class" + value.getClass().getSimpleName() + " not appropriate!");
        }
    }

}
