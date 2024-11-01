package com.stellariver.milky.common.base;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated element must be the name of an enum object of the enum class {@link OfEnum#enumType}
 * or any field of this enum class, default is enum name
 * you can also assign some selected ones, but the field must be string type or enum name
 * {@code null} elements is considered valid.
 *
 * @author houchuang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
public @interface OfEnum {

    Class<? extends Enum<?>> enumType();

    /**
     * if field is blank, use enum name as key
     */
    String field() default "";

    boolean blankAsNull() default false;

    /**
     * when the check key is enum name or the field selected is String type
     * the selected values could be a subset code of selected enum values
     */
    String[] selectedEnums() default {};

    String message() default "${validatedValue}不在允许的${enumKeys}范围之内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
