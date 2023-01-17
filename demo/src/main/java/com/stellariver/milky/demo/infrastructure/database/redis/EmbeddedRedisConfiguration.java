package com.stellariver.milky.demo.infrastructure.database.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import redis.embedded.RedisServer;

@Configuration
@Order(Integer.MIN_VALUE)
public class EmbeddedRedisConfiguration {

    @Bean(destroyMethod = "stop")
    public RedisServer redis(){
        RedisServer redisServer = new RedisServer(6379);
        redisServer.start();
        return redisServer;
    }

}
