package com.stellariver.milky.spring.partner;

import com.stellariver.milky.common.tool.util.Json;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class LocalCondition implements Condition {

    static private final Map<Class<? extends Annotation>, Predicate<AnnotatedTypeMetadata>> predicates = new HashMap<>();
    static {

        predicates.put(Bean.class, metadata -> {
            MergedAnnotations annotations = metadata.getAnnotations();
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Bean.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

        predicates.put(Component.class, metadata -> {
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Component.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

        predicates.put(Repository.class, metadata -> {
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Repository.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

        predicates.put(Service.class, metadata -> {
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Service.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

        predicates.put(Controller.class, metadata -> {
            MergedAnnotations annotations = metadata.getAnnotations();
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Controller.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

        predicates.put(RestController.class, metadata -> {
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(RestController.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

        predicates.put(Configuration.class, metadata -> {
            MergedAnnotations annotations = metadata.getAnnotations();
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Configuration.class.getName());
            System.out.println(Json.toJson(annotationAttributes));
            return true;
        });

    }


    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        boolean localProfile = Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("local");
        if (!localProfile) {
            return true;
        }
        return predicates.entrySet().stream().anyMatch(e -> metadata.isAnnotated(e.getKey().getName()) && e.getValue().test(metadata));
    }


}
