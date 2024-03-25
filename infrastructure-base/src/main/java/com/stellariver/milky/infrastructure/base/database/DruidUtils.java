package com.stellariver.milky.infrastructure.base.database;

import com.alibaba.druid.DbType;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;

import java.util.ArrayList;
import java.util.List;

public class DruidUtils {

    private static final SQLUtils.FormatOption option = new SQLUtils.FormatOption(false, false);

    public static String resolveSql(StatementProxy statement, String sql) {

        int parametersSize = statement.getParametersSize();

        if (parametersSize != 0) {
            List<Object> parameters = new ArrayList<>(parametersSize);
            for (int i = 0; i < parametersSize; ++i) {
                JdbcParameter jdbcParam = statement.getParameter(i);
                parameters.add(jdbcParam != null ? jdbcParam.getValue() : null);
            }

            String dbType = statement.getConnectionProxy().getDirectDataSource().getDbType();
            sql = SQLUtils.format(sql, DbType.valueOf(dbType), parameters, option);
        }

        return sql;
    }

}
