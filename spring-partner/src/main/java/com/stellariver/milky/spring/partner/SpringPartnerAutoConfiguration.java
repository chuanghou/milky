package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.*;
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
    @ConditionalOnMissingBean(TraceIdProvider.class)
    public TraceIdProvider traceIdProvider() {
        TraceIdProvider provider = new DefaultTraceIdProvider();
        TraceIdContext.init(provider);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public StaticSupport staticSupport(@Autowired(required = false) MilkyStableSupport milkyStableSupport,
                                       @Autowired(required = false) RunnerExtension runnerExtension,
                                       @Autowired(required = false) TraceIdProvider traceIdProvider,
                                       BeanLoader beanLoader) {
        return new StaticSupport(milkyStableSupport, runnerExtension, traceIdProvider, beanLoader);
    }


    @Bean
    @ConditionalOnMissingBean
    public BeanLoader beanLoader(ApplicationContext applicationContext) {
        return new BeanLoaderImpl(applicationContext);
    }

}
