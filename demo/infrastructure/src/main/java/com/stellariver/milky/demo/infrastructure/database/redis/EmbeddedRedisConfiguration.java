package com.stellariver.milky.demo.infrastructure.database.redis;

import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.spring.partner.NotWindowsCondition;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import redis.embedded.RedisServer;

@Configuration
@Conditional(NotWindowsCondition.class)
public class EmbeddedRedisConfiguration {

    @Order(Integer.MIN_VALUE)
    @Bean(destroyMethod = "stop")
    public RedisServer redis(){
        RedisServer redisServer = new RedisServer(6379);
        redisServer.start();
        return redisServer;
    }


    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ConcurrentOperate concurrentOperate(RedissonClient redissonClient) {
        return new RedissionConcurrentOperateImpl(redissonClient);
    }

}
