package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.validate.tool.CustomValid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.PARAM_IS_NULL;

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
        BizException.trueThrow(number == null, PARAM_IS_NULL.message("number"), false);

    }

    @CustomValid(groups = NameGroup.class)
    public void nameTest() {
        BizException.trueThrow(name == null, PARAM_IS_NULL.message("name不能为空"), false);
    }

    interface NameGroup{}

}
