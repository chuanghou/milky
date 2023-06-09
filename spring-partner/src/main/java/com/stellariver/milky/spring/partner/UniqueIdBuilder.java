package com.stellariver.milky.spring.partner;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniqueIdBuilder {

    final String namespace;
    final String tableName;
    final JdbcTemplate jdbcTemplate;
    final ResultSetExtractor<IdBuilderDO> extractor;
    final BlockingQueue<Pair<AtomicLong, Long>> queue = new ArrayBlockingQueue<>(1);
    final String selectSql;
    final String updateSql;
    volatile Pair<AtomicLong, Long> section;

    static private final Executor executor = Executors.newSingleThreadExecutor();
    static private final int MAX_TIMES = 5;

    public UniqueIdBuilder(DataSource dataSource, String tableName, String nameSpace) {
        this.namespace = nameSpace;
        this.tableName = tableName;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        String SELECT_SQL_TEMPLATE = "SELECT name_space, id, step, version FROM %s WHERE name_space = '%s'; ";
        this.selectSql = String.format(SELECT_SQL_TEMPLATE, tableName, nameSpace);
        String UPDATE_SQL_TEMPLATE = "UPDATE %s SET id = ?, version = ? WHERE name_space = '%s' AND version = ?;";
        this.updateSql = String.format(UPDATE_SQL_TEMPLATE, tableName, nameSpace);

        extractor = rs -> {
            boolean next = rs.next();
            if (next) {
                return IdBuilderDO.builder()
                        .nameSpace(rs.getString(1))
                        .id(rs.getLong(2))
                        .step(rs.getLong(3))
                        .version(rs.getLong(4))
                        .build();
            } else {
                String message = String.format("the namespace %s haven't been initialized, " +
                                "please execute sql like  \"insert into %s values ('%s', start, step, 1)\"" +
                                "'start' stands for the first id to be got, and 'step' means the section length, " +
                                "at least 100, or else this idGetter will be meaningless ",
                        namespace, tableName, namespace);
                throw new NotInitNamespaceException(message);
            }
        };
    }


    @SneakyThrows
    public Long get() {

        if (null == section) {
            synchronized (this) {
                if (null == section) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            queue.put(doLoadSectionFromDB());
                            queue.put(doLoadSectionFromDB());
                        } catch (Throwable throwable) {
                            log.error("uniqueIdGetter error", throwable);
                        }
                    }, executor);
                    section = queue.take();
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
                                queue.put(doLoadSectionFromDB());
                            } catch (Throwable throwable) {
                                log.error("uniqueIdGetter error", throwable);
                            }
                        }, executor);
                        section = queue.take();
                    }
                }
            }

            if (times++ > MAX_TIMES) {
                throw new RuntimeException("optimistic lock compete!");
            }
        }while (true);
    }

    public Pair<AtomicLong, Long> doLoadSectionFromDB() {
        int count;
        Pair<AtomicLong, Long> section;
        int times = 0;
        do {
            IdBuilderDO idBuilderDO;
            try {
                idBuilderDO = jdbcTemplate.query(selectSql, extractor);
            } catch (BadSqlGrammarException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                    String message = String.format("uniqueIdGetter need a table %s to hold the id record", tableName);
                    throw new HavenNotCreateTableException(message);
                }
                throw ex;
            }

            if (idBuilderDO == null) {
                throw new RuntimeException("unreachable code, because if there is not a row, the extractor will find it before");
            }

            AtomicLong start = new AtomicLong(idBuilderDO.getId());
            long end = idBuilderDO.getId() + idBuilderDO.getStep();
            section = Pair.of(start, end);
            idBuilderDO.setId(idBuilderDO.getId() + idBuilderDO.getStep());
            long newStart = idBuilderDO.getId() + idBuilderDO.getStep();
            long version = idBuilderDO.getVersion();
            count = jdbcTemplate.update(updateSql, newStart, version + 1, version);
            if (times++ > MAX_TIMES) {
                String message = String.format("uniqueIdBuilder %s optimistic lock exception", namespace);
                throw new OptimisticLockException(message);
            }
        } while (count < 1);
        return section;

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

    public static class NotInitNamespaceException extends RuntimeException{

        public NotInitNamespaceException(String message) {
            super((message));
        }

    }

    public static class OptimisticLockException extends RuntimeException{

        public OptimisticLockException(String message) {
            super((message));
        }

    }
}
