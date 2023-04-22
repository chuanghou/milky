package com.stellariver.milky.spring.partner;

import lombok.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class SpringPartnerDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry registry) {

        AnnotationAttributes enableSpringPartner = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableSpringPartner.class.getName()));
        if (enableSpringPartner == null) {
            return;
        }
        String[] scanPackages = Arrays.stream(enableSpringPartner.getStringArray("scanPackages")).filter(StringUtils::hasText).toArray(String[]::new);
        if (scanPackages.length == 0) {
            String declaringClass = annotationMetadata.getClassName();
            String packageName = ClassUtils.getPackageName(declaringClass);
            scanPackages = new String[]{packageName};
        }
        BeanDefinitionBuilder scanPackagesBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SpringPartnerScanPackages.class);
        scanPackagesBeanBuilder.addPropertyValue("scanPackages", scanPackages);
        registry.registerBeanDefinition("springPartnerScanPackages", scanPackagesBeanBuilder.getBeanDefinition());
    }

}
