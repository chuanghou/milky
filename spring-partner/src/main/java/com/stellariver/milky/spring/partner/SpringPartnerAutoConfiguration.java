package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.TraceIdGetter;
import com.stellariver.milky.common.tool.common.BeanLoader;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.util.RunnerExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

public class SpringPartnerAutoConfiguration {

    static class DummyStaticSupport {}

    @Bean
    public DummyStaticSupport dummyStaticSupport(@Autowired(required = false)
                                                 MilkyStableSupport milkyStableSupport,
                                                 @Autowired(required = false)
                                                 RunnerExtension runnerExtension,
                                                 @Autowired(required = false)
                                                 TraceIdGetter traceIdGetter,
                                                 BeanLoader beanLoader) {
        Optional.ofNullable(milkyStableSupport).ifPresent(Runner::setMilkyStableSupport);
        Optional.ofNullable(runnerExtension).ifPresent(Runner::setFailureExtendable);
        Optional.ofNullable(traceIdGetter).ifPresent(Result::setTraceIdGetter);
        BeanUtil.setBeanLoader(beanLoader);
        return new DummyStaticSupport();
    }

}
