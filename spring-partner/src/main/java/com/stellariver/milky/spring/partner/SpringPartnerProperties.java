package com.stellariver.milky.spring.partner;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.partner")
public class SpringPartnerProperties {

    String[] scanPackages;

}
