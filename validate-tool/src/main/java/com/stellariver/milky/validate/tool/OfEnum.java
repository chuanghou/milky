package com.stellariver.milky.validate.tool;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

@Target(ElementType.FIELD)
@Constraint(validatedBy = OfEnumValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface OfEnum {

    Class<? extends Enum<?>> enumType();

    // if field is blank, used enum field name as key
    String field() default "";

    // when the check key is enum name or the field selected is String type
    // the selected values could be a subset code of selected enum values
    String[] selected() default {};

    String message() default "${validatedValue}不在允许的${enumKeys}范围之内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
