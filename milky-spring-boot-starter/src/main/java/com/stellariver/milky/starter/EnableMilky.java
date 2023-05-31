package com.stellariver.milky.starter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * This annotation is used to enable milky abilities, supposed to place
 * with @SpringBootApplication or any class annotated with @Configuration,
 * the scanPackages means every milky related java class will be scanned, like
 * the annotation of @ComponentScan, so this annotation is suggested with @SpringbootApplication, reasonable!
 * @author houchuang
 */
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({DomainSupportDefinitionRegistrar.class, DomainSupportAutoConfiguration.class})
public @interface EnableMilky {

    /**
     * Base packages to scan for milky annotated components.
     * @return scan packages
     */
    String[] scanPackages() default {};

}
