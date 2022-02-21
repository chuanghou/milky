package com.stellariver.milky.starter;

import org.springframework.context.annotation.Import;

@Import(DomainSupportDefinitionRegistrar.class)
public @interface EnableMilky {

    String domainPackage();

}
