package com.stellariver.milky.common.tool.common;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Clock {

    private final long period;
    volatile private long now;
    volatile int todayInteger;
    volatile String todayString;

    private Clock(long period) {
        this.period = period;
        this.now = System.currentTimeMillis();
        this.todayString = DateFormatUtils.format(new Date(now), "yyyyMMdd");
        this.todayInteger = Integer.parseInt(this.todayString);
        scheduleClockUpdating();
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            now = System.currentTimeMillis();
            todayString = DateFormatUtils.format(new Date(now), "yyyyMMdd");
            todayInteger = Integer.parseInt(todayString);
        }, period, period, TimeUnit.MILLISECONDS);
    }

    public static final Clock INSTANCE = new Clock(1);

    public static long currentTimeMillis() {
        return INSTANCE.now;
    }

    public static Date now() {
        return new Date(INSTANCE.now);
    }

    public static Date beforeNow(int days) {
        return new Date(currentTimeMillis() - 1000L * 3600 * 24 * days);
    }

    public static int todayInteger() {
        return INSTANCE.todayInteger;
    }
    public static String todayString() {
        return INSTANCE.todayString;
    }

}
