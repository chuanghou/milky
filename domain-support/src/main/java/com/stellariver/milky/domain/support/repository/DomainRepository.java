package com.stellariver.milky.domain.support.repository;


import com.stellariver.milky.domain.support.base.AggregateRoot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainRepository {

    Class<? extends AggregateRoot> value();

}
