package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.TraceIdGetter;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.util.RunnerExtension;
import com.stellariver.milky.spring.partner.BeanLoaderImpl;
import com.stellariver.milky.spring.partner.StaticSupport;
import com.stellariver.milky.spring.partner.wire.EnableStaticWire;
import com.stellariver.milky.starter.EnableMilky;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * @author houchuang
 */
@EnableMilky
@EnableStaticWire
@EnableCaching
@SpringBootApplication
public class MilkyDemoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MilkyDemoApplication.class, args);
    }

    @Bean
    public BeanLoader beanLoader(ApplicationContext applicationContext) {
        return new BeanLoaderImpl(applicationContext);
    }


    @Bean
    public StaticSupport staticSupport(@Autowired(required = false) MilkyStableSupport milkyStableSupport,
                                       @Autowired(required = false) RunnerExtension runnerExtension,
                                       @Autowired(required = false) TraceIdGetter traceIdGetter,
                                       BeanLoader beanLoader) {
        Optional.ofNullable(milkyStableSupport).ifPresent(Runner::setMilkyStableSupport);
        Optional.ofNullable(runnerExtension).ifPresent(Runner::setFailureExtendable);
        Optional.ofNullable(traceIdGetter).ifPresent(Result::initTraceIdGetter);
        return new StaticSupport(milkyStableSupport, runnerExtension, traceIdGetter, beanLoader);
    }

}
