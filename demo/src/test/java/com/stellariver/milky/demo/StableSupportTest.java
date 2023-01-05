package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.common.Option;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.StableConfig;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.demo.basic.ErrorEnums;
import com.stellariver.milky.demo.basic.UKs;
import com.stellariver.milky.common.tool.stable.CbConfig;
import com.stellariver.milky.demo.infrastructure.nacos.stable.FakeConfigCenterListener;
import com.stellariver.milky.common.tool.stable.RlConfig;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

@CustomLog
@SpringBootTest
public class StableSupportTest {

    @Autowired
    FakeConfigCenterListener fakeConfigCenterListener;

    @Autowired
    AbstractStableSupport abstractStableSupport;

    @Test
    @SneakyThrows
    public void test() {


        RlConfig rlConfig = RlConfig.builder().key(UKs.stableTest.getKey()).qps(10.0).build();
        CbConfig cbConfig = CbConfig.builder().key(UKs.stableTest.getKey())
                .minimumNumberOfCalls(10)
                .slidingWindowSize(15)
                .waitIntervalInOpenState(Duration.ofSeconds(2))
                .build();
        StableConfig stableConfig = StableConfig.builder().rlConfigs(Collect.asList(rlConfig))
                .cbConfigs(Collect.asList(cbConfig)).build();
        fakeConfigCenterListener.receiveMessage(Json.toJson(stableConfig));

        Option<Result<String>, String> option = Option.<Result<String>, String>builder().check(Result::isSuccess)
                .lambdaId(UKs.stableTest)
                .transfer(Result::getData)
                .build();
        long now = Clock.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            Runner.checkout(option, () -> stableTest(0));
        }
        long cost = Clock.currentTimeMillis() - now;
        Assertions.assertTrue((cost > 1850) && (cost < 2050));

        Thread.sleep(1000);
        now = Clock.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            Runner.checkout(option, () -> stableTest(0));
        }
        cost = Clock.currentTimeMillis() - now;
        Assertions.assertTrue((cost > 850) && (cost < 1050));

        int count = 0;

        for (int i = 0; i < 20; i++) {
            int finalI = i;
            try {
                Runner.checkout(option, () -> stableTest(finalI));
            } catch (CallNotPermittedException ex) {
                count++;
            } catch (Throwable ignore) {
            }
        }
        Assertions.assertEquals(count, 11);
        CircuitBreaker circuitBreaker = abstractStableSupport.circuitBreaker(UKs.stableTest.getKey());
        Assertions.assertNotNull(circuitBreaker);
        Assertions.assertEquals(circuitBreaker.getState(), CircuitBreaker.State.OPEN);
        Thread.sleep(2000);
        Assertions.assertEquals(circuitBreaker.getState(), CircuitBreaker.State.HALF_OPEN);
        for (int i = 0; i < 9; i++) {
            try {
                stableTest(1);
                Runner.checkout(option, () -> stableTest(0));
            } catch (Throwable ignore) {
            }
        }
        Assertions.assertEquals(circuitBreaker.getState(), CircuitBreaker.State.HALF_OPEN);
        for (int i = 0; i < 9; i++) {
            try {
                Runner.checkout(option, () -> stableTest(0));
            } catch (Throwable ignore) {
            }
        }
        Assertions.assertEquals(circuitBreaker.getState(), CircuitBreaker.State.CLOSED);


    }


    static private Result<String> stableTest(int i) {
        if (i > 0) {
            return Result.error(ErrorEnums.SYSTEM_EXCEPTION);
        }
        return Result.success();
    }
}
