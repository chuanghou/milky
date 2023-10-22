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

    final JdbcTemplate jdbcTemplate;

    final ResultSetExtractor<UniqueIdBuilder.IdBuilderDO> extractor;

    public SectionLoader(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        extractor = rs -> {
            boolean next = rs.next();
            if (next) {
                UniqueIdBuilder.IdBuilderDO idBuilderDO = UniqueIdBuilder.IdBuilderDO.builder()
                        .nameSpace(rs.getString(1))
                        .id(rs.getLong(2))
                        .step(rs.getLong(3))
                        .version(rs.getLong(4))
                        .build();
                next = rs.next();
                if (next) {
                    throw new RuntimeException("NameSpace should be unique!");
                } else {
                    return idBuilderDO;
                }
            } else {
                return null;
            }
        };
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pair<AtomicLong, Long> load(String tableName, String nameSpace) {

        String SELECT_SQL_TEMPLATE = "SELECT name_space, id, step, version FROM %s WHERE name_space = '%s';";
        String selectSql = String.format(SELECT_SQL_TEMPLATE, tableName, nameSpace);
        String UPDATE_SQL_TEMPLATE = "UPDATE %s SET id = ?, version = ? WHERE name_space = '%s' AND version = ?;";
        String updateSql = String.format(UPDATE_SQL_TEMPLATE, tableName, nameSpace);

        int optimisticCount, times = 0;
        Pair<AtomicLong, Long> section;
        do {
            UniqueIdBuilder.IdBuilderDO idBuilderDO;
            try {
                idBuilderDO = jdbcTemplate.query(selectSql, extractor);
            } catch (BadSqlGrammarException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                    throw new RuntimeException("unique_id table not exist, please execute sql in your db" +
                            "CREATE TABLE `unique_id` (\n" +
                            "  `name_space` varchar(50) NOT NULL,\n" +
                            "  `id` bigint(20) DEFAULT NULL,\n" +
                            "  `step` bigint(20) DEFAULT NULL,\n" +
                            "  `version` int(11) DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`name_space`)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                } else {
                    throw ex;
                }
            }

            if (idBuilderDO == null) {
                String insertSql = String.format("INSERT INTO %s VALUES('%s', %s, %s, %s);", tableName, nameSpace, 1, 100, 1);
                try {
                    jdbcTemplate.execute(insertSql);
                } catch (DuplicateKeyException ignore) {}
                idBuilderDO = jdbcTemplate.query(selectSql, extractor);
            }

            if (idBuilderDO == null) {
                throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
            }

            AtomicLong start = new AtomicLong(idBuilderDO.getId());
            long end = idBuilderDO.getId() + idBuilderDO.getStep();
            section = Pair.of(start, end);
            long version = idBuilderDO.getVersion();
            optimisticCount = jdbcTemplate.update(updateSql, end, version + 1, version);
            if (times++ > UniqueIdBuilder.MAX_TIMES) {
                String message = String.format("uniqueIdBuilder %s optimistic lock exception", nameSpace);
                throw new UniqueIdBuilder.OptimisticLockException(message);
            }
        } while (optimisticCount < 1);
        return section;
    }

}
