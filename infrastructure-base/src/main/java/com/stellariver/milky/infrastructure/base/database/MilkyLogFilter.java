package com.stellariver.milky.infrastructure.base.database;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.logging.LogFilter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class MilkyLogFilter extends LogFilter {

    private final SQLUtils.FormatOption option = new SQLUtils.FormatOption(false, true);

    public String resolveSql(StatementProxy statement, String sql) {

        int parametersSize = statement.getParametersSize();
        if (parametersSize == 0) {
            return sql;
        }

        List<Object> parameters = new ArrayList<>(parametersSize);
        for (int i = 0; i < parametersSize; ++i) {
            JdbcParameter jdbcParam = statement.getParameter(i);
            parameters.add(jdbcParam != null ? jdbcParam.getValue() : null);
        }

        DbType dbType = DbType.of(statement.getConnectionProxy().getDirectDataSource().getDbType());
        return SQLUtils.format(sql, dbType, parameters, option);

    }


    public double resolveCost(StatementProxy statement) {
        statement.setLastExecuteTimeNano();
        double nanos = statement.getLastExecuteTimeNano();
        return nanos / (1000 * 1000);
    }

    public Pair<List<String>, List<List<String>>> resolveResultSets() {
        return null;
    }




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
}
