package com.stellariver.milky.demo;

import com.stellariver.milky.starter.EnableMilky;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMilky("com.stellariver.milky.demo")
@SpringBootApplication
public class MilkyDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilkyDemoApplication.class, args);
    }
}
