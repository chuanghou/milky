package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.MilkyConfiguration;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.base.MilkyScanPackages;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.BeanLoader;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.dependency.MilkyRepository;
import com.stellariver.milky.domain.support.util.AsyncExecutorConfiguration;
import com.stellariver.milky.domain.support.util.AsyncExecutorService;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.util.ThreadLocalPasser;
import com.stellariver.milky.domain.support.util.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.*;

@EnableConfigurationProperties(MilkProperties.class)
public class DomainSupportAutoConfiguration {

    private static final Logger log = Logger.getLogger(DomainSupportAutoConfiguration.class);

    @Bean
    public MilkyConfiguration milkyConfiguration(MilkyScanPackages milkyScanPackages, MilkProperties milkProperties) {
        return new MilkyConfiguration(milkProperties.enableMq, milkyScanPackages.getScanPackages());
    }

    @Bean
    public MilkySupport milkySupport(ConcurrentOperate concurrentOperate, EventBus eventBus,
                                     MilkyRepository milkyRepository, AsyncExecutorService asyncExecutorService) {
        return new MilkySupport(concurrentOperate, eventBus, milkyRepository, asyncExecutorService);
    }


    @Bean
    public CommandBus commandBus(MilkySupport milkySupport, MilkyConfiguration milkyConfiguration) {
        return CommandBus.builder().milkySupport(milkySupport)
                .configuration(milkyConfiguration)
                .init();
    }

    @Bean
    public EventBus eventBus(BeanLoader beanLoader, AsyncExecutorService asyncExecutorService) {
        return new EventBus(beanLoader, asyncExecutorService);
    }

    @Bean
    BeanLoader beanLoader(ApplicationContext applicationContext) {
        BeanLoaderImpl beanLoader = new BeanLoaderImpl(applicationContext);
        BeanUtil.setBeanLoader(beanLoader);
        return beanLoader;
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutorService asyncExecutorService(List<ThreadLocalPasser<?>> threadLocalPassers, MilkProperties properties) {

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> log.with("threadName", t.getName()).error(e.getMessage(), e))
                .setNameFormat("async-thread-%d")
                .build();

        AsyncExecutorConfiguration configuration = AsyncExecutorConfiguration.builder()
                .corePoolSize(properties.getCorePoolSize())
                .maximumPoolSize(properties.getMaximumPoolSize())
                .keepAliveTimeMinutes(properties.getKeepAliveTimeMinutes())
                .blockingQueueCapacity(properties.getBlockingQueueCapacity())
                .build();

        return new AsyncExecutorService(configuration, threadFactory, threadLocalPassers);
    }

}
