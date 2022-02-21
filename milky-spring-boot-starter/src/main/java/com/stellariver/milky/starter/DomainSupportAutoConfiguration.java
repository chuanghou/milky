package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.common.ErrorCodeBase;
import com.stellariver.milky.common.tool.log.Log;
import com.stellariver.milky.domain.support.base.DomainPackages;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.util.BeanUtils;
import com.stellariver.milky.spring.partner.SpringBeanLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.*;

@Slf4j
@EnableConfigurationProperties(MilkyProperties.class)
public class DomainSupportAutoConfiguration {

    @Bean
    public CommandBus commandBus(BeanLoader beanLoader, ConcurrentOperate concurrentOperate,
                                 EventBus eventBus, DomainPackages domainPackages, MilkyProperties milkyProperties) {
        boolean enableMq = milkyProperties.isEnableMq();
        return new CommandBus(beanLoader, concurrentOperate, eventBus, domainPackages, enableMq);
    }

    @Bean
    public EventBus eventBus(BeanLoader beanLoader) {
        return new EventBus(beanLoader);
    }

    @Bean
    @ConditionalOnMissingBean(name = "beanLoader")
    BeanLoader beanLoader(SpringBeanLoader springBeanLoader) {
        BeanLoaderImpl beanLoader = new BeanLoaderImpl(springBeanLoader);
        BeanUtils.setBeanLoader(beanLoader);
        return beanLoader;
    }

    @Bean
    SpringBeanLoader springBeanLoader(ApplicationContext applicationContext) {
        return new SpringBeanLoader(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) ->
                        Log.of(() -> log.error("|线程名={}|错误信息={}|", t.getName(), e.getMessage(), e)).log(ErrorCodeBase.UNKNOWN))
                .setNameFormat("event-handler-url-thread-%d")
                .build();

        return new ThreadPoolExecutor(10, 20, 5,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(500),
                threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
