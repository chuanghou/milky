package com.stellariver.milky.starter;

import com.stellariver.milky.domain.support.base.DomainPackages;
import com.stellariver.milky.domain.support.context.ContextPrepareProcessor;
import com.stellariver.milky.domain.support.event.EventRouters;
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
        if (enableMilky != null) {
            String[] domainPackages = Arrays.stream(enableMilky.getStringArray("domainPackages"))
                    .filter(StringUtils::hasText).toArray(String[]::new);
            BeanDefinitionBuilder domainPackagesBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DomainPackages.class);
            domainPackagesBeanBuilder.addPropertyValue("packages", domainPackages);
            registry.registerBeanDefinition("domainPackages", domainPackagesBeanBuilder.getBeanDefinition());
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AssignableTypeFilter(ContextPrepareProcessor.class));
            scanner.addIncludeFilter(new AssignableTypeFilter(EventRouters.class));
            scanner.addIncludeFilter(new AssignableTypeFilter(DomainRepository.class));
            scanner.scan(domainPackages);
        }
    }
}

