package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.Divisible;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.OfEnum;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Reflect;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;

public class DivisibleValidator implements ConstraintValidator<Divisible, Object> {

    private Integer divisor;

    @Override
    public void initialize(Divisible divisible) {

        divisor = divisible.value();

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid;
        if (value == null) {
            valid = true;
        } else if (value instanceof Integer) {
            valid = ((Integer) value) % divisor == 0;
        } else if (value instanceof Long) {
            valid = ((Long) value) % divisor == 0;
        } else if (value instanceof BigDecimal) {
            BigDecimal remainder = ((BigDecimal) value).divideAndRemainder(BigDecimal.valueOf(divisor))[1];
            valid = remainder.compareTo(BigDecimal.ZERO) == 0;
        } else {
            throw new SysEx(CONFIG_ERROR.message(" not support type" + value.getClass().getSimpleName()));
        }

        if (!valid) {
            HibernateConstraintValidatorContext hibernateContext
                    = constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
            hibernateContext.addExpressionVariable("dividend", value);
            hibernateContext.addExpressionVariable("divisor", divisor);
        }
        return valid;
    }

}
