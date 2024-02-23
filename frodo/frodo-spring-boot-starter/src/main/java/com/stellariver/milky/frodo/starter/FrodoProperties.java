package com.stellariver.milky.frodo.starter;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "frodo")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FrodoProperties {

    Integer port = 24113;

}
