package com.stellariver.milky.demo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.executor.EnhancedExecutor;
import com.stellariver.milky.common.tool.executor.EnhancedExecutorConfiguration;
import com.stellariver.milky.common.tool.executor.Profile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class EnhancedExecutorTest {

    @Test
    public void testExecutor() throws InterruptedException {

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("async-thread-%d")
                .build();

        EnhancedExecutorConfiguration configuration = EnhancedExecutorConfiguration.builder()
                .corePoolSize(2)
                .maximumPoolSize(2)
                .keepAliveTimeMinutes(10)
                .blockingQueueCapacity(2)
                .build();
        EnhancedExecutor enhancedExecutor;
        String message = null;

        try {
            enhancedExecutor = new EnhancedExecutor(configuration, threadFactory, new ArrayList<>());
        } catch (IllegalArgumentException exception) {
            message = exception.getMessage();
        }
        Assertions.assertEquals(message, "A customized Thread.UncaughtExceptionHandler is recommended!");

        threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> log.error("uncaught exception from executor", e))
                .setNameFormat("async-thread-%d")
                .build();


        enhancedExecutor = new EnhancedExecutor(configuration, threadFactory, new ArrayList<>());
        enhancedExecutor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "1");

        try {
            enhancedExecutor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "1");
        } catch (IllegalArgumentException exception) {
            message = exception.getMessage();
        }

        Assertions.assertTrue(message !=null && message.contains("repeat"));


        Thread.sleep(1500);

        message = null;

        try {
            enhancedExecutor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "1");
        } catch (IllegalArgumentException exception) {
            message = exception.getMessage();
        }

        Assertions.assertNull(message);


    }


    @Test
    public void testProfile() throws InterruptedException {

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> log.error("uncaught exception from executor", e))
                .setNameFormat("async-thread-%d")
                .build();

        EnhancedExecutorConfiguration configuration = EnhancedExecutorConfiguration.builder()
                .corePoolSize(2)
                .maximumPoolSize(2)
                .keepAliveTimeMinutes(10)
                .blockingQueueCapacity(2)
                .build();
        EnhancedExecutor enhancedExecutor = new EnhancedExecutor(configuration, threadFactory, new ArrayList<>());

        enhancedExecutor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "1");

        Profile profile = enhancedExecutor.getProfile("1");
        Assertions.assertNotNull(profile);

        Thread.sleep(1500);
        profile = enhancedExecutor.getProfile("1");
        Assertions.assertNotNull(profile);
        Assertions.assertTrue(profile.getHistory());
        AtomicReference<Boolean> executorMark = new AtomicReference<>(true);

        enhancedExecutor.addByPassPattern("1");

        enhancedExecutor.submit(() -> {
            executorMark.set(false);
            System.out.println("should not appear");
        }, "1");
        Thread.sleep(10);
        Assertions.assertTrue(executorMark.get());


        enhancedExecutor.submit(() -> {
            if (executorMark.get()) {
                throw new RuntimeException("Throwable");
            }
        }, "2");

        Thread.sleep(10);

        profile = enhancedExecutor.getProfile("2");
        Assertions.assertTrue(profile != null && profile.getSubmitTime() != null);
        Assertions.assertNotNull(profile.getStartTime());
        Assertions.assertNotNull(profile.getEndTime());


    }

}
