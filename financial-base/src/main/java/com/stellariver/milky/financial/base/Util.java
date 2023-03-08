package com.stellariver.milky.financial.base;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysEx;
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
        SysEx.falseThrow(equals, ErrorEnumsBase.PARAM_FORMAT_WRONG.message(value));
        return new BigDecimal(value.substring(0, value.length() - 1)).divide(PERCENT, RoundingMode.UNNECESSARY);
    }

    static public boolean same(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) == 0;
    }

    static public boolean greater(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) > 0;
    }

    static public boolean greaterOrSame(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) >= 0;
    }

    static public boolean less(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) < 0;
    }

    static public boolean lessOrSame(@NonNull BigDecimal left, @NonNull BigDecimal right) {
        return left.compareTo(right) <= 0;
    }

}
