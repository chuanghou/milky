package com.stellariver.milky.example.starter;

import com.stellariver.milky.starter.EnableMilky;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.stellariver.milky.example")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
