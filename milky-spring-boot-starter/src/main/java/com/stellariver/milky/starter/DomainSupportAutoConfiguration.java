package com.stellariver.milky.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.ScanPackages;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import com.stellariver.milky.domain.support.depend.MessageRepository;
import com.stellariver.milky.domain.support.event.EventBus;
import com.stellariver.milky.domain.support.util.BeanUtils;
import com.stellariver.milky.spring.partner.SpringBeanLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.*;

@EnableConfigurationProperties(MilkyProperties.class)
public class DomainSupportAutoConfiguration {

    private static final Logger log = Logger.getLogger(DomainSupportAutoConfiguration.class);

    @Bean
    public CommandBus commandBus(BeanLoader beanLoader, ConcurrentOperate concurrentOperate,
                                 EventBus eventBus, ScanPackages scanPackages, MilkyProperties milkyProperties,
                                 MessageRepository messageRepository) {
        boolean enableMq = milkyProperties.isEnableMq();
        return new CommandBus(beanLoader, concurrentOperate,
                eventBus, scanPackages.getPackages(), enableMq, messageRepository);
    }

    @Bean
    public EventBus eventBus(BeanLoader beanLoader, ExecutorService asyncExecutorService) {
        return new EventBus(beanLoader, asyncExecutorService);
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
    @ConditionalOnMissingBean(name = "asyncExecutorService")
    public ExecutorService asyncExecutorService() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> log.error("|线程名={}|错误信息={}|", t.getName(), e.getMessage(), e))
                .setNameFormat("event-handler-url-thread-%d")
                .build();

        return new ThreadPoolExecutor(10, 20, 5,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(500),
                threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
