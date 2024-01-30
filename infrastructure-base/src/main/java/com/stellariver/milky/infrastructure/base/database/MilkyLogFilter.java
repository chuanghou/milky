package com.stellariver.milky.infrastructure.base.database;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.logging.LogFilter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;

@CustomLog
public class MilkyLogFilter extends LogFilter {

    private final SQLUtils.FormatOption option = new SQLUtils.FormatOption(false, false);

    @Override
    protected void connectionLog(String message) {

    }

    @Override
    protected void statementLog(String message) {

    }

    @Override
    protected void statementLogError(String message, Throwable error) {

    }

    @Override
    protected void resultSetLog(String message) {

    }

    @Override
    protected void resultSetLogError(String message, Throwable error) {

    }

    @Override
    public String getDataSourceLoggerName() {
        return null;
    }

    @Override
    public void setDataSourceLoggerName(String loggerName) {

    }

    @Override
    public String getConnectionLoggerName() {
        return null;
    }

    @Override
    public void setConnectionLoggerName(String loggerName) {

    }

    @Override
    public String getStatementLoggerName() {
        return null;
    }

    @Override
    public void setStatementLoggerName(String loggerName) {

    }

    @Override
    public String getResultSetLoggerName() {
        return null;
    }

    @Override
    public void setResultSetLoggerName(String loggerName) {

    }


    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
        print(statement, sql);
    }

    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        String sql;
        if (statement instanceof PreparedStatementProxy) {
            sql = ((PreparedStatementProxy) statement).getSql();
        } else {
            sql = statement.getBatchSql();
        }
        print(statement, sql);
    }

    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
        print(statement, sql);
    }

    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        print(statement, sql);
    }

    public void print(StatementProxy statement, String sql) {

        int parametersSize = statement.getParametersSize();
        if (parametersSize != 0) {
            List<Object> parameters = new ArrayList<>(parametersSize);
            for (int i = 0; i < parametersSize; ++i) {
                JdbcParameter jdbcParam = statement.getParameter(i);
                parameters.add(jdbcParam != null ? jdbcParam.getValue() : null);
            }

            DbType dbType = DbType.of(statement.getConnectionProxy().getDirectDataSource().getDbType());
            sql = SQLUtils.format(sql, dbType, parameters, option);
        }

        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        double cost = nanos / (1000 * 1000);

        log.cost(cost).info(sql);

    }

}
