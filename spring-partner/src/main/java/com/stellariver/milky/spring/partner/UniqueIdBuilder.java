package com.stellariver.milky.spring.partner;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniqueIdBuilder{

    final String tableName;
    final String nameSpace;
    final BlockingQueue<Pair<AtomicLong, Long>> queue = new ArrayBlockingQueue<>(1);
    volatile Pair<AtomicLong, Long> section;

    final SectionLoader sectionLoader;

    static final Executor executor = Executors.newSingleThreadExecutor();
    static final int MAX_TIMES = 5;

    public UniqueIdBuilder(String tableName, String nameSpace, SectionLoader sectionLoader) {

        if (StringUtils.isBlank(tableName)) {
            throw new RuntimeException("table name should not be blank!");
        }
        this.tableName = tableName;

        if (StringUtils.isBlank(nameSpace)) {
            throw new RuntimeException("nameSpace should not be blank!");
        }
        this.nameSpace = nameSpace;

        this.sectionLoader = sectionLoader;
    }

    @SneakyThrows
    public Long get() {

        if (null == section) {
            synchronized (this) {
                if (null == section) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Pair<AtomicLong, Long> section0 = sectionLoader.load(tableName, nameSpace);
                            boolean offered0 = queue.offer(section0, 3000, TimeUnit.MILLISECONDS);
                            Pair<AtomicLong, Long> section1 = sectionLoader.load(tableName, nameSpace);
                            boolean offered1 = queue.offer(section1, 5000, TimeUnit.MILLISECONDS);
                            if (!offered0 || !offered1) {
                                throw new ShouldNotAppearException("it should not appear! namespace " + nameSpace);
                            }
                        } catch (Throwable throwable) {
                            log.error("uniqueIdGetter error", throwable);
                        }
                    }, executor);
                    section = queue.poll(4000, TimeUnit.MILLISECONDS);
                    if (section == null) {
                        throw new ShouldNotAppearException("it should not appear! namespace is " + nameSpace);
                    }
                }
            }
        }

        int times = 0;
        do {
            long value = section.getLeft().getAndIncrement();
            if (value < section.getRight()) {
                return value;
            }

            if (value >= section.getRight()) {
                synchronized (this) {
                    if (value >= section.getRight()) {
                        CompletableFuture.runAsync(() -> {
                            try {
                                Pair<AtomicLong, Long> newSection = sectionLoader.load(tableName, nameSpace);
                                boolean offered = queue.offer(newSection, 1000L, TimeUnit.MILLISECONDS);
                                if (!offered) {
                                    throw new ShouldNotAppearException("it should not appear! namespace " + nameSpace);
                                }
                            } catch (Throwable throwable) {
                                log.error("uniqueIdGetter error", throwable);
                            }
                        }, executor);
                        section = queue.poll(1000, TimeUnit.MILLISECONDS);
                        if (section == null) {
                            throw new ShouldNotAppearException("it should not appear! namespace is " + nameSpace);
                        }
                    }
                }
            }

            if (times++ > MAX_TIMES) {
                throw new OptimisticLockException("optimistic lock compete! namespace: " + nameSpace);
            }
        }while (true);
    }

    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class IdBuilderDO {
        String nameSpace;
        Long id;
        Long step;
        Long version;
    }

    public static class HavenNotCreateTableException extends RuntimeException{

        public HavenNotCreateTableException(String message) {
            super((message));
        }

    }

    public static class NamespaceInitFormatException extends RuntimeException{

        public NamespaceInitFormatException(String message) {
            super((message));
        }

    }

    public static class OptimisticLockException extends RuntimeException{

        public OptimisticLockException(String message) {
            super((message));
        }

    }

    public static class ShouldNotAppearException extends RuntimeException{

        public ShouldNotAppearException(String message) {
            super((message));
        }

    }
}
