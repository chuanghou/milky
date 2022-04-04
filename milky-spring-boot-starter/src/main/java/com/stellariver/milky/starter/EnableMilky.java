package com.stellariver.milky.starter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({DomainSupportDefinitionRegistrar.class, DomainSupportAutoConfiguration.class})
public @interface EnableMilky {

    String[] value();

}
