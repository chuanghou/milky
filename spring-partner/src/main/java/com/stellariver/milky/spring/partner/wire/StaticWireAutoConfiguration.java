package com.stellariver.milky.spring.partner.wire;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties(StaticWireProperties.class)
public class StaticWireAutoConfiguration {

    @Bean
    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
    public ApplicationRunner staticWireRunner(StaticWireScanPackages staticWireScanPackages,
                                              StaticWireProperties staticWireProperties) {
        List<String> packages = new ArrayList<>(Arrays.asList(staticWireScanPackages.getScanPackages()));
        String[] scanPackages = staticWireProperties.getScanPackages();
        if (scanPackages != null) {
            packages.addAll(Arrays.asList(scanPackages));
        }
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(packages.toArray(new String[0])).addScanners(new FieldAnnotationsScanner());
        Reflections reflections = new Reflections(configuration);
        return new StaticWireApplicationRunner(reflections);
    }

}
