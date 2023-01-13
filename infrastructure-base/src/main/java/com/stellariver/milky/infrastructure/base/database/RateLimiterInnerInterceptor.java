package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RateLimiterWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 限流插件
 *
 * @author houchuang
 * @since 2022-11-03
 */
@RequiredArgsConstructor
public class RateLimiterInnerInterceptor implements InnerInterceptor {

    final private MilkyStableSupport milkyStableSupport;

    final private Map<Pattern, String> patternMap;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql){
        String sql = boundSql.getSql();
        doRateLimiter(sql);
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        String sql = ms.getBoundSql(parameter).getSql();
        doRateLimiter(sql);
    }

    private void doRateLimiter(String sql) {
        String key = Kit.op(patternMap).orElseGet(HashMap::new).keySet().stream()
                .filter(p -> p.matcher(sql).find()).findFirst().map(patternMap::get).orElse(null);
        if (key != null) {
            RateLimiterWrapper rateLimiterWrapper = milkyStableSupport.rateLimiter(key);
            if (rateLimiterWrapper != null) {
                rateLimiterWrapper.acquire();
            }
        }
    }

}
