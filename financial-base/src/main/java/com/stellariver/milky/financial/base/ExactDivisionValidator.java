package com.stellariver.milky.financial.base;

import com.stellariver.milky.common.base.SysEx;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class ExactDivisionValidator implements ConstraintValidator<ExactDivision, Object> {

    private BigDecimal divider;

    @Override
    public void initialize(ExactDivision anno) {
        divider = new BigDecimal(anno.value());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        if (value instanceof Double || value instanceof Float) {
            throw new SysEx("double or float couldn't be used to describe an exact number, please use int or long, " +
                    "if your number include point, use string");
        }
        BigDecimal bigDecimal = new BigDecimal(value.toString());
        return bigDecimal.remainder(divider).compareTo(BigDecimal.ZERO) == 0;
    }

}
