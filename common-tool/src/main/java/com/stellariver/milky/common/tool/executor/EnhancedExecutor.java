package com.stellariver.milky.common.tool.executor;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author houchuang
 */
public class EnhancedExecutor extends ThreadPoolExecutor {

    private final List<ThreadLocalPasser<?>> threadLocalPassers;

    private final Map<String, Runnable> tasks = new ConcurrentHashMap<>();

    /**
     * The threadFactory parameter need a unCaughtExceptionHandler like follows
     * <pre> {@code
     * ThreadFactory threadFactory = new ThreadFactoryBuilder()
     *     .setUncaughtExceptionHandler(
     *         (t, e) -> log.error("uncaught exception from executor", e)
     *      )
     *     .setNameFormat("async-thread-%d")
     *     .build();
     * }</pre>
     */
    @SneakyThrows
    public EnhancedExecutor(ExecutorConfiguration configuration,
                            ThreadFactory threadFactory,
                            CallerRunsPolicy callerRunsPolicy,
                            List<ThreadLocalPasser<?>> threadLocalPassers) {
        super(configuration.getCorePoolSize(),
                configuration.getMaximumPoolSize(),
                configuration.getKeepAliveTimeMinutes(),
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(configuration.getBlockingQueueCapacity()),
                threadFactory, callerRunsPolicy);
        this.threadLocalPassers = Kit.op(threadLocalPassers).orElseGet(ArrayList::new);
        Boolean b = CompletableFuture.supplyAsync(() -> {
            Thread.UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
            return handler instanceof ThreadGroup;
        }, this).get();
        SysEx.trueThrow(b, ErrorEnumsBase.CONFIG_ERROR.message("应该手动指定异常处理器"));
    }

    public EnhancedExecutor(ExecutorConfiguration configuration,
                            ThreadFactory threadFactory,
                            List<ThreadLocalPasser<?>> threadLocalPassers) {
        this(configuration, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy(), threadLocalPassers);
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        final Thread superThread = Thread.currentThread();
        HashMap<Class<?>, Object> threadLocalMap = new HashMap<>(16);
        threadLocalPassers.forEach(passer -> threadLocalMap.put(passer.getClass(), passer.prepareThreadLocal()));
        super.execute(() -> {
            if (superThread == Thread.currentThread()) {
                runnable.run();
                return;
            }
            threadLocalPassers.forEach(passer -> passer.pass(threadLocalMap.get(passer.getClass())));
            try {
                runnable.run();
            } finally {
                threadLocalPassers.forEach(ThreadLocalPasser::clearThreadLocal);
            }
        });
    }


    @NonNull @Override
    public Future<?> submit(@NonNull Runnable task) {
        if (task instanceof IdentifiedRunnable) {

        } else {

        }
        return super.submit(task);
    }


    @NonNull @Override
    public <T> Future<T> submit(@NonNull Runnable task, T result) {
        if (task instanceof IdentifiedRunnable) {

        } else {

        }

        return super.submit(task, result);
    }

    @NonNull @Override
    public <T> Future<T> submit(@NonNull Callable<T> task) {
        if (task instanceof IdentifiedCallable) {

        } else {

        }
        return super.submit(task);
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {

        if (r instanceof FutureTask) {
            FutureTask<?> futureTask = (FutureTask<?>) r;
            futureT
        }

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
    }


}