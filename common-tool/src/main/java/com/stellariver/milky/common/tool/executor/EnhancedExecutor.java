package com.stellariver.milky.common.tool.executor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author houchuang
 */
@Slf4j
public class EnhancedExecutor extends ThreadPoolExecutor {

    private final ThreadLocal<String> taskIdentify = new ThreadLocal<>();
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private final Cache<String, Profile> history = CacheBuilder.newBuilder().maximumSize(100).build();
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
        Thread.UncaughtExceptionHandler exceptionHandler = threadFactory.newThread(() -> {}).getUncaughtExceptionHandler();
        if (exceptionHandler.getClass().equals(ThreadGroup.class)) {
            throw new IllegalArgumentException("A customized Thread.UncaughtExceptionHandler is recommended!");
        }
        this.threadLocalPassers = Kit.op(threadLocalPassers).orElseGet(ArrayList::new);
    }

    public EnhancedExecutor(EnhancedExecutorConfiguration configuration,
                            ThreadFactory threadFactory,
                            List<ThreadLocalPasser<?>> threadLocalPassers) {
        this(configuration, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy(), threadLocalPassers);
    }



    @Override
    protected void afterExecute(Runnable runnable, Throwable t) {
        String identify = taskIdentify.get();
        Profile profile = profiles.remove(identify);
        profile.setThrowable(t);
        history.put(identify, profile);
    }

    @Override
    @SneakyThrows
    public void execute(@NonNull Runnable runnable) {
        final Thread superThread = Thread.currentThread();
        HashMap<Class<?>, Object> threadLocalMap = new HashMap<>(16);
        threadLocalPassers.forEach(passer -> threadLocalMap.put(passer.getClass(), passer.prepareThreadLocal()));

        String identify = taskIdentify.get();
        if (identify == null) {
            throw new IllegalArgumentException("task identify is null");
        }
        Profile profile = Profile.builder().identify(identify).submitTime(LocalTime.now()).build();
        Profile shouldNull = profiles.putIfAbsent(identify, profile);
        if (shouldNull != null) {
            throw new IllegalArgumentException("task identify repeat: " + identify);
        }

        super.execute(() -> {

            if (superThread == Thread.currentThread()) {
                runnable.run();
                return;
            }

            Profile currentProfile = profiles.get(identify);
            taskIdentify.set(identify);
            currentProfile.setThread(Thread.currentThread());
            currentProfile.setStartTime(LocalTime.now());
            threadLocalPassers.forEach(passer -> passer.pass(threadLocalMap.get(passer.getClass())));
            try {
                if (Boolean.TRUE.equals(currentProfile.getManualStop())) {
                    log.info("Manual stop task " + identify + "success!" );
                    return;
                }
                runnable.run();
            } finally {
                currentProfile.setEndTime(LocalTime.now());
                threadLocalPassers.forEach(ThreadLocalPasser::clearThreadLocal);
            }

        });

    }

    @Nullable
    public Profile profile(String identify) {
        return Optional.ofNullable(profiles.get(identify)).orElse(history.getIfPresent(identify));
    }

    public void stop(String identify) {

        Profile profile = profiles.get(identify);
        if (profile == null) {
            return;
        }

        Thread profileThread = profile.getThread();
        if (profileThread == null) {
            profile.setManualStop(true);
        }

        Thread thread = profile.getThread();
        thread.interrupt();
    }

}