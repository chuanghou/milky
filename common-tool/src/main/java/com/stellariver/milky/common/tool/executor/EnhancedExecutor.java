package com.stellariver.milky.common.tool.executor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author houchuang
 */
@Slf4j
public class EnhancedExecutor extends ThreadPoolExecutor {

    private final ThreadLocal<String> taskIdentify = new ThreadLocal<>();
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private final Cache<String, Profile> history = CacheBuilder.newBuilder().maximumSize(100).build();
    private final List<ThreadLocalPasser<?>> threadLocalPassers;
    private final Map<String, Pattern> byPassPatterns = new ConcurrentHashMap<>();

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


    public Future<?> submit(Runnable task, @NonNull String identify) {
        taskIdentify.set(identify);
        try {
            return super.submit(task);
        } finally {
            taskIdentify.remove();
        }
    }

    public <T> Future<T> submit(Runnable task, T result, @NonNull String identify) {
        taskIdentify.set(identify);
        try {
            return super.submit(task, result);
        } finally {
            taskIdentify.remove();
        }
    }

    public <T> Future<T> submit(Callable<T> task, @NonNull String identify) {
        taskIdentify.set(identify);
        try {
            return super.submit(task);
        } finally {
            taskIdentify.remove();
        }
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable t) {
        String identify = taskIdentify.get();
        Profile profile = profiles.remove(identify);
        profile.setHistory(true);
        history.put(identify, profile);
    }


    @Override
    public void execute(@NonNull Runnable runnable) {
        final Thread superThread = Thread.currentThread();
        HashMap<Class<?>, Object> threadLocalMap = new HashMap<>(16);
        threadLocalPassers.forEach(passer -> threadLocalMap.put(passer.getClass(), passer.prepareThreadLocal()));

        String identify = taskIdentify.get();
        if (identify == null) {
            throw new IllegalArgumentException("Task identify is null");
        }

        for (Map.Entry<String, Pattern> entry : byPassPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(identify);
            if (matcher.find()) {
                log.warn("Task: " + identify + " has been by passed by pattern " + entry.getKey() + " !");
                return;
            }
        }

        Profile profile = Profile.builder().identify(identify).submitTime(LocalTime.now()).history(false).build();
        Profile shouldNull = profiles.putIfAbsent(identify, profile);
        if (shouldNull != null) {
            throw new IllegalArgumentException("Task identify: " + identify + " repeat!");
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

    public List<String> byPassPatterns() {
        return new ArrayList<>(byPassPatterns.keySet());
    }

    public void addByPassPattern(String pattern) {
        if (byPassPatterns.size() > 100) {
            throw new IllegalArgumentException("Already have 100 patterns");
        }
        byPassPatterns.put(pattern, Pattern.compile(pattern));
    }

    public void removeByPassPattern(String pattern) {
        byPassPatterns.remove(pattern);
    }

}