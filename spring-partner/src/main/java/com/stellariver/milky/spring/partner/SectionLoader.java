package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionLoader {

    private static final long DEFAULT_STEP = 100L;

    final JdbcTemplate jdbcTemplate;

    final ResultSetExtractor<UniqueIdBuilder.IdBuilderDO> extractor;

    public SectionLoader(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        extractor = rs -> {
            if (!rs.next()) {
                return null;
            }
            UniqueIdBuilder.IdBuilderDO idBuilderDO = UniqueIdBuilder.IdBuilderDO.builder()
                    .nameSpace(rs.getString(1))
                    .id(rs.getLong(2))
                    .step(rs.getLong(3))
                    .version(rs.getLong(4))
                    .build();
            if (rs.next()) {
                throw new IllegalStateException("name_space should be unique");
            }
            return idBuilderDO;
        };
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pair<AtomicLong, Long> load(String tableName, String nameSpace) {
        validateTableName(tableName);
        String selectSql = "SELECT name_space, id, step, version FROM " + tableName + " WHERE name_space = ?";
        String updateSql = "UPDATE " + tableName + " SET id = ?, version = ? WHERE name_space = ? AND version = ?";
        String insertSql = "INSERT INTO " + tableName + " (name_space, id, step, version) VALUES (?, ?, ?, ?)";

        int optimisticCount;
        int times = 0;
        Pair<AtomicLong, Long> section;
        do {
            UniqueIdBuilder.IdBuilderDO idBuilderDO;
            try {
                idBuilderDO = jdbcTemplate.query(selectSql, extractor, nameSpace);
            } catch (BadSqlGrammarException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                    throw new IllegalStateException("unique_id table not exist, please execute sql in your db" +
                            "CREATE TABLE `unique_id` (\n" +
                            "  `name_space` varchar(50) NOT NULL,\n" +
                            "  `id` bigint(20) DEFAULT NULL,\n" +
                            "  `step` bigint(20) DEFAULT NULL,\n" +
                            "  `version` int(11) DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`name_space`)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                }
                throw ex;
            }

            if (idBuilderDO == null) {
                try {
                    jdbcTemplate.update(insertSql, nameSpace, 1L, DEFAULT_STEP, 1L);
                } catch (DuplicateKeyException ignore) {
                    // concurrent insert
                }
                idBuilderDO = jdbcTemplate.query(selectSql, extractor, nameSpace);
            }

            if (idBuilderDO == null) {
                throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
            }

            AtomicLong start = new AtomicLong(idBuilderDO.getId());
            long end = idBuilderDO.getId() + idBuilderDO.getStep();
            section = Pair.of(start, end);
            long version = idBuilderDO.getVersion();
            optimisticCount = jdbcTemplate.update(updateSql, end, version + 1, nameSpace, version);
            if (times++ > UniqueIdBuilder.MAX_TIMES) {
                throw new UniqueIdBuilder.OptimisticLockException(
                        String.format("uniqueIdBuilder %s optimistic lock exception", nameSpace));
            }
        } while (optimisticCount < 1);
        return section;
    }

    private static void validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("invalid unique id table name: " + tableName);
        }
    }

}
