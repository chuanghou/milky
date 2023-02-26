package com.stellariver.milky.common.tool.common;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Clock {

    private final long period;
    volatile private long now;
    volatile int today;

    private Clock(long period) {
        this.period = period;
        this.now = System.currentTimeMillis();
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
            today = Integer.parseInt(DateFormatUtils.format(new Date(now), "yyyyMMdd"));
        }, period, period, TimeUnit.MILLISECONDS);

    }

    private static class InstanceHolder {
        public static final Clock INSTANCE = new Clock(1);
    }

    public static long currentTimeMillis() {
        return InstanceHolder.INSTANCE.now;
    }

    public static Date now() {
        return new Date(InstanceHolder.INSTANCE.now);
    }

    public static Date beforeNow(int days) {
        return new Date(currentTimeMillis() - 1000L * 3600 * 24 * days);
    }

    public static int today() {
        return InstanceHolder.INSTANCE.today;
    }

    public static int beforeToday(int days) {
        return InstanceHolder.INSTANCE.today - days;
    }

}
