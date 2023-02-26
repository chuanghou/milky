package com.stellariver.milky.financial.base;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

    static public final BigDecimal PERCENT = new BigDecimal("100");

    public static String toPercent(@NonNull BigDecimal value) {
        return value.multiply(PERCENT).toPlainString() + "%";
    }

    public static BigDecimal fromPercent(@NonNull String value) {
        boolean equals = value.charAt(value.length() - 1) == '%';
        SysException.falseThrow(equals, ErrorEnumsBase.PARAM_FORMAT_WRONG.message(value));
        return new BigDecimal(value.substring(0, value.length() - 1)).divide(PERCENT, RoundingMode.UNNECESSARY);
    }

    static public boolean same(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) == 0;
    }

    static public boolean greater(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) > 0;
    }

    static public boolean greaterOrSame(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) >= 0;
    }

    static public boolean less(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) < 0;
    }

    static public boolean lessOrSame(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) <= 0;
    }

}
