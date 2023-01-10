package com.stellariver.milky.demo.infrastructure.database;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * @author houchuang
 */
@Configuration
public class H2Configuration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws SQLException {
        // 启动一个h2的web server，调试时可以通过localhost:8082访问到h2的内容
        // JDBC url：jdbc:h2:mem:testdb
        // User Name: sa
        // Password: 无
        // 注意如果使用断点，断点类型使用thread，不能使用all，否则webserver 无法访问
        return Server.createWebServer("-web", "-webAllowOthers", "-webDaemon", "-webPort", "8082");
    }

}