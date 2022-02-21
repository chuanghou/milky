package com.stellariver.milky.starter;

import com.stellariver.milky.domain.support.context.ContextPrepareProcessor;
import com.stellariver.milky.domain.support.event.EventProcessor;
import com.stellariver.milky.domain.support.repository.DomainRepositoryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DomainSupportDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, @Nonnull BeanDefinitionRegistry registry) {
        AnnotationAttributes enableMilky = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableMilky.class.getName()));
        if (enableMilky != null) {
            String[] basePackages = Arrays.stream(enableMilky.getStringArray("basePackages"))
                    .filter(StringUtils::hasText).toArray(String[]::new);
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AssignableTypeFilter(ContextPrepareProcessor.class));
            scanner.addIncludeFilter(new AssignableTypeFilter(EventProcessor.class));
            scanner.addIncludeFilter(new AssignableTypeFilter(DomainRepositoryService.class));
            scanner.scan(basePackages);
        }
    }

    @Override
    public void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

