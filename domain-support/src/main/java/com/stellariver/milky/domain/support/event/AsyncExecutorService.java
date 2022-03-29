package com.stellariver.milky.domain.support.event;

import java.util.List;
import java.util.concurrent.*;

public class AsyncExecutorService extends ThreadPoolExecutor {

    List<ThreadLocalPasser<?>> threadLocalPassers;

    public AsyncExecutorService(AsyncExecutorConfiguration configuration, ThreadFactory threadFactory,
                                List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(configuration.getCorePoolSize(),
                configuration.getMaximumPoolSize(),
                configuration.getKeepAliveTimeMinutes(),
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(configuration.getBlockingQueueCapacity()),
                threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        this.threadLocalPassers = threadLocalPassers;
    }


    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ThreadLocalFutureTask<>(runnable, value, threadLocalPassers);
    }
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ThreadLocalFutureTask<>(callable, threadLocalPassers);
    }

}
