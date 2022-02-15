package com.stellariver.milky.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "milky")
public class MilkyProperties {

    private String domainPackage;

}