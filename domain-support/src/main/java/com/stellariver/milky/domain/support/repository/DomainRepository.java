package com.stellariver.milky.domain.support.repository;


import com.stellariver.milky.domain.support.base.AggregateRoot;
import org.springframework.stereotype.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Repository
public @interface DomainRepository {

    Class<? extends AggregateRoot> value();

}
