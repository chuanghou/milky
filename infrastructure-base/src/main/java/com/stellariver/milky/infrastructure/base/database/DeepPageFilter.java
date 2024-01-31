package com.stellariver.milky.infrastructure.base.database;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.stellariver.milky.common.base.BizEx;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stellariver.milky.common.base.ErrorEnumsBase.DEEP_PAGING;

@CustomLog
public class DeepPageFilter extends FilterEventAdapter {

    static private final Pattern LIMIT_PATTERN = Pattern.compile("limit\\s+[0-9]+(((\\s+(offset))|,)\\s+[0-9]+)?$");
    static private final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

    private boolean block = false;

    private long deepLimit = 5000L;

    private Set<Pattern> filterPatterns;

    public DeepPageFilter() {
    }

    public DeepPageFilter(boolean block, long deepLimit) {
        this.block = block;
        this.deepLimit = deepLimit;
    }

    public DeepPageFilter(boolean block, long deepLimit, Set<Pattern> patterns) {
        this.block = block;
        this.deepLimit = deepLimit;
        this.filterPatterns = patterns;
    }

    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
        sql = DruidUtils.resolveSql(statement, sql);
        Set<Pattern> patterns = Optional.ofNullable(filterPatterns).orElseGet(HashSet::new);
        for (Pattern pattern : patterns) {
            boolean filter = pattern.matcher(sql).find();
            if (filter) {
                return;
            }
        }
        sql = sql.substring(sql.length() - 30).toLowerCase();
        Matcher matcher = LIMIT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String value = matcher.group();
            matcher = NUMBER_PATTERN.matcher(value);
            long max = Long.parseLong(matcher.group());
            if (matcher.find()) {
                max = Math.max(max, Long.parseLong(matcher.group()));
            }
            if (max >= deepLimit) {
                sql = SqlFormatter.of(Dialect.MySql).format(sql);
                String message = String.format("fatal error, slow sql possible: \n%s ", sql);
                log.arg0(message).error(DEEP_PAGING.getCode());
                customStrategyWhenFail(sql);
                BizEx.trueThrow(block, DEEP_PAGING.message(message));
            }
        }
    }

    /**
     * custom strategy for extend
     * @param sql deep page sql
     */
    protected void customStrategyWhenFail(String sql) {

    }

}

