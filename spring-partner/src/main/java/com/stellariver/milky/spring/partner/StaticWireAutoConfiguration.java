package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.wire.StaticWireSupport;
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
@EnableConfigurationProperties(SpringPartnerProperties.class)
public class StaticWireAutoConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApplicationRunner staticWireRunner(StaticWireScanPackages staticWireScanPackages,
                                              SpringPartnerProperties springPartnerProperties) {
        List<String> packages = new ArrayList<>(Arrays.asList(staticWireScanPackages.getScanPackages()));
        String[] scanPackages = springPartnerProperties.getScanPackages();
        if (scanPackages != null) {
            packages.addAll(Arrays.asList(scanPackages));
        }
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(packages.toArray(new String[0])).addScanners(new FieldAnnotationsScanner());
        Reflections reflections = new Reflections(configuration);
        return args -> StaticWireSupport.wire(reflections);
    }

}
