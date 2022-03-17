package com.stellariver.milky.common.tool.utils;

import org.apache.commons.lang3.time.DateUtils;


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

}
