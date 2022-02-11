package com.stellariver.milky.domain.support;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stellariver.milky.common.tool.common.ErrorCodeBase;
import com.stellariver.milky.common.tool.log.Log;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.event.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Slf4j
@Configuration
public class DomainSupportAutoConfiguration {

    @Bean
    public CommandBus commandBus() {
        return new CommandBus();
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public Executor defaultExecutor() {
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
