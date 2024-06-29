package com.stellariver.milky.demo.infrastructure.database;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "fake.redis.client", name = "enable")
public class FakeRedisClientConfiguration {

    @Bean
    public FakeRedisClient fakeRedisClient() throws InterruptedException {
        Thread.sleep(10_000L);
        return new FakeRedisClient();
    }

}
