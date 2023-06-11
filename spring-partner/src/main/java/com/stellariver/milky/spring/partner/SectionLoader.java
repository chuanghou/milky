package com.stellariver.milky.spring.partner;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicLong;

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
                String message = String.format("the namespace %s haven't been initialized, " +
                                "please execute sql like  \"insert into %s values ('%s', start, step, 1)\"" +
                                "'start' stands for the first id to be got, and 'step' means the section length, " +
                                "at least 100, or else this idGetter will be meaningless ",
                        "nameSpace", "tableName", "nameSpace");
                throw new UniqueIdBuilder.NamespaceInitFormatException(message);
            }
        };
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pair<AtomicLong, Long> load(String tableName, String nameSpace) {

        String SELECT_SQL_TEMPLATE = "SELECT name_space, id, step, version FROM %s WHERE name_space = '%s'; ";
        String selectSql = String.format(SELECT_SQL_TEMPLATE, tableName, nameSpace);
        String UPDATE_SQL_TEMPLATE = "UPDATE %s SET id = ?, version = ? WHERE name_space = '%s' AND version = ?;";
        String updateSql = String.format(UPDATE_SQL_TEMPLATE, tableName, nameSpace);

        int count;
        Pair<AtomicLong, Long> section;
        int times = 0;
        do {
            UniqueIdBuilder.IdBuilderDO idBuilderDO;
            try {
                idBuilderDO = jdbcTemplate.query(selectSql, extractor);
            } catch (BadSqlGrammarException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                    String message = String.format("uniqueIdGetter need a table %s to hold the id record", tableName);
                    throw new UniqueIdBuilder.HavenNotCreateTableException(message);
                }
                throw ex;
            }

            if (idBuilderDO == null) {
                throw new UniqueIdBuilder.NamespaceInitFormatException("unreachable code, because if there is not a valid row, the extractor will find it before");
            }
            if (idBuilderDO.getId() < 0 || idBuilderDO.getStep() < 100 || idBuilderDO.getVersion() < 0) {
                String message = String.format("idBuildDO %s not valid", idBuilderDO);
                throw new UniqueIdBuilder.NamespaceInitFormatException(message);
            }

            AtomicLong start = new AtomicLong(idBuilderDO.getId());
            long end = idBuilderDO.getId() + idBuilderDO.getStep();
            section = Pair.of(start, end);
            idBuilderDO.setId(idBuilderDO.getId() + idBuilderDO.getStep());
            long newStart = idBuilderDO.getId() + idBuilderDO.getStep();
            long version = idBuilderDO.getVersion();
            count = jdbcTemplate.update(updateSql, newStart, version + 1, version);
            if (times++ > UniqueIdBuilder.MAX_TIMES) {
                String message = String.format("uniqueIdBuilder %s optimistic lock exception", nameSpace);
                throw new UniqueIdBuilder.OptimisticLockException(message);
            }
        } while (count < 1);
        return section;
    }

}
