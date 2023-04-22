package com.stellariver.milky.spring.partner;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("static.wire")
public class StaticWireProperties {

    String[] scanPackages;

}
