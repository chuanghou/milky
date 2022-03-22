package com.stellariver.milky.starter;

import com.stellariver.milky.domain.support.base.ScanPackages;
import com.stellariver.milky.domain.support.context.DependencyPrepares;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.BusInterceptors;
import com.stellariver.milky.domain.support.repository.DomainRepository;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class DomainSupportDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, @Nonnull BeanDefinitionRegistry registry) {
        AnnotationAttributes enableMilky = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableMilky.class.getName()));
        if (enableMilky == null) {
            return;
        }
        String[] scanPackages = Arrays.stream(enableMilky.getStringArray("scanPackages"))
                .filter(StringUtils::hasText).toArray(String[]::new);
        BeanDefinitionBuilder scanPackagesBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ScanPackages.class);
        scanPackagesBeanBuilder.addPropertyValue("packages", scanPackages);
        registry.registerBeanDefinition("scanPackages", scanPackagesBeanBuilder.getBeanDefinition());
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AssignableTypeFilter(DependencyPrepares.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(EventRouters.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(DomainRepository.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(BusInterceptors.class));
        scanner.scan(scanPackages);
    }
}

