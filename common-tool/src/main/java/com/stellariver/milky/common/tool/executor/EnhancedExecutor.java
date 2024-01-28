package com.stellariver.milky.common.tool.executor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author houchuang
 */
@Slf4j
public class EnhancedExecutor extends ThreadPoolExecutor {

    static private Field futureTaskCallable;
    static private Field runnableAdapterTask;

    private final Map<String, Profile> forward = new ConcurrentHashMap<>();
    private final Map<Runnable, String> backward = new ConcurrentHashMap<>();
    private final Cache<String, Profile> history = CacheBuilder.newBuilder().maximumSize(100).build();

    static {
        try {
            futureTaskCallable = FutureTask.class.getDeclaredField("callable");
            futureTaskCallable.setAccessible(true);
            runnableAdapterTask = Class.forName("java.util.concurrent.Executors.RunnableAdapter").getDeclaredField("task");
            runnableAdapterTask.setAccessible(true);
        } catch (Throwable ignore) {}
    }

    private final List<ThreadLocalPasser<?>> threadLocalPassers;

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
    public EnhancedExecutor(EnhancedExecutorConfiguration configuration,
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

    public EnhancedExecutor(EnhancedExecutorConfiguration configuration,
                            ThreadFactory threadFactory,
                            List<ThreadLocalPasser<?>> threadLocalPassers) {
        this(configuration, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy(), threadLocalPassers);
    }

    @Override
    @SneakyThrows
    protected void beforeExecute(Thread t, Runnable runnable) {

        String identify;
        if (runnable instanceof FutureTask){
            FutureTask<?> futureTask = (FutureTask<?>) runnable;
            Callable<?> callable = (Callable<?>) futureTaskCallable.get(futureTask);
            if (callable.getClass().getName().equals("java.util.concurrent.Executors.RunnableAdapter")) {
                identify = ((IdentifiedRunnable) runnableAdapterTask.get(callable)).getIdentify();
            } else {
                identify = ((IdentifiedCallable<?>) callable).getIdentify();
            }
        } else if ((runnable instanceof IdentifiedRunnable)){
            identify = ((IdentifiedRunnable) runnable).getIdentify();
        } else {
            throw new RuntimeException();
        }

        SysEx.trueThrow(forward.containsKey(identify), ErrorEnumsBase.REPEAT_IDENTIFY);
        Profile profile = Profile.builder().identify(identify).runnable(runnable).start(LocalTime.now()).build();
        forward.put(identify, profile);
        backward.put(runnable, identify);

    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable t) {
        String identify = backward.remove(runnable);
        Profile profile = forward.remove(identify);
        history.put(identify, profile);
    }

    @Override
    @SneakyThrows
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


    public Profile profile(String identify) {
        return forward.get(identify);
    }

}