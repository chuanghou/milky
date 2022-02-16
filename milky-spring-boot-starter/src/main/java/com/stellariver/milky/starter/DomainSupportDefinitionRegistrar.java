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
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nonnull;

public class DomainSupportDefinitionRegistrar implements
        ImportBeanDefinitionRegistrar, BeanFactoryAware, ResourceLoaderAware {

    private ConfigurableListableBeanFactory beanFactory;

    private ResourceLoader resourceLoader;

    @Override
    public void setBeanFactory(@Nonnull BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }
    @Override
    public void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    @Override
    public void registerBeanDefinitions(@Nonnull AnnotationMetadata annotationMetadata, @Nonnull BeanDefinitionRegistry registry) {
        MilkyProperties milkyProperties = beanFactory.getBean(MilkyProperties.class);
        String domainPackage = milkyProperties.getDomainPackage();
        DomainSupportBeanDefinitionScanner scanner = new DomainSupportBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(resourceLoader);
        scanner.registerFilters();
        scanner.addIncludeFilter(new AssignableTypeFilter(ContextPrepareProcessor.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(EventProcessor.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(DomainRepositoryService.class));
        scanner.doScan(domainPackage);
    }
}

