package com.stellariver.milky.demo.infrastructure.database;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("mybatis.base")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyBatisBaseConfiguration {

    String mapperLocation;

    String typeHandlersPackage;

    String typeAliasesPackage;

    String typeEnumsPackage;

}
