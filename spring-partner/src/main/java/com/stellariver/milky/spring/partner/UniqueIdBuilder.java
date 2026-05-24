package com.stellariver.milky.spring.partner;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniqueIdBuilder {

    /** 作废号段上自增时的重试上限（正常切换由 advanceSection 短锁串行化） */
    static final int MAX_TIMES = 8;

    private static final long PREFETCH_OFFER_TIMEOUT_MS = 30_000L;

    private static final ExecutorService PREFETCH_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "milky-unique-id-prefetch");
        thread.setDaemon(true);
        return thread;
    });

    final String tableName;
    final String nameSpace;
    final BlockingQueue<Section> prefetchQueue = new ArrayBlockingQueue<>(1);
    final SectionLoader sectionLoader;
    final AtomicReference<Section> currentSection = new AtomicReference<>();

    public UniqueIdBuilder(String tableName, String nameSpace, SectionLoader sectionLoader) {
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("table name should not be blank");
        }
        if (StringUtils.isBlank(nameSpace)) {
            throw new IllegalArgumentException("nameSpace should not be blank");
        }
        this.tableName = tableName;
        this.nameSpace = nameSpace;
        this.sectionLoader = sectionLoader;
    }

    public Long get() {
        ensureInitialized();
        while (true) {
            Section segment = currentSection.get();
            long id = segment.cursor.getAndIncrement();
            if (id < segment.endExclusive) {
                return id;
            }
            if (currentSection.get() != segment) {
                continue;
            }
            advanceSection(segment);
        }
    }

    private void ensureInitialized() {
        if (currentSection.get() != null) {
            return;
        }
        synchronized (this) {
            if (currentSection.get() != null) {
                return;
            }
            currentSection.set(toSection(sectionLoader.load(tableName, nameSpace)));
            prefetchNextSection();
        }
    }

    /**
     * 号段用尽时切换：短锁避免多线程同时 sync load 打爆 DB 乐观锁；热路径自增仍在锁外。
     */
    private void advanceSection(Section exhausted) {
        if (currentSection.get() != exhausted) {
            return;
        }
        synchronized (this) {
            if (currentSection.get() != exhausted) {
                return;
            }
            for (int attempt = 0; attempt <= MAX_TIMES; attempt++) {
                Section next = prefetchQueue.poll();
                if (next == null) {
                    log.warn("unique id prefetch miss, sync load. namespace={}", nameSpace);
                    next = toSection(sectionLoader.load(tableName, nameSpace));
                }
                if (currentSection.compareAndSet(exhausted, next)) {
                    prefetchNextSection();
                    return;
                }
                if (currentSection.get() != exhausted) {
                    return;
                }
            }
            throw new OptimisticLockException("optimistic lock compete! namespace: " + nameSpace);
        }
    }

    private void prefetchNextSection() {
        PREFETCH_EXECUTOR.execute(() -> {
            if (!prefetchQueue.isEmpty()) {
                return;
            }
            try {
                Section loaded = toSection(sectionLoader.load(tableName, nameSpace));
                if (!prefetchQueue.offer(loaded, PREFETCH_OFFER_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    log.warn("unique id prefetch queue busy, skip buffer. namespace={}", nameSpace);
                }
            } catch (Throwable throwable) {
                log.error("unique id prefetch failed. namespace={}", nameSpace, throwable);
            }
        });
    }

    private static Section toSection(Pair<AtomicLong, Long> pair) {
        return new Section(pair.getLeft(), pair.getRight());
    }

    static final class Section {

        final AtomicLong cursor;
        final long endExclusive;

        Section(AtomicLong cursor, long endExclusive) {
            this.cursor = cursor;
            this.endExclusive = endExclusive;
        }

    }

    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class IdBuilderDO {
        String nameSpace;
        Long id;
        Long step;
        Long version;
    }

    public static class OptimisticLockException extends RuntimeException {

        public OptimisticLockException(String message) {
            super(message);
        }

    }

}
