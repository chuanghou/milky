package com.stellariver.milky.common.tool.common;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 *
 * <p>System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）</p>
 * <p>System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道</p>
 * <p>后台定时更新时钟，JVM退出时，线程自动回收</p>
 * <p>10亿：43410,206,210.72815533980582%</p>
 * <p>1亿：4699,29,162.0344827586207%</p>
 * <p>1000万：480,12,40.0%</p>
 * <p>100万：50,10,5.0%</p>
 *
 * @author hubin
 * @since 2016-08-01
 */
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
