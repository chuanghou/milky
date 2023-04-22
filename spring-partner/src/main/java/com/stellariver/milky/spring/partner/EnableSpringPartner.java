package com.stellariver.milky.spring.partner;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({SpringPartnerDefinitionRegistrar.class, SpringPartnerAutoConfiguration.class})
public @interface EnableSpringPartner {

    /**
     * Base packages to scan for milky annotated components.
     */
    String[] scanPackages() default {};

}
