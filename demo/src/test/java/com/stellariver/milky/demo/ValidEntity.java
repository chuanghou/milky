package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.demo.basic.ErrorEnums;
import com.stellariver.milky.validate.tool.CustomValid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidEntity {

    Long number;

    String name;


    @CustomValid
    public void numberTest() {
        BizException.nullThrow(number, () -> ErrorEnums.PARAM_IS_NULL.message("number不能为空"));
    }

    @CustomValid(groups = NameGroup.class)
    public void nameTest() {
        BizException.nullThrow(name, () -> ErrorEnums.PARAM_IS_NULL.message("name不能为空"));
    }

    interface NameGroup{}

}
