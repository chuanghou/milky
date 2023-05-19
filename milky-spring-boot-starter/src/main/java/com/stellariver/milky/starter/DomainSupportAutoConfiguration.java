package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.common.BeanLoader;
import com.stellariver.milky.domain.support.base.MilkyScanPackages;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.util.AsyncExecutorConfiguration;
import com.stellariver.milky.domain.support.util.ThreadLocalPasser;
import com.stellariver.milky.domain.support.util.ThreadLocalTransferableExecutor;
import lombok.CustomLog;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * @author houchuang
 */
@CustomLog
@EnableConfigurationProperties(MilkProperties.class)
public class DomainSupportAutoConfiguration {

    @Bean
    @SuppressWarnings("all")
    public TransactionSupport transactionSupport(DataSourceTransactionManager dataSourceTransactionManager) {
        return new TransactionSupportImpl(dataSourceTransactionManager);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MilkySupport milkySupport(ConcurrentOperate concurrentOperate,
                                     TraceRepository traceRepository,
                                     @Autowired(required = false)
                                     TransactionSupport transactionSupport,
                                     ThreadLocalTransferableExecutor threadLocalTransferableExecutor,
                                     @Autowired(required = false)
                                     List<Interceptors> interceptors,
                                     @Autowired(required = false)
                                     List<EventRouters> eventRouters,
                                     @Autowired(required = false)
                                     List<DaoAdapter<?>> daoAdapters,
                                     @Autowired(required = false)
                                     List<DAOWrapper<?, ?>> daoWrappers,
                                     BeanLoader beanLoader,
                                     MilkyScanPackages milkyScanPackages,
                                     MilkProperties milkProperties) {
        String[] scanPackages = milkProperties.getScanPackages();
        if (scanPackages == null) {
            scanPackages = milkyScanPackages.getScanPackages();
        }
        ConfigurationBuilder configuration = new ConfigurationBuilder().forPackages(scanPackages).addScanners(new SubTypesScanner());
        Reflections reflections = new Reflections(configuration);
        return new MilkySupport(concurrentOperate,
                traceRepository,
                threadLocalTransferableExecutor,
                interceptors,
                eventRouters,
                daoAdapters,
                daoWrappers,
                reflections,
                beanLoader,
                transactionSupport);
    }

    @Bean
    public CommandBus commandBus(MilkySupport milkySupport, EventBus eventBus) {
        return new CommandBus(milkySupport, eventBus);
    }

    @Bean
    public EventBus eventBus(MilkySupport milkySupport) {
        return new EventBus(milkySupport);
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadLocalTransferableExecutor asyncExecutor(@Autowired(required = false) List<ThreadLocalPasser<?>> threadLocalPassers, MilkProperties properties) {
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

        return new ThreadLocalTransferableExecutor(configuration, threadFactory, threadLocalPassers);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceRepository traceRepository() {
        return (context, success) -> {};
    }



    @Bean
    public ApplicationRunner milkyApplicationRunner() {
        return args -> CommandBus.wire();
    }

}
