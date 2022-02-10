package com.echobaba.milky.domain.support;

import com.echobaba.milky.domain.support.command.CommandBus;
import com.echobaba.milky.domain.support.event.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


}
