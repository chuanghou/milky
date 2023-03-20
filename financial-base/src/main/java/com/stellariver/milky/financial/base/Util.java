package com.stellariver.milky.financial.base;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysEx;

import java.math.BigDecimal;

public class Util {

    public static String toPercent(BigDecimal value) {
        return value.movePointRight(2).toPlainString() + "%";
    }

    public static BigDecimal fromPercent(String value) {
        boolean equals = Kit.eq(value.charAt(value.length() - 1), '%');
        SysEx.falseThrowGet(equals, () -> ErrorEnumsBase.PARAM_FORMAT_WRONG.message(value));
        return new BigDecimal(value.substring(0, value.length() - 1)).movePointLeft(2);
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

    static public BigDecimal multiply100(BigDecimal bigDecimal) {
        return bigDecimal.movePointRight(2);
    }

    static public BigDecimal divide100(BigDecimal bigDecimal) {
        return bigDecimal.movePointLeft(2);
    }

}
