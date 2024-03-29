package com.stellariver.milky.common.tool.validate;

import com.stellariver.milky.common.base.Compare;
import com.stellariver.milky.common.base.DateFormat;
import com.stellariver.milky.common.tool.common.Clock;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;

public class DateFormatValidator implements ConstraintValidator<DateFormat, Object> {

    private String format;
    private Compare compare;
    private Duration delay;
    private boolean blankAsNull;

    @Override
    public void initialize(DateFormat anno) {
        format = anno.format();
        compare = anno.compare();
        delay = Duration.of(anno.delay(), anno.unit());
        blankAsNull = anno.blankAsNull();
    }

    @Override
    @SneakyThrows
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }

        if (!(value instanceof String)) {
            return false;
        }

        if (StringUtils.isBlank((String) value) && blankAsNull) {
            return true;
        }

        if (format.length() != ((String) value).length()) {
            return false;
        }

        Date param;
        try {
             param = DateUtils.parseDate((String) value, format);
        } catch (ParseException e) {
            return false;
        }

        if (compare == Compare.NOT_CHECK) {
            return true;
        }

        String formattedNow = DateFormatUtils.format(Clock.now(), format);
        Date formattedNowDate = DateUtils.parseDate(formattedNow, format);
        long l = formattedNowDate.getTime() + delay.toMillis();

        return compare.compare(param, new Date(l));
    }

}
