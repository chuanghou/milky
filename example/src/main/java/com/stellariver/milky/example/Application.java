package com.stellariver.milky.example;

import com.stellariver.milky.starter.EnableMilky;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMilky(domainPackages = {"com.stellariver.milky.example.domain"})
@MapperScan("com.stellariver.milky.example.domain")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
