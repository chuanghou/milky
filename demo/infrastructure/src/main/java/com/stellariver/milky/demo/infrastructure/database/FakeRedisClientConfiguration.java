package com.stellariver.milky.demo.infrastructure.database;

import com.stellariver.milky.spring.partner.LocalCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(LocalCondition.class)
public class FakeRedisClientConfiguration {

    @Bean
    public FakeRedisClient fakeRedisClient() throws InterruptedException {
        Thread.sleep(10_000L);
        return new FakeRedisClient();
    }

}
