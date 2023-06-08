package com.stellariver.milky.spring.partner;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.stellariver.milky.common.base.ErrorEnumsBase.*;
import static com.stellariver.milky.common.base.SysEx.trueThrow;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UniqueIdGetter {

    private final JdbcTemplate jdbcTemplate;
    private final BlockingQueue<Pair<AtomicLong, Long>> queue = new ArrayBlockingQueue<>(1);
    private final String selectSql;
    private final String updateSql;
    volatile Pair<AtomicLong, Long> section;

    static private final Executor executor = Executors.newSingleThreadExecutor();
    static private final int MAX_TIMES = 5;

    public UniqueIdGetter(DataSource dataSource, String tableName, String nameSpace) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        String SELECT_SQL_TEMPLATE = "SELECT name_space, id, step, version FROM %s WHERE name_space = %s";
        this.selectSql = String.format(SELECT_SQL_TEMPLATE, tableName, nameSpace);
        String UPDATE_SQL_TEMPLATE = "UPDATE %s SET id = ?, version = ? WHERE namespace = %s AND version = ?";
        this.updateSql = String.format(UPDATE_SQL_TEMPLATE, tableName, nameSpace);
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
            IdBuilderDO idBuilderDO = jdbcTemplate.query(selectSql, rs -> {
                return IdBuilderDO.builder()
                        .name_space(rs.getString(1))
                        .id(rs.getLong(2))
                        .step(rs.getLong(3))
                        .version(rs.getLong(4))
                        .build();
            });

            AtomicLong start = new AtomicLong(idBuilderDO.getId());
            long end = idBuilderDO.getId() + idBuilderDO.getStep();
            section = Pair.of(start, end);
            idBuilderDO.setId(idBuilderDO.getId() + idBuilderDO.getStep());
            long newStart = idBuilderDO.getId() + idBuilderDO.getStep();
            long version = idBuilderDO.getVersion();
            count = jdbcTemplate.update(updateSql, newStart, version + 1, version);
            trueThrow(times++ > MAX_TIMES, OPTIMISTIC_COMPETITION);
        } while (count < 1);
        return section;

    }

    @Data
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class IdBuilderDO {
        String name_space;
        Long id;
        Long step;
        Long version;
    }

}
