package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.MilkyConfiguration;
import com.stellariver.milky.domain.support.base.MilkyRepositories;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.base.MilkyScanPackages;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.event.AsyncExecutorConfiguration;
import com.stellariver.milky.domain.support.event.AsyncExecutorService;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.event.ThreadLocalPasser;
import com.stellariver.milky.domain.support.util.BeanUtils;
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
    public CommandBus commandBus(MilkySupport support, MilkyRepositories repositories, MilkyConfiguration configuration) {
        return CommandBus.builder().milkySupport(support)
                .repositories(repositories).configuration(configuration).init();
    }

    @Bean
    public EventBus eventBus(BeanLoader beanLoader, AsyncExecutorService asyncExecutorService) {
        return new EventBus(beanLoader, asyncExecutorService);
    }

    @Bean
    BeanLoader beanLoader(ApplicationContext applicationContext) {
        BeanLoaderImpl beanLoader = new BeanLoaderImpl(applicationContext);
        BeanUtils.setBeanLoader(beanLoader);
        return beanLoader;
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutorService asyncExecutorService(List<ThreadLocalPasser<?>> threadLocalPassers, MilkProperties properties) {

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> log.with("threadName", t.getName()).error("", e))
                .setNameFormat("async-event-handler-url-thread-%d")
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
