package com.stellariver.milky.common.tool.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Constraint(validatedBy = OfEnumValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface OfEnum {

    Class<? extends Enum<?>> enumType();

    // if field is blank, used enum field name as key
    String field() default "";



    String message() default "目标值必须是 ${enumType.simpleName} 类型的枚举值";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
