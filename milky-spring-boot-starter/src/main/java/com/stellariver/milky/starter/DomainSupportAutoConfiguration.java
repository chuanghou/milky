package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.MilkyConfiguration;
import com.stellariver.milky.domain.support.base.MilkySupport;
import com.stellariver.milky.domain.support.base.MilkyScanPackages;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.DependencyPrepares;
import com.stellariver.milky.domain.support.dependency.BeanLoader;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.dependency.DomainRepository;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.util.AsyncExecutorConfiguration;
import com.stellariver.milky.domain.support.util.AsyncExecutor;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.util.ThreadLocalPasser;
import com.stellariver.milky.domain.support.util.BeanUtil;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
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
                                        AsyncExecutor asyncExecutor,
                                        List<DependencyPrepares> dependencyPrepares,
                                        List<Interceptors> interceptors,
                                        List<EventRouters> eventRouters,
                                        List<DomainRepository<?>> domainRepositories,
                                        BeanLoader beanLoader,
                                        MilkyConfiguration milkyConfiguration) {
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(milkyConfiguration.getScanPackages())
                .addScanners(new SubTypesScanner());
        Reflections reflections = new Reflections(configuration);
        return MilkySupport.builder().concurrentOperate(concurrentOperate)
                .traceRepository(traceRepository)
                .asyncExecutor(asyncExecutor)
                .dependencyPrepares(dependencyPrepares)
                .interceptors(interceptors)
                .eventRouters(eventRouters)
                .domainRepositories(domainRepositories)
                .beanLoader(beanLoader)
                .reflections(reflections)
                .build();
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
    public AsyncExecutor asyncExecutorService(List<ThreadLocalPasser<?>> threadLocalPassers, MilkProperties properties) {

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

}
