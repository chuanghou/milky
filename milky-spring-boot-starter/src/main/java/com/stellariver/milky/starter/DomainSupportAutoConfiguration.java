package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.MilkyConfiguration;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.base.MilkyScanPackages;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.DependencyPrepares;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.util.AsyncExecutorConfiguration;
import com.stellariver.milky.domain.support.util.AsyncExecutor;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.util.ThreadLocalPasser;
import com.stellariver.milky.domain.support.util.BeanUtil;
import com.stellariver.milky.spring.partner.BeanLoaderImpl;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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
    public MilkySupport milkySupport(ConcurrentOperate concurrentOperate,
                                     TraceRepository traceRepository,
                                     @Autowired(required = false)
                                     TransactionSupport transactionSupport,
                                     AsyncExecutor asyncExecutor,
                                     @Autowired(required = false)
                                     List<DependencyPrepares> dependencyPrepares,
                                     @Autowired(required = false)
                                     List<Interceptors> interceptors,
                                     @Autowired(required = false)
                                     List<EventRouters> eventRouters,
                                     @Autowired(required = false)
                                     List<AggregateDaoAdapter<?>> daoAdapters,
                                     @Autowired(required = false)
                                     List<DAOWrapper<?, ?>> daoWrappers,
                                     BeanLoader beanLoader,
                                     MilkyConfiguration milkyConfiguration) {
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(milkyConfiguration.getScanPackages())
                .addScanners(new SubTypesScanner());
        Reflections reflections = new Reflections(configuration);
        return new MilkySupport(concurrentOperate,
                traceRepository,
                asyncExecutor,
                dependencyPrepares,
                interceptors,
                eventRouters,
                daoAdapters,
                daoWrappers,
                reflections,
                beanLoader,
                transactionSupport);
    }


    @Bean
    public CommandBus commandBus(MilkySupport milkySupport, EventBus eventBus, MilkyConfiguration milkyConfiguration) {
        return new CommandBus(milkySupport, eventBus, milkyConfiguration);
    }

    @Bean
    public EventBus eventBus(MilkySupport milkySupport) {
        return new EventBus(milkySupport);
    }

    @Bean
    BeanLoader beanLoader(ApplicationContext applicationContext) {
        BeanLoaderImpl beanLoader = new BeanLoaderImpl(applicationContext);
        BeanUtil.setBeanLoader(beanLoader);
        return beanLoader;
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncExecutor asyncExecutor(@Autowired(required = false) List<ThreadLocalPasser<?>> threadLocalPassers, MilkProperties properties) {
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

        return new AsyncExecutor(configuration, threadFactory, threadLocalPassers);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceRepository traceRepository() {
        return new TraceRepository() {
            @Override
            public void record(Context context, boolean success) {

            }
        };
    }

}
