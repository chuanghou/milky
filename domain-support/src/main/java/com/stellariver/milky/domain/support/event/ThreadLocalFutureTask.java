package com.stellariver.milky.domain.support.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ThreadLocalFutureTask<T> extends FutureTask<T> {

    private final List<ThreadLocalTransfer<?>> threadLocalTransfers;

    private final Map<Class<? extends ThreadLocalTransfer<?>>, Object> threadLocals = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ThreadLocalFutureTask(Callable<T> callable, List<ThreadLocalTransfer<?>> threadLocalTransfers) {
        super(callable);
        this.threadLocalTransfers = threadLocalTransfers;
        threadLocalTransfers.forEach(threadLocalTransfer ->
                threadLocals.put((Class<? extends ThreadLocalTransfer<?>>) threadLocalTransfer.getClass(),
                        threadLocalTransfer.prepareThreadLocal()));
    }

    @SuppressWarnings("unchecked")
    public ThreadLocalFutureTask(Runnable runnable, T value, List<ThreadLocalTransfer<?>> threadLocalTransfers) {
        super(runnable, value);
        this.threadLocalTransfers = threadLocalTransfers;
        threadLocalTransfers.forEach(threadLocalTransfer ->
                threadLocals.put((Class<? extends ThreadLocalTransfer<?>>) threadLocalTransfer.getClass(),
                        threadLocalTransfer.prepareThreadLocal()));
    }

    @Override
    public void run() {
        try {
            threadLocalTransfers.forEach(threadLocalTransfer -> {
                Object threadLocal = threadLocals.get(ThreadLocalTransfer.class);
                threadLocalTransfer.fillThreadLocal(threadLocal);
            });
            super.run();
        } finally {
            threadLocalTransfers.forEach(ThreadLocalTransfer::clearThreadLocal);
        }
    }
}
