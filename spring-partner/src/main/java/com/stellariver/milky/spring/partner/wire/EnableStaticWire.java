package com.stellariver.milky.spring.partner.wire;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({StaticWireScanPackagesDefinitionRegistrar.class, StaticWireAutoConfiguration.class})
public @interface EnableStaticWire {

    /**
     * Base packages to scan for milky annotated components.
     */
    String[] scanPackages() default {};

}
