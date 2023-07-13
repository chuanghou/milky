package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.TimeFormat;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.util.Date;

public class TimeFormatValidator implements ConstraintValidator<TimeFormat, Object> {

    private String format;
    private boolean checkNotEarlier;
    @Override
    public void initialize(TimeFormat anno) {
        format = anno.format();
        checkNotEarlier = anno.checkNotEarlier();
    }

    @Override
    @SneakyThrows
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        Date param;
        try {
             param = DateUtils.parseDate((String) value, format);
        } catch (ParseException e) {
            return false;
        }

        if (checkNotEarlier) {
            Date now = DateUtils.parseDate(DateFormatUtils.format(new Date(), format), format);
            return !param.before(now);
        }
        return true;
    }

}
