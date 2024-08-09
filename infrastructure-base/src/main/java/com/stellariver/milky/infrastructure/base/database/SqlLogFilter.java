package com.stellariver.milky.infrastructure.base.database;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.Supplier;

@Setter
@CustomLog
@NoArgsConstructor
public class SqlLogFilter extends FilterEventAdapter {

    private SqlLogConfig sqlLogConfig = new SqlLogConfig();

    public SqlLogFilter(SqlLogConfig sqlLogConfig) {
        this.sqlLogConfig = sqlLogConfig;
    }

    private static final ThreadLocal<Pair<ResultSetProxy, Long>> recordCounter = ThreadLocal.withInitial(() -> Pair.of(null, 0L));
    private static final ThreadLocal<Boolean> byPassEnable = ThreadLocal.withInitial(() -> false);

    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {
        if (byPassEnable.get()) {
            return;
        }
        print(statement, sql);
    }

    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        if (byPassEnable.get()) {
            return;
        }
        String sql;
        if (statement instanceof PreparedStatementProxy) {
            sql = ((PreparedStatementProxy) statement).getSql();
        } else {
            sql = statement.getBatchSql();
        }
        print(statement, sql);
    }

    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet)  {
        if (byPassEnable.get()) {
            return;
        }
        print(statement, sql);
    }

    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        if (byPassEnable.get()) {
            return;
        }
        print(statement, sql);
    }

    @SneakyThrows
    public void print(StatementProxy statement, String sql) {
        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        int updateCount = statement.getUpdateCount();
        double cost = nanos / 1000_000L;
        if (cost > sqlLogConfig.getAlarmSqlCostThreshold()) {
            log.cost(cost).error(DruidUtils.resolveSql(statement, sql) + " ==>> " + "[affected: " + updateCount + "]");
        } else if (statement.getUpdateCount() >= 0 || sqlLogConfig.getEnableSelectSql()){
            log.cost(cost).info(DruidUtils.resolveSql(statement, sql) + " ==>> " + "[affected: " + updateCount + "]");
        }
    }

    public static <T> T byPass(Supplier<T> supplier) {
        byPassEnable.set(true);
        try {
            return supplier.get();
        } finally {
            byPassEnable.set(false);
        }
    }

    @Override
    public boolean resultSet_next(FilterChain chain, ResultSetProxy resultSet) throws SQLException {
        boolean moreRows = super.resultSet_next(chain, resultSet);
        if (moreRows && (!byPassEnable.get())) {

            Pair<ResultSetProxy, Long> proxyCounter = recordCounter.get();
            if (proxyCounter.getKey() == null || proxyCounter.getKey() != resultSet) {
                recordCounter.set(Pair.of(resultSet, 1L));
            } else {
                recordCounter.set(Pair.of(proxyCounter.getLeft(), proxyCounter.getRight() + 1));
            }

            Long countResult = recordCounter.get().getRight();
            if (recordCounter.get().getRight() > sqlLogConfig.getAlarmSqlCountThreshold()) {
                log.arg0(countResult).error(recordSql(resultSet).toString());
            } else if (sqlLogConfig.getEnableSelectSql()){
                log.arg0(countResult).info(recordSql(resultSet).toString());
            }
        }
        return moreRows;
    }

    private StringBuilder recordSql(ResultSetProxy resultSet) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append("record: [");
        ResultSetMetaData meta = resultSet.getMetaData();
        for (int i = 0, size = meta.getColumnCount(); i < size; ++i) {
            if (i != 0) {
                builder.append(", ");
            }
            int columnIndex = i + 1;
            int type = meta.getColumnType(columnIndex);

            Object value;
            if (type == Types.TIMESTAMP) {
                value = resultSet.getTimestamp(columnIndex);
            } else if (type == Types.BLOB) {
                value = "<BLOB>";
            } else if (type == Types.CLOB) {
                value = "<CLOB>";
            } else if (type == Types.NCLOB) {
                value = "<NCLOB>";
            } else if (type == Types.BINARY) {
                value = "<BINARY>";
            } else {
                value = resultSet.getObject(columnIndex);
            }
            builder.append(String.format("%s:%s", meta.getColumnName(columnIndex).toLowerCase(), value));
        }
        builder.append("]");
        return builder;
    }

}
