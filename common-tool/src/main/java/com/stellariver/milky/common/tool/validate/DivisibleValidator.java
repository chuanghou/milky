package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.Divisible;
import com.stellariver.milky.common.base.SysEx;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;

public class DivisibleValidator implements ConstraintValidator<Divisible, Object> {

    private Integer divisor;

    Double delta;

    @Override
    public void initialize(Divisible divisible) {

        divisor = divisible.value();
        delta = divisible.delta();

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
        } else if (value instanceof Float) {
            float v = ((Float) value) % divisor;
            valid = Math.abs(v) < delta;
        } else if (value instanceof Double) {
            double v = ((Double) value) % divisor;
            valid = Math.abs(v) < delta;
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
