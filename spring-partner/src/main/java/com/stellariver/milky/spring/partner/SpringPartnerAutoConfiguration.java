package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.TraceIdContext;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.util.RunnerExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Optional;


public class SpringPartnerAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public StaticSupport staticSupport(@Autowired(required = false) MilkyStableSupport milkyStableSupport,
                                       @Autowired(required = false) RunnerExtension runnerExtension,
                                       @Autowired(required = false) TraceIdContext traceIdContext,
                                       BeanLoader beanLoader) {
        Optional.ofNullable(milkyStableSupport).ifPresent(Runner::setMilkyStableSupport);
        Optional.ofNullable(runnerExtension).ifPresent(Runner::setFailureExtendable);
        Optional.ofNullable(traceIdContext).ifPresent(Result::initTraceIdContext);
        return new StaticSupport(milkyStableSupport, runnerExtension, traceIdContext, beanLoader);
    }


    @Bean
    @ConditionalOnMissingBean
    public BeanLoader beanLoader(ApplicationContext applicationContext) {
        return new BeanLoaderImpl(applicationContext);
    }

}
