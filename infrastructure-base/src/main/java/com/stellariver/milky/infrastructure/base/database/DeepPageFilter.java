package com.stellariver.milky.infrastructure.base.database;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stellariver.milky.common.base.ErrorEnumsBase.DEEP_PAGING;

@CustomLog
public class DeepPageFilter extends FilterEventAdapter {

    private static final SQLUtils.FormatOption option = new SQLUtils.FormatOption(false, true);
    static private final Pattern LIMIT_PATTERN = Pattern.compile("limit\\s+[0-9]+(((\\s+(offset))|,)\\s+[0-9]+)?$");
    static private final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

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

    static private final Set<Pattern> emptyPatterns = new HashSet<>();

    @Override
    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        sql = DruidUtils.resolveSql(statement, sql);
        Set<Pattern> patterns = Optional.ofNullable(filterPatterns).orElse(emptyPatterns);
        for (Pattern pattern : patterns) {
            boolean filter = pattern.matcher(sql).find();
            if (filter) {
                return;
            }
        }

        String tailSql = sql.substring(Math.max(0, sql.length() - 30)).toLowerCase();
        Matcher matcher = LIMIT_PATTERN.matcher(tailSql);
        if (matcher.find()) {
            String value = matcher.group();
            matcher = NUMBER_PATTERN.matcher(value);

            if (!matcher.find()) {
                throw new SysEx(ErrorEnumsBase.UNREACHABLE_CODE);
            }

            long max = Long.parseLong(matcher.group());
            if (matcher.find()) {
                max = Math.max(max, Long.parseLong(matcher.group()));
            }

            String dbType = statement.getConnectionProxy().getDirectDataSource().getDbType();
            if (max >= deepLimit) {
                sql = SQLUtils.format(sql, DbType.valueOf(dbType), option);
                log.error(sql);
                customStrategyWhenFail(sql);
                SysEx.trueThrow(block, DEEP_PAGING.message("\n" + sql));
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

