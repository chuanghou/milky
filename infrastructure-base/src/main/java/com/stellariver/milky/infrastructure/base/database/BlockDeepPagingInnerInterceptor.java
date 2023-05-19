package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.tool.common.Kit;
import lombok.CustomLog;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stellariver.milky.common.base.ErrorEnumsBase.DEEP_PAGING;

/**
 * 防止深度分页插件，默认不阻断，分页限制1000条
 *
 * @author houchuang
 * @since 2022-11-03
 * TODO 因为mybatis是预编译的方式，所以其实拿不到完整sql，所以这事还有点难搞，后面想想办法吧
 */

@CustomLog
@Deprecated
public class BlockDeepPagingInnerInterceptor implements InnerInterceptor {

    static private final Pattern LIMIT_PATTERN = Pattern.compile("limit\\s+[0-9]+(((\\s+(offset))|,)\\s+[0-9]+)?$");
    static private final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

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
    @SuppressWarnings("unused")
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
        String sql = originalSql.substring(originalSql.length() - 30).toLowerCase();
        Matcher matcher = LIMIT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String value = matcher.group();
            matcher = NUMBER_PATTERN.matcher(value);
            boolean b = matcher.find();
            long max = Long.parseLong(matcher.group());
            if (matcher.find()) {
                max = Math.max(max, Long.parseLong(matcher.group()));
            }
            if (max >= deepLimit) {
                originalSql = SqlFormatter.of(Dialect.MySql).format(originalSql);
                String message = String.format("fatal error, slow sql possible: \n%s ", originalSql);
                log.arg0(message).error(DEEP_PAGING.getCode());
                customStrategyWhenFail(originalSql);
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
