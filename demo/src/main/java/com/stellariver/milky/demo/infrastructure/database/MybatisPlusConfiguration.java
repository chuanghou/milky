package com.stellariver.milky.demo.infrastructure.database;

import org.h2.tools.Server;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@MapperScan("com.stellariver.milky.demo.infrastructure.database")
public class MybatisPlusConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws SQLException {
        // 启动一个h2的web server，调试时可以通过localhost:8082访问到h2的内容
        // JDBC url：jdbc:h2:mem:testdb
        // User Name: sa
        // Password: 无
        // 注意如果使用断点，断点类型使用thread，不能使用all，否则webserver 无法访问
        return Server.createWebServer("-web", "-webAllowOthers", "-webDaemon", "-webPort", "8082");
    }

    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder.setType(EmbeddedDatabaseType.H2)
                .addScripts("h2/schema.sql")
                .build();
    }
}
