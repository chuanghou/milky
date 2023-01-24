package com.stellariver.milky.demo;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ValidEntity.MyTestGroup
public class ValidEntity {

    Long number;


    @Target(ElementType.TYPE)
    @Constraint(validatedBy = TestGroupValidator.class)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MyTestGroup {}

    static class TestGroupValidator implements ConstraintValidator<MyTestGroup, ValidEntity> {

        @Override
        public boolean isValid(ValidEntity validEntity, ConstraintValidatorContext context) {
            return validEntity.getNumber() == null;
        }

    }



}
