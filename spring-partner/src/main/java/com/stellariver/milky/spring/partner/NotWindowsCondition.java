package com.stellariver.milky.spring.partner;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NotWindowsCondition implements Condition {

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
