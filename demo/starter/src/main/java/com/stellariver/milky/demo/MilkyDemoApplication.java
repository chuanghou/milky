package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.BeanLoader;
import com.stellariver.milky.spring.partner.BeanLoaderImpl;
import com.stellariver.milky.spring.partner.wire.EnableStaticWire;
import com.stellariver.milky.starter.EnableMilky;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author houchuang
 */
@EnableMilky
@EnableStaticWire
@EnableCaching
@SpringBootApplication
public class MilkyDemoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MilkyDemoApplication.class, args);
    }

}
