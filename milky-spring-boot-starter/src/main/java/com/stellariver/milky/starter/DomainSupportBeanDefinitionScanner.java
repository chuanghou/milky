package com.stellariver.milky.starter;

import com.stellariver.milky.domain.support.context.ContextPrepareProcessor;
import com.stellariver.milky.domain.support.event.EventProcessor;
import com.stellariver.milky.domain.support.repository.DomainRepositoryService;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Set;

public class DomainSupportBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public DomainSupportBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    protected void registerFilters() {
        addIncludeFilter(new AssignableTypeFilter(ContextPrepareProcessor.class));
        addIncludeFilter(new AssignableTypeFilter(EventProcessor.class));
        addIncludeFilter(new AssignableTypeFilter(DomainRepositoryService.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }
}
