package com.echobaba.milky.common.tool;

import com.echobaba.milky.common.tool.cache.ThreadLocalCacheSupport;
import com.echobaba.milky.common.tool.common.BeanLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonToolAutoConfiguration {

    @Bean
    public BeanLoader beanLoader() {
        return new BeanLoader();
    }

    @Bean
    public ThreadLocalCacheSupport threadLocalCacheSupport() {
        return new ThreadLocalCacheSupport();
    }

}
