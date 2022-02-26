package com.stellariver.milky.example.domain.configuration;

import com.stellariver.milky.starter.EnableMilky;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMilky(domainPackages = {"com.stellariver.milky.example"})
public class DomainConfiguration {
}
