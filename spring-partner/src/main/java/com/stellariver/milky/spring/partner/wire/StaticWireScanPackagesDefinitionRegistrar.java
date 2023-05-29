package com.stellariver.milky.spring.partner.wire;

import lombok.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class StaticWireScanPackagesDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry registry) {

        AnnotationAttributes enableStaticWire = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableStaticWire.class.getName()));
        if (enableStaticWire == null) {
            return;
        }
        String[] scanPackages = Arrays.stream(enableStaticWire.getStringArray("scanPackages")).filter(StringUtils::hasText).toArray(String[]::new);
        if (scanPackages.length == 0) {
            String declaringClass = annotationMetadata.getClassName();
            String packageName = ClassUtils.getPackageName(declaringClass);
            scanPackages = new String[]{packageName};
        }
        BeanDefinitionBuilder scanPackagesBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(StaticWireScanPackages.class);
        scanPackagesBeanBuilder.addPropertyValue("scanPackages", scanPackages);
        registry.registerBeanDefinition("staticWireScanPackages", scanPackagesBeanBuilder.getBeanDefinition());

    }

}
