package com.stellariver.milky.example.infrastructure.mapper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.stellariver.milky.example")
public class MybatisConfiguration {
}
