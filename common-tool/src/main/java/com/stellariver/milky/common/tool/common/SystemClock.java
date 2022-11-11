package com.stellariver.milky.common.tool.common;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
public class SystemClock {

    private final long period;
    private final AtomicLong now;

    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    public static long now() {
        return instance().currentTimeMillis();
    }

    public static Date date() {
        return new Date(instance().currentTimeMillis());
    }

    public static Date yesterday() {
        return beforeNow(1);
    }

    public static String yesterdayDs() {
        return DateFormatUtils.format(yesterday(), "yyyyMMdd");
    }

    public static Date beforeNow(int days) {
        return new Date(instance().currentTimeMillis() - 1000L * 3600 * 24 * days);
    }

    public static String beforeNowDs(int days) {
        return DateFormatUtils.format(beforeNow(days), "yyyyMMdd");
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    private long currentTimeMillis() {
        return now.get();
    }

    private static class InstanceHolder {
        public static final SystemClock INSTANCE = new SystemClock(1);
    }
}
