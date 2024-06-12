package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.common.base.TraceIdGetter;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.executor.EnhancedExecutor;
import com.stellariver.milky.common.tool.executor.EnhancedExecutorConfiguration;
import com.stellariver.milky.common.tool.executor.ThreadLocalPasser;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.domain.support.base.*;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * @author houchuang
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MilkProperties.class)
public class DomainSupportAutoConfiguration {

    @Bean
    public TransactionSupport transactionSupport(DataSourceTransactionManager dataSourceTransactionManager) {
        return new TransactionSupportImpl(dataSourceTransactionManager);
    }

    @Bean
    public DomainTunnel domainTunnel() {
        return new DomainTunnelImpl();
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
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MilkySupport milkySupport(ConcurrentOperate concurrentOperate,
                                     MilkyTraceRepository milkyTraceRepository,
                                     @Autowired(required = false)
                                     TransactionSupport transactionSupport,
                                     EnhancedExecutor enhancedExecutor,
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
        ConfigurationBuilder configuration = new ConfigurationBuilder().forPackages(scanPackages)
                .addScanners(new SubTypesScanner()).filterInputsBy(new FilterBuilder().include(".*class"));
        Reflections reflections = new Reflections(configuration);
        return new MilkySupport(concurrentOperate,
                milkyTraceRepository,
                enhancedExecutor,
                interceptors,
                eventRouters,
                daoAdapters,
                daoWrappers,
                reflections,
                beanLoader,
                transactionSupport);
    }


    @Bean
    @ConditionalOnMissingBean
    public ConcurrentOperate concurrentOperate() {
        return new ConcurrentOperateImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public EnhancedExecutor enhancedExecutor(MilkProperties properties,
                                             @Autowired(required = false) List<ThreadLocalPasser<?>> threadLocalPassers) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> log.error("uncaught exception from executor", e))
                .setNameFormat("async-thread-%d")
                .build();

        EnhancedExecutorConfiguration configuration = EnhancedExecutorConfiguration.builder()
                .corePoolSize(properties.getCorePoolSize())
                .maximumPoolSize(properties.getMaximumPoolSize())
                .keepAliveTimeMinutes(properties.getKeepAliveTimeMinutes())
                .blockingQueueCapacity(properties.getBlockingQueueCapacity())
                .build();

        return new EnhancedExecutor(configuration, threadFactory, threadLocalPassers);
    }


    @Bean
    @ConditionalOnMissingBean
    public TraceIdGetter traceIdGetter(MilkProperties milkProperties) {
        return () -> MDC.get(milkProperties.getTraceIdKey());
    }

    @Bean
    @ConditionalOnMissingBean
    public MilkyTraceRepository milkyTraceRepository(TraceIdGetter traceIdGetter) {
        return (context, success) -> {
            Long invocationId = context.getInvocationId();
            String traceId = traceIdGetter.getTraceId();
            List<Trail> treeTrails = context.getTreeTrails();
            Map<Class<? extends Typed<?>>, Object> metaData = context.getMetaData();
            log.info(Json.toJson(context.getTreeTrails()));
        };
    }



}
