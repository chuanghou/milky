package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.Option;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.stable.RlConfig;
import com.stellariver.milky.common.tool.stable.StableConfig;
import com.stellariver.milky.common.tool.stable.StableConfigReader;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.basic.UKs;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.function.Function;

@CustomLog
@SpringBootTest
@DirtiesContext
@Import(SqlRateLimiterTest.TestConfig.class)
public class SqlRateLimiterTest {

    @Autowired
    UniqueIdGetter uniqueIdGetter;

    static class TestConfig {

        @Bean
        public MilkyStableSupport milkyStableSupport() {
            RlConfig rlConfig = RlConfig.builder().ruleId(UKs.sqlRateLimiter.getKey()).qps(10.0).build();
            StableConfig stableConfig = StableConfig.builder()
                    .rlConfigs(Collect.toMap(Collect.asList(rlConfig), RlConfig::getRuleId))
                    .build();
            StableConfigReader stableConfigReader = () -> stableConfig;
            return new MilkyStableSupport(stableConfigReader);
        }

    }

    @Test
    @SneakyThrows
    public void test() {

        Option<Long, Long> option = Option.<Long, Long>builder()
                .transfer(Function.identity())
                .build();
        long now = Clock.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Runner.checkout(option, () -> uniqueIdGetter.get());
        }
        long cost = Clock.currentTimeMillis() - now;
        Assertions.assertTrue((cost > 1800) && (cost < 2100));

    }

}
