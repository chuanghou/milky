package com.stellariver.milky.domain.support.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author houchuang
 */
public class ThreadLocalFutureTask<T> extends FutureTask<T> {

    private final List<ThreadLocalPasser<?>> threadLocalPassers;

    private final Map<Class<? extends ThreadLocalPasser<?>>, Object> threadLocals = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ThreadLocalFutureTask(Callable<T> callable, List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(callable);
        this.threadLocalPassers = threadLocalPassers;
        threadLocalPassers.forEach(passer -> {
            Class<? extends ThreadLocalPasser<?>> clazz = (Class<? extends ThreadLocalPasser<?>>) passer.getClass();
            threadLocals.put(clazz, passer.prepareThreadLocal());
        });
    }

    @SuppressWarnings("unchecked")
    public ThreadLocalFutureTask(Runnable runnable, T value, List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(runnable, value);
        this.threadLocalPassers = threadLocalPassers;
        threadLocalPassers.forEach(passer -> {
            Class<? extends ThreadLocalPasser<?>> clazz = (Class<? extends ThreadLocalPasser<?>>) passer.getClass();
            threadLocals.put(clazz, passer.prepareThreadLocal());
        });
    }

    @Override
    public void run() {
        try {
            threadLocalPassers.forEach(threadLocalPasser -> {
                Object threadLocal = threadLocals.get(threadLocalPasser.getClass());
                threadLocalPasser.pass(threadLocal);
            });
            super.run();
        } finally {
            threadLocalPassers.forEach(ThreadLocalPasser::clearThreadLocal);
        }
    }

}
