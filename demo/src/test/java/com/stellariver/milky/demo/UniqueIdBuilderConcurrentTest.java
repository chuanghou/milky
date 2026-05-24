package com.stellariver.milky.demo;

import com.stellariver.milky.spring.partner.SectionLoader;
import com.stellariver.milky.spring.partner.UniqueIdBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link UniqueIdBuilder} 多线程压测：重点覆盖号段边界 CAS 切换。
 */
@SpringBootTest
class UniqueIdBuilderConcurrentTest {

    private static final int THREAD_COUNT = 32;

    /** 每线程发号次数；总量应跨多个 step 边界 */
    private static final int IDS_PER_THREAD = 1_000;

    private static final long SEGMENT_STEP = 50L;

    @Autowired
    DataSource dataSource;

    @Autowired
    SectionLoader sectionLoader;

    @Test
    void concurrentGet_directBuilder_noDuplicate() throws InterruptedException {
        String namespace = "stress_direct_" + System.nanoTime();
        initNamespace(namespace, 1L, SEGMENT_STEP);

        UniqueIdBuilder builder = new UniqueIdBuilder("unique_id", namespace, sectionLoader);
        warmUp(builder, (int) (SEGMENT_STEP * 3));
        StressResult result = runStress(builder::get);

        Assertions.assertEquals(0, result.duplicates, () -> "duplicate ids: " + result.firstDuplicate);
        Assertions.assertEquals(THREAD_COUNT * IDS_PER_THREAD, result.uniqueCount);
        logResult("direct builder", namespace, result);
    }

    @Test
    void concurrentGet_springBean_noDuplicate() throws InterruptedException {
        String namespace = "stress_spring_" + System.nanoTime();
        initNamespace(namespace, 1L, SEGMENT_STEP);

        UniqueIdBuilder builder = new UniqueIdBuilder("unique_id", namespace, sectionLoader);
        warmUp(builder, (int) (SEGMENT_STEP * 3));
        StressResult result = runStress(builder::get);

        Assertions.assertEquals(0, result.duplicates, () -> "duplicate ids: " + result.firstDuplicate);
        Assertions.assertEquals(THREAD_COUNT * IDS_PER_THREAD, result.uniqueCount);
        logResult("builder (spring SectionLoader)", namespace, result);
    }

    /** 单线程预热：完成首次号段加载与预取，避免压测起跑时 32 路同时 sync load */
    private static void warmUp(UniqueIdBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.get();
        }
    }

    private StressResult runStress(IdSupplier supplier) throws InterruptedException {
        int total = THREAD_COUNT * IDS_PER_THREAD;
        Set<Long> uniqueIds = ConcurrentHashMap.newKeySet(total);
        AtomicInteger duplicates = new AtomicInteger();
        AtomicReference<Long> firstDuplicate = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                pool.execute(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int j = 0; j < IDS_PER_THREAD; j++) {
                            long id = supplier.get();
                            if (!uniqueIds.add(id)) {
                                duplicates.incrementAndGet();
                                firstDuplicate.compareAndSet(null, id);
                            }
                        }
                    } catch (Throwable throwable) {
                        failure.compareAndSet(null, throwable);
                    } finally {
                        done.countDown();
                    }
                });
            }

            Assertions.assertTrue(ready.await(30, TimeUnit.SECONDS), "workers not ready");

            long beginNs = System.nanoTime();
            start.countDown();
            Assertions.assertTrue(done.await(120, TimeUnit.SECONDS), "workers not finished in time");
            long elapsedNs = System.nanoTime() - beginNs;

            if (failure.get() != null) {
                Assertions.fail(failure.get());
            }

            return new StressResult(uniqueIds.size(), duplicates.get(), firstDuplicate.get(), elapsedNs);
        } finally {
            pool.shutdownNow();
        }
    }

    private void initNamespace(String namespace, long id, long step) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("DELETE FROM unique_id WHERE name_space = ?", namespace);
        jdbc.update(
                "INSERT INTO unique_id (name_space, id, step, version) VALUES (?, ?, ?, 1)",
                namespace, id, step);
    }

    private static void logResult(String label, String namespace, StressResult result) {
        double elapsedMs = result.elapsedNs / 1_000_000.0;
        double idsPerSec = result.uniqueCount * 1_000_000_000.0 / result.elapsedNs;
        System.out.printf(
                "[UniqueId stress] %s namespace=%s threads=%d perThread=%d total=%d elapsed=%.1f ms throughput=%.0f ids/s duplicates=%d%n",
                label,
                namespace,
                THREAD_COUNT,
                IDS_PER_THREAD,
                result.uniqueCount,
                elapsedMs,
                idsPerSec,
                result.duplicates);
    }

    @FunctionalInterface
    private interface IdSupplier {
        long get();
    }

    private static final class StressResult {
        final int uniqueCount;
        final int duplicates;
        final Long firstDuplicate;
        final long elapsedNs;

        StressResult(int uniqueCount, int duplicates, Long firstDuplicate, long elapsedNs) {
            this.uniqueCount = uniqueCount;
            this.duplicates = duplicates;
            this.firstDuplicate = firstDuplicate;
            this.elapsedNs = elapsedNs;
        }
    }

}
