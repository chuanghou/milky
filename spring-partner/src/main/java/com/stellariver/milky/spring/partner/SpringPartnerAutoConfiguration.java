package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.base.TraceIdGetter;
import com.stellariver.milky.common.tool.common.BeanLoader;
import com.stellariver.milky.common.tool.common.BeanUtil;
import com.stellariver.milky.common.tool.common.Runner;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.common.tool.util.RunnerExtension;
import com.stellariver.milky.common.tool.wire.StaticWireSupport;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
public class SpringPartnerAutoConfiguration {

    @Bean
    BeanLoader beanLoader(ApplicationContext applicationContext) {
        return new BeanLoaderImpl(applicationContext);
    }


    @Bean
    public StaticSupport staticSupport(@Autowired(required = false) MilkyStableSupport milkyStableSupport,
                                       @Autowired(required = false) RunnerExtension runnerExtension,
                                       @Autowired(required = false) TraceIdGetter traceIdGetter,
                                       BeanLoader beanLoader) {
        Optional.ofNullable(milkyStableSupport).ifPresent(Runner::setMilkyStableSupport);
        Optional.ofNullable(runnerExtension).ifPresent(Runner::setFailureExtendable);
        Optional.ofNullable(traceIdGetter).ifPresent(Result::setTraceIdGetter);
        BeanUtil.setBeanLoader(beanLoader);
        return new StaticSupport();
    }

    static class StaticWireCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            AnnotationAttributes enableSpringPartner = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableSpringPartner.class.getName()));
            boolean springPartnerScanPackages = context.getRegistry().containsBeanDefinition("springPartnerScanPackages");
            return (enableSpringPartner != null) || springPartnerScanPackages ;
        }

    }


    @Bean
    @Conditional(StaticWireCondition.class)
    public ApplicationRunner staticWireRunner(@Autowired(required = false) SpringPartnerScanPackages springPartnerScanPackages,
                                              @Autowired(required = false) SpringPartnerProperties springPartnerProperties) {
        List<String> packages = new ArrayList<>();
        if (springPartnerProperties != null) {
            packages.addAll(Arrays.asList(springPartnerProperties.getScanPackages()));
        }
        if (springPartnerScanPackages != null) {
            packages.addAll(Arrays.asList(springPartnerScanPackages.getScanPackages()));
        }
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .forPackages(packages.toArray(new String[0])).addScanners(new FieldAnnotationsScanner());
        Reflections reflections = new Reflections(configuration);
        return args -> StaticWireSupport.wire(reflections);
    }


}
