package com.stellariver.milky.demo;

import com.stellariver.milky.spring.partner.wire.EnableStaticWire;
import com.stellariver.milky.starter.EnableMilky;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author houchuang
 */
@EnableMilky
@EnableStaticWire
@SpringBootApplication
public class MilkyDemoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MilkyDemoApplication.class, args);
    }

}
