package com.stellariver.milky.demo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
@DirtiesContext
public class RedissonTest{

    @Autowired
    RedissonClient redissonClient;

    @Test
    @SneakyThrows
    public void testLock() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
        if (windows) {
            return;
        }
        RLock rLock0 = redissonClient.getLock("test");
        boolean b = rLock0.tryLock();
        Assertions.assertTrue(b);

        CompletableFuture<Boolean> test = CompletableFuture.supplyAsync(() -> {
            RLock rLock1 = redissonClient.getLock("test");
            return rLock1.tryLock();
        });
        Boolean aBoolean = test.get();
        Assertions.assertFalse(aBoolean);

        rLock0.unlock();

        CompletableFuture<Boolean> test1 = CompletableFuture.supplyAsync(() -> {
            RLock rLock1 = redissonClient.getLock("test");
            return rLock1.tryLock();
        });
        Boolean locked = test1.get();
        Assertions.assertTrue(locked);

        //TODO 全局限流
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("test-limiter");

    }

}
