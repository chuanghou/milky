package com.stellariver.milky.domain.support.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ThreadLocalFutureTask<T> extends FutureTask<T> {

    private final List<ThreadLocalPasser<?>> threadLocalPassers;

    private final Map<Class<? extends ThreadLocalPasser<?>>, Object> threadLocals = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ThreadLocalFutureTask(Callable<T> callable, List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(callable);
        this.threadLocalPassers = threadLocalPassers;
        threadLocalPassers.forEach(threadLocalPasser ->
                threadLocals.put((Class<? extends ThreadLocalPasser<?>>) threadLocalPasser.getClass(),
                        threadLocalPasser.prepareThreadLocal()));
    }

    @SuppressWarnings("unchecked")
    public ThreadLocalFutureTask(Runnable runnable, T value, List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(runnable, value);
        this.threadLocalPassers = threadLocalPassers;
        threadLocalPassers.forEach(threadLocalPasser ->
                threadLocals.put((Class<? extends ThreadLocalPasser<?>>) threadLocalPasser.getClass(),
                        threadLocalPasser.prepareThreadLocal()));
    }

    @Override
    public void run() {
        try {
            threadLocalPassers.forEach(threadLocalPasser -> {
                Object threadLocal = threadLocals.get(ThreadLocalPasser.class);
                threadLocalPasser.pass(threadLocal);
            });
            super.run();
        } finally {
            threadLocalPassers.forEach(ThreadLocalPasser::clearThreadLocal);
        }
    }
}
