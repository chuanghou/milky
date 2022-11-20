package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.infrastructure.base.ErrorEnums;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 防止深度分页插件，默认不阻断，分页限制1000条
 *
 * @author houchuang
 * @since 2022-11-03
 */
public class BlockDeepPagingInnerInterceptor implements InnerInterceptor {

    static private final Pattern LIMIT_PATTERN = Pattern.compile("limit\\s+[0-9]+(,\\s+[0-9]+)?$");

    protected final Log logger = LogFactory.getLog(this.getClass());

    private boolean block = false;

    private long deepLimit = 1000L;

    private Set<Pattern> filterPatterns;

    public BlockDeepPagingInnerInterceptor() {
    }

    public BlockDeepPagingInnerInterceptor(boolean block, long deepLimit) {
        this.block = block;
        this.deepLimit = deepLimit;
    }

    public BlockDeepPagingInnerInterceptor(boolean block, long deepLimit, Set<Pattern> patterns) {
        this.block = block;
        this.deepLimit = deepLimit;
        this.filterPatterns = patterns;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql){
        String originalSql = boundSql.getSql();
        Set<Pattern> patterns = Kit.op(filterPatterns).orElseGet(HashSet::new);
        for (Pattern pattern : patterns) {
            boolean filter = pattern.matcher(originalSql).find();
            if (filter) {
                return;
            }
        }
        String sql = originalSql.substring(originalSql.length() - 20);
        Matcher matcher = LIMIT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String value = matcher.group();
            int start = value.indexOf(" ");
            int end = value.indexOf(",");
            if (end == -1) {
                value = value.substring(start).trim();
            } else {
                value = value.substring(start, end).trim();
            }
            long limit = Long.parseLong(value);
            if (limit >= deepLimit) {
                originalSql = SqlFormatter.of(Dialect.MySql).format(originalSql);
                String message = String.format("fatal error, slow sql possible: \n%s ", originalSql);
                logger.error(message);
                customStrategyWhenFail(originalSql);
                BizException.trueThrow(block, ErrorEnums.DEEP_PAGING.message(message));
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
