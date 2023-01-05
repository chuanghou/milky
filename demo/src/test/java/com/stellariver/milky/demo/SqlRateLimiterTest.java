package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.Option;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.RlConfig;
import com.stellariver.milky.common.tool.stable.StableConfig;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.demo.basic.UKs;
import com.stellariver.milky.demo.infrastructure.nacos.stable.FakeConfigCenterListener;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;

@CustomLog
@SpringBootTest
public class SqlRateLimiterTest {

    @Autowired
    FakeConfigCenterListener fakeConfigCenterListener;

    @Autowired
    AbstractStableSupport abstractStableSupport;

    @Autowired
    IdBuilder idBuilder;

    @Test
    @SneakyThrows
    public void test() {

        RlConfig rlConfig = RlConfig.builder().key(UKs.sqlRateLimiter.getKey()).qps(10.0).build();
        StableConfig stableConfig = StableConfig.builder().rlConfigs(Collect.asList(rlConfig)).build();
        fakeConfigCenterListener.receiveMessage(Json.toJson(stableConfig));

        Option<Long, Long> option = Option.<Long, Long>builder()
                .transfer(Function.identity())
                .lambdaId(UKs.sqlRateLimiter)
                .build();
        long now = Clock.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            Runner.checkout(option, () -> idBuilder.build());
        }
        long cost = Clock.currentTimeMillis() - now;
        Assertions.assertTrue((cost > 1850) && (cost < 2050));


    }

}
