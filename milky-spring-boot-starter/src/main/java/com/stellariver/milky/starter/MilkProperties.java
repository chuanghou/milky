package com.stellariver.milky.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author houchuang
 */
@Data
@ConfigurationProperties("milky")
public class MilkProperties {

    boolean enableMq = false;

    int corePoolSize = 20;

    int maximumPoolSize = 50;

    int keepAliveTimeMinutes = 3;

    int blockingQueueCapacity = 500;

    String traceIdKey = "traceId";

    String[] scanPackages;


}
