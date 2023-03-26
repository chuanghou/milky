package com.stellariver.milky.starter.demo.starter;

import com.stellariver.milky.starter.EnableMilky;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author houchuang
 */
@EnableMilky(scanPackages = "com.stellariver.milky.demo")
@SpringBootApplication(scanBasePackages = "com.stellariver.milky.demo")
public class MilkyDemoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MilkyDemoApplication.class, args);
    }

}
