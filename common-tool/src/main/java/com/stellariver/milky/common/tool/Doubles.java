package com.stellariver.milky.common.tool;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Doubles {

    public static double add(double left, double... right) {
        BigDecimal l = BigDecimal.valueOf(left);
        for (double r : right) {
            l = l.add(BigDecimal.valueOf(r));
        }
        return l.stripTrailingZeros().doubleValue();
    }

    public static double subtract(double left, double... right) {
        BigDecimal l = BigDecimal.valueOf(left);
        for (double r : right) {
            l = l.subtract(BigDecimal.valueOf(r));
        }
        return l.stripTrailingZeros().doubleValue();
    }

    public static double multiply(double left, double... right) {
        BigDecimal l = BigDecimal.valueOf(left);
        for (double r : right) {
            l = l.multiply(BigDecimal.valueOf(r));
        }
        return l.stripTrailingZeros().doubleValue();
    }

    public static double divide(double left, double right, int scale, RoundingMode roundingMode) {
        return BigDecimal.valueOf(left).divide(BigDecimal.valueOf(right), scale, roundingMode).stripTrailingZeros().doubleValue();
    }

    public static double divide(double left, double right, int scale) {
        return BigDecimal.valueOf(left).divide(BigDecimal.valueOf(right), scale, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
    }

    public static Pair<Double, Double> divideAndRemainder(double left, double right) {
        BigDecimal[] bigDecimals = BigDecimal.valueOf(left).divideAndRemainder(BigDecimal.valueOf(right));
        return Pair.of(bigDecimals[0].stripTrailingZeros().doubleValue(), bigDecimals[1].stripTrailingZeros().doubleValue());
    }

    public static double divideToIntegralValue(double left, double right) {
        return BigDecimal.valueOf(left).divideToIntegralValue(BigDecimal.valueOf(right)).stripTrailingZeros().doubleValue();
    }

    public static double remainder(double left, double right) {
        return BigDecimal.valueOf(left).remainder(BigDecimal.valueOf(right)).stripTrailingZeros().doubleValue();
    }

    public static double scale(double value, int bits, RoundingMode roundingMode) {
        return BigDecimal.valueOf(value).setScale(bits, roundingMode).stripTrailingZeros().doubleValue();
    }

    public static double scale(double value, int bits) {
        return BigDecimal.valueOf(value).setScale(bits, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
    }

}
