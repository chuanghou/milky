package com.stellariver.milky.demo.infrastructure.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

//@Configuration
public class RedisConfiguration {

    /**
     * 初始化连接超时时间
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    /**
     * 查询超时时间
     */
    private static final int DEFAULT_SO_TIMEOUT = 2000;

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password}")
    private String password;

    //    @Bean(destroyMethod = "close")
    public JedisPool jedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(32);
        config.setMaxIdle(32);
        config.setMinIdle(20);
        return new JedisPool(config, host, port,
                DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT, password, 0, null);
    }

    //    @Bean
    public RedisHelper redisHelper(JedisPool jedisPool) {
        return new RedisHelper(jedisPool);
    }

}
