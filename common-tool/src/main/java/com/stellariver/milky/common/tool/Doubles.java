package com.stellariver.milky.common.tool;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Doubles {

    public static double add(double left, double right) {
        return BigDecimal.valueOf(left).add(BigDecimal.valueOf(right)).doubleValue();
    }

    public static double subtract(double left, double right) {
        return BigDecimal.valueOf(left).subtract(BigDecimal.valueOf(right)).doubleValue();
    }

    public static double multiply(double left, double right) {
        return BigDecimal.valueOf(left).multiply(BigDecimal.valueOf(right)).doubleValue();
    }

    public static double divide(double left, double right, int scale, RoundingMode roundingMode) {
        return BigDecimal.valueOf(left).divide(BigDecimal.valueOf(right), scale, roundingMode).doubleValue();
    }

    public static double divide(double left, double right, int scale) {
        return BigDecimal.valueOf(left).divide(BigDecimal.valueOf(right), scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static Pair<Double, Double> divideAndRemainder(double left, double right) {
        BigDecimal[] bigDecimals = BigDecimal.valueOf(left).divideAndRemainder(BigDecimal.valueOf(right));
        return Pair.of(bigDecimals[0].doubleValue(), bigDecimals[1].doubleValue());
    }

    public static double divideToIntegralValue(double left, double right) {
        return BigDecimal.valueOf(left).divideToIntegralValue(BigDecimal.valueOf(right)).doubleValue();
    }

    public static double remainder(double left, double right) {
        return BigDecimal.valueOf(left).remainder(BigDecimal.valueOf(right)).doubleValue();
    }

    public static double scale(double value, int number, RoundingMode roundingMode) {
        return BigDecimal.valueOf(value).setScale(number, roundingMode).doubleValue();
    }

    public static double scale(double value, int number) {
        return BigDecimal.valueOf(value).setScale(number, RoundingMode.HALF_UP).doubleValue();
    }

}
