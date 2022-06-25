package com.stellariver.milky.domain.support.util;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class AsyncExecutor extends ThreadPoolExecutor {

    private final List<ThreadLocalPasser<?>> threadLocalPassers;

    public AsyncExecutor(AsyncExecutorConfiguration configuration,
                         ThreadFactory threadFactory,
                         CallerRunsPolicy callerRunsPolicy,
                         List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(configuration.getCorePoolSize(),
                configuration.getMaximumPoolSize(),
                configuration.getKeepAliveTimeMinutes(),
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(configuration.getBlockingQueueCapacity()),
                threadFactory, callerRunsPolicy);
        this.threadLocalPassers = threadLocalPassers;
    }

    public AsyncExecutor(AsyncExecutorConfiguration configuration,
                         ThreadFactory threadFactory,
                         List<ThreadLocalPasser<?>> threadLocalPassers) {
        this(configuration, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy(), threadLocalPassers);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        final Thread superThread = Thread.currentThread();
        HashMap<Class<?>, Object> threadLocalMap = new HashMap<>();
        threadLocalPassers.forEach(passer -> threadLocalMap.put(passer.getClass(), passer.prepareThreadLocal()));
        super.execute(() -> {
            if (superThread == Thread.currentThread()) {
                command.run();
                return;
            }
            threadLocalPassers.forEach(passer -> passer.pass(threadLocalMap.get(passer.getClass())));
            try {
                command.run();
            } finally {
                threadLocalPassers.forEach(ThreadLocalPasser::clearThreadLocal);
            }
        });
    }
}