package com.stellariver.milky.frodo.starter;

import com.stellariver.milky.frodo.core.JettyServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(FrodoProperties.class)
public class FrodoAutoConfiguration {

    @Bean
    public JettyServer jettyServer(FrodoProperties frodoProperties) {
        return new JettyServer(frodoProperties.getPort());
    }

}
