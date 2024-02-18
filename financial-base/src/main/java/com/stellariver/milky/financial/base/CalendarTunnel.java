package com.stellariver.milky.financial.base;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


public interface CalendarTunnel {

    default boolean isTradingDay(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
        return isTradingDay(localDate);
    }

    default boolean isTradingDay(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return isTradingDay(localDate);
    }

    default boolean isTradingDay() {
        return isTradingDay(LocalDate.now());
    }

    boolean isTradingDay(LocalDate localDate);

    default LocalDate nextTradingDay() {
        return nextTradingDays(1).get(0);
    }

    default Date nextTradingDayOfDate() {
        LocalDate localDate = nextTradingDays(1).get(0);
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    default String nextTradingDayOfString() {
        LocalDate localDate = nextTradingDays(1).get(0);
        return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    List<LocalDate> nextTradingDays(int num);

    default LocalDate lastTradingDay() {
        return lastTradingDays(1).get(0);
    }

    default Date lastTradingDayOfDate() {
        LocalDate localDate = lastTradingDays(1).get(0);
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    default String lastTradingDayOfString() {
        LocalDate localDate = lastTradingDays(1).get(0);
        return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    List<LocalDate> lastTradingDays(int num);

}
