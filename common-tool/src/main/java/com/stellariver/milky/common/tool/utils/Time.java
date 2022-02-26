package com.stellariver.milky.common.tool.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class Time {

    public static Date now() {
        return new Date();
    }

    public static Date yesterday() {
        return DateUtils.addDays(new Date(), -1);
    }

    public static Date tomorrow() {
        return DateUtils.addDays(new Date(), 1);
    }

    public static LocalDateTime transfer(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 此方法慎用，本方法计算天数差时，只计算天数差别
     * @param endDate 终点时间
     * @param startDate 起点时间
     * @return 相差天数
     */
    public static long minusDayPart(Date endDate, Date startDate) {
        LocalDateTime endLocalDateTime = transfer(endDate);
        LocalDateTime startLocalDateTime = transfer(DateUtils.ceiling(startDate, Calendar.DATE));
        return ChronoUnit.DAYS.between(startLocalDateTime, endLocalDateTime);
    }
}
