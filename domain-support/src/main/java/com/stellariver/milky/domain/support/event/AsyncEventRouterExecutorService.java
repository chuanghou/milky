package com.stellariver.milky.domain.support.event;

import java.util.List;
import java.util.concurrent.*;

public class AsyncEventRouterExecutorService extends ThreadPoolExecutor {

    List<ThreadLocalTransfer<?>> threadLocalTransfers;


    public AsyncEventRouterExecutorService(int corePoolSize,
                                           int maximumPoolSize,
                                           int keepAliveTime,
                                           TimeUnit minutes,
                                           ArrayBlockingQueue<Runnable> workQueue,
                                           ThreadFactory threadFactory,
                                           CallerRunsPolicy callerRunsPolicy,
                                           List<ThreadLocalTransfer<?>> threadLocalTransfers) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, minutes, workQueue, threadFactory, callerRunsPolicy);
        this.threadLocalTransfers = threadLocalTransfers;
    }
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ThreadLocalFutureTask<>(runnable, value, threadLocalTransfers);
    }
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ThreadLocalFutureTask<>(callable, threadLocalTransfers);
    }

}
