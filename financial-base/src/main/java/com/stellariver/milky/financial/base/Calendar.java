package com.stellariver.milky.financial.base;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


public interface Calendar {

    boolean isTrading(LocalDate localDate);

    default boolean isTrading(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
        return isTrading(localDate);
    }

    default boolean isTrading(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return isTrading(localDate);
    }

    default boolean isTrading() {
        return isTrading(LocalDate.now());
    }

    List<LocalDate> nextTradings(int num);

    default LocalDate nextTrading() {
        return nextTradings(1).get(0);
    }

    List<LocalDate> lastTradings(int num);

    default LocalDate lastTradingDay() {
        return lastTradings(1).get(0);
    }

}
