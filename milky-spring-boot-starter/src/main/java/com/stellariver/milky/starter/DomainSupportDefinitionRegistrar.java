package com.stellariver.milky.starter;

import com.stellariver.milky.domain.support.base.MilkyScanPackages;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.dependency.DaoAdapter;
import com.stellariver.milky.domain.support.util.ThreadLocalPasser;
import lombok.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * @author houchuang
 */
public class DomainSupportDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry registry) {


        AnnotationAttributes enableMilky = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableMilky.class.getName()));
        if (enableMilky == null) {
            return;
        }
        String[] scanPackages = Arrays.stream(enableMilky.getStringArray("scanPackages")).filter(StringUtils::hasText).toArray(String[]::new);
        if (scanPackages.length == 0) {
            String declaringClass = annotationMetadata.getClassName();
            String packageName = ClassUtils.getPackageName(declaringClass);
            scanPackages = new String[] { packageName };
        }
        BeanDefinitionBuilder scanPackagesBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MilkyScanPackages.class);
        scanPackagesBeanBuilder.addPropertyValue("scanPackages", scanPackages);
        registry.registerBeanDefinition("scanPackages", scanPackagesBeanBuilder.getBeanDefinition());
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AssignableTypeFilter(EventRouters.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(DaoAdapter.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(DAOWrapper.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(Interceptors.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(TraceRepository.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(ThreadLocalPasser.class));
        scanner.scan(scanPackages);
    }
}

